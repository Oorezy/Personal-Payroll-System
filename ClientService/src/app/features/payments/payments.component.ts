import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PaymentRecord, PaymentStatus } from '../../core/models/api.models';
import { PayrollApiService } from '../../core/services/payroll-api.service';
import { ToastService } from '../../core/services/toast.service';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { StatusPillComponent } from '../../shared/status-pill/status-pill.component';

type PaymentFilter = 'ALL' | PaymentStatus;

@Component({
  selector: 'app-payments',
  imports: [CurrencyPipe, DatePipe, FormsModule, EmptyStateComponent, StatusPillComponent],
  template: `
    <div class="page">
      <header class="page-header"><div><p class="eyebrow">Payment centre</p><h1>Payments</h1><p>Review, approve and track every payroll transfer.</p></div><div class="page-actions"><span class="secure-chip">▣ Provider-secured transfers</span></div></header>

      <section class="payment-metrics">
        <div class="card"><span class="metric-icon amber">!</span><p><small>Awaiting approval</small><strong>{{ count('AWAITING_APPROVAL') }}</strong></p></div>
        <div class="card"><span class="metric-icon blue">↻</span><p><small>Processing</small><strong>{{ count('PROCESSING') }}</strong></p></div>
        <div class="card"><span class="metric-icon green">✓</span><p><small>Paid</small><strong>{{ count('PAID') }}</strong></p></div>
        <div class="card"><span class="metric-icon red">×</span><p><small>Failed</small><strong>{{ count('FAILED') }}</strong></p></div>
      </section>

      <section class="filter-card card">
        <div class="tabs" role="tablist">
          @for (tab of tabs; track tab.value) { <button role="tab" [class.active]="filter() === tab.value" [attr.aria-selected]="filter() === tab.value" (click)="filter.set(tab.value)">{{ tab.label }} <span>{{ tab.value === 'ALL' ? payments().length : count(tab.value) }}</span></button> }
        </div>
        <div class="filter-tools"><div class="search"><input type="search" placeholder="Search payments…" (input)="setSearch($event)" aria-label="Search payments"></div><select class="filter-select" (change)="setCurrency($event)" aria-label="Filter by currency"><option value="ALL">All currencies</option><option value="EUR">EUR</option><option value="NGN">NGN</option></select></div>
      </section>

      <section class="card payments-card">
        @if (loading()) { <div class="payment-skeletons">@for (item of [1,2,3,4,5]; track item) { <div><i class="skeleton"></i><p><span class="skeleton"></span><span class="skeleton"></span></p><b class="skeleton"></b></div> }</div> }
        @else if (filteredPayments().length) {
          <div class="table-wrap"><table><thead><tr><th>Worker</th><th>Due date</th><th>Schedule</th><th>Amount</th><th>Provider</th><th>Status</th><th></th></tr></thead><tbody>
            @for (payment of filteredPayments(); track payment.id) {
              <tr [class.attention-row]="payment.status === 'AWAITING_APPROVAL' || payment.status === 'FAILED'">
                <td><div class="person-cell"><span class="avatar">{{ initials(payment.workerName) }}</span><span><span class="cell-primary">{{ payment.workerName }}</span><span class="cell-secondary">Payment #{{ payment.id }}</span></span></div></td>
                <td><span class="date-primary">{{ payment.dueDate | date:'d MMM y' }}</span><span class="cell-secondary">{{ relativeDate(payment.dueDate) }}</span></td><td>{{ payment.scheduleName || 'Payroll payment' }}</td><td class="amount">{{ payment.amount | currency:payment.currency:'symbol-narrow':'1.0-2' }}</td>
                <td><span class="provider">{{ payment.providerName ? formatLabel(payment.providerName) : 'Not initiated' }}</span></td><td><app-status-pill [status]="payment.status" /></td><td><button class="btn btn-ghost" (click)="open(payment)">View details →</button></td>
              </tr>
            }
          </tbody></table></div>
        } @else if (payments().length) { <app-empty-state title="No matching payments" message="Try another status, currency or search term." icon="⌕" /> }
        @else { <app-empty-state title="No payments yet" message="Payment records appear automatically when a schedule reaches its due date." icon="↗" /> }
      </section>
    </div>

    @if (selected(); as payment) {
      <button class="panel-backdrop" aria-label="Close payment details" (click)="selected.set(null)"></button>
      <aside class="side-panel payment-panel" role="dialog" aria-modal="true">
        <div class="panel-header"><div><p class="eyebrow">Payment #{{ payment.id }}</p><h2>{{ payment.workerName }}</h2><p>{{ payment.scheduleName || 'Payroll payment' }}</p></div><button class="icon-btn" aria-label="Close" (click)="selected.set(null)">×</button></div>
        <div class="panel-content">
          <div class="payment-amount"><span>{{ payment.status === 'PAID' ? 'Amount paid' : 'Payment amount' }}</span><strong>{{ payment.amount | currency:payment.currency:'symbol-narrow':'1.0-2' }}</strong><app-status-pill [status]="payment.status" /></div>
          @if (payment.status === 'AWAITING_APPROVAL') { <div class="review-callout"><span>!</span><p><strong>Review before you approve</strong><small>Approving sends this payment to the configured provider. This action cannot be undone once processing begins.</small></p></div> }
          @if (payment.status === 'FAILED') { <div class="failure-callout"><span>×</span><p><strong>Transfer failed</strong><small>{{ payment.failureReason || 'The payment provider could not complete this transfer.' }}</small></p></div> }
          <dl class="details-list">
            <div><dt>Worker</dt><dd>{{ payment.workerName }}</dd></div><div><dt>Schedule</dt><dd>{{ payment.scheduleName || '—' }}</dd></div><div><dt>Due date</dt><dd>{{ payment.dueDate | date:'EEEE, d MMMM y' }}</dd></div><div><dt>Currency</dt><dd>{{ payment.currency }}</dd></div><div><dt>Provider</dt><dd>{{ payment.providerName ? formatLabel(payment.providerName) : 'Not initiated' }}</dd></div><div><dt>Transfer reference</dt><dd class="mono">{{ payment.providerTransferReference || '—' }}</dd></div><div><dt>Retry attempts</dt><dd>{{ payment.retryCount }} / 3</dd></div>
          </dl>
          @if (payment.notes) { <div class="note"><span>Payment note</span><p>{{ payment.notes }}</p></div> }
          @if (payment.status === 'AWAITING_APPROVAL' || payment.status === 'FAILED') { <div class="field action-note"><label for="actionNote">Add a note <span>(optional)</span></label><textarea id="actionNote" [(ngModel)]="actionNote" placeholder="A private note about this action"></textarea></div> }
          <div class="timeline"><h3>Payment timeline</h3><div class="timeline-item complete"><i></i><p><strong>Payment created</strong><span>{{ payment.createdDate | date:'d MMM y, HH:mm' }}</span></p></div>@if (payment.approvedAt) { <div class="timeline-item complete"><i></i><p><strong>Approved</strong><span>{{ payment.approvedAt | date:'d MMM y, HH:mm' }}</span></p></div> }@if (payment.processedAt) { <div class="timeline-item complete"><i></i><p><strong>Sent to provider</strong><span>{{ payment.processedAt | date:'d MMM y, HH:mm' }}</span></p></div> }@if (payment.confirmedAt) { <div class="timeline-item complete"><i></i><p><strong>Provider confirmation</strong><span>{{ payment.confirmedAt | date:'d MMM y, HH:mm' }}</span></p></div> }</div>
        </div>
        <div class="panel-footer payment-actions">
          @if (canCancel(payment)) { <button class="btn btn-ghost danger-text" [disabled]="acting()" (click)="cancel(payment)">Cancel</button><button class="btn btn-secondary" [disabled]="acting()" (click)="skip(payment)">Skip payment</button> }
          @if (payment.status === 'FAILED' && payment.retryCount < 3) { <button class="btn btn-primary push-right" [disabled]="acting()" (click)="retry(payment)">@if (acting()) { <span class="spinner"></span> Retrying… } @else { Retry payment → }</button> }
          @if (payment.status === 'AWAITING_APPROVAL') { <button class="btn btn-primary push-right" [disabled]="acting()" (click)="approve(payment)">@if (acting()) { <span class="spinner"></span> Approving… } @else { Approve and pay → }</button> }
          @if (!canAct(payment)) { <button class="btn btn-secondary push-right" (click)="selected.set(null)">Close</button> }
        </div>
      </aside>
    }
  `,
  styleUrl: './payments.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PaymentsComponent implements OnInit {
  private readonly api = inject(PayrollApiService); private readonly toast = inject(ToastService);
  readonly payments = signal<PaymentRecord[]>([]); readonly loading = signal(true); readonly filter = signal<PaymentFilter>('ALL'); readonly currency = signal<'ALL' | 'EUR' | 'NGN'>('ALL'); readonly search = signal(''); readonly selected = signal<PaymentRecord | null>(null); readonly acting = signal(false);
  actionNote = '';
  readonly tabs: { label: string; value: PaymentFilter }[] = [{ label: 'All', value: 'ALL' }, { label: 'Needs approval', value: 'AWAITING_APPROVAL' }, { label: 'Processing', value: 'PROCESSING' }, { label: 'Paid', value: 'PAID' }, { label: 'Failed', value: 'FAILED' }];
  readonly filteredPayments = computed(() => { const query = this.search().toLowerCase(); return this.payments().filter(payment => (this.filter() === 'ALL' || payment.status === this.filter()) && (this.currency() === 'ALL' || payment.currency === this.currency()) && (!query || payment.workerName.toLowerCase().includes(query) || payment.scheduleName?.toLowerCase().includes(query) || String(payment.id).includes(query))); });

  ngOnInit(): void { this.load(); }
  load(): void { this.loading.set(true); this.api.payments().subscribe({ next: payments => { this.payments.set(payments); this.loading.set(false); }, error: (error: Error) => { this.toast.error(error.message); this.loading.set(false); } }); }
  open(payment: PaymentRecord): void { this.actionNote = ''; this.selected.set(payment); }
  approve(payment: PaymentRecord): void { if (!confirm(`Approve ${this.currencyAmount(payment)} for ${payment.workerName}?`)) return; this.act(this.api.approvePayment(payment.id, this.actionNote), 'Payment approved and sent for processing.'); }
  retry(payment: PaymentRecord): void { this.act(this.api.retryPayment(payment.id, this.actionNote), 'Payment retry sent to the provider.'); }
  cancel(payment: PaymentRecord): void { if (!confirm('Cancel this payment?')) return; this.act(this.api.cancelPayment(payment.id), 'Payment cancelled.'); }
  skip(payment: PaymentRecord): void { if (!confirm('Skip this payment? It will remain in payment history.')) return; this.act(this.api.skipPayment(payment.id), 'Payment skipped.'); }
  act(request: ReturnType<PayrollApiService['cancelPayment']>, message: string): void { this.acting.set(true); request.subscribe({ next: updated => { this.payments.update(items => items.map(item => item.id === updated.id ? updated : item)); this.selected.set(updated); this.acting.set(false); this.toast.success(message); }, error: (error: Error) => { this.acting.set(false); this.toast.error(error.message); } }); }
  count(status: PaymentStatus): number { return this.payments().filter(payment => payment.status === status).length; }
  canCancel(payment: PaymentRecord): boolean { return ['SCHEDULED', 'DUE', 'AWAITING_APPROVAL', 'OVERDUE', 'FAILED'].includes(payment.status); }
  canAct(payment: PaymentRecord): boolean { return this.canCancel(payment) || (payment.status === 'FAILED' && payment.retryCount < 3); }
  initials(name: string): string { return name.split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase(); }
  formatLabel(value: string): string { return value.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, letter => letter.toUpperCase()); }
  relativeDate(value: string): string { const date = new Date(`${value}T00:00:00`); const today = new Date(); today.setHours(0,0,0,0); const days = Math.round((date.getTime() - today.getTime()) / 86400000); return days === 0 ? 'Due today' : days === 1 ? 'Due tomorrow' : days > 1 ? `In ${days} days` : `${Math.abs(days)} days ago`; }
  currencyAmount(payment: PaymentRecord): string { return new Intl.NumberFormat(undefined, { style: 'currency', currency: payment.currency }).format(payment.amount); }
  setSearch(event: Event): void { this.search.set((event.target as HTMLInputElement).value); }
  setCurrency(event: Event): void { this.currency.set((event.target as HTMLSelectElement).value as 'ALL' | 'EUR' | 'NGN'); }
}
