import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { CreateScheduleInput, CurrencyCode, PaymentFrequency, PaymentMode, PaymentSchedule, UpdateScheduleInput, Worker } from '../../core/models/api.models';
import { PayrollApiService } from '../../core/services/payroll-api.service';
import { ToastService } from '../../core/services/toast.service';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { StatusPillComponent } from '../../shared/status-pill/status-pill.component';

@Component({
  selector: 'app-schedules',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, EmptyStateComponent, StatusPillComponent],
  template: `
    <div class="page">
      <header class="page-header"><div><p class="eyebrow">Payroll planning</p><h1>Payment schedules</h1><p>Plan recurring and one-time payments without losing oversight.</p></div><div class="page-actions"><button class="btn btn-primary" (click)="openCreate()">New schedule <span>＋</span></button></div></header>
      <section class="summary-row">
        <div class="card"><span class="summary-icon">□</span><p><small>Active schedules</small><strong>{{ activeCount() }}</strong></p></div>
        <div class="card"><span class="summary-icon amber">Ⅱ</span><p><small>Paused</small><strong>{{ pausedCount() }}</strong></p></div>
        <div class="card"><span class="summary-icon blue">→</span><p><small>Next due</small><strong>{{ nextDue() ? (nextDue() | date:'d MMM') : '—' }}</strong></p></div>
      </section>
      <section class="toolbar"><div class="search"><input type="search" placeholder="Search schedules…" aria-label="Search schedules" (input)="setSearch($event)"></div><select class="filter-select" (change)="setFilter($event)" aria-label="Filter schedules"><option value="ALL">All schedules</option><option value="ACTIVE">Active</option><option value="PAUSED">Paused</option><option value="COMPLETED">Completed</option></select></section>
      <section class="card schedules-card">
        @if (loading()) { <div class="schedule-skeletons">@for (item of [1,2,3,4]; track item) { <div><i class="skeleton"></i><p><span class="skeleton"></span><span class="skeleton"></span></p><b class="skeleton"></b></div> }</div> }
        @else if (filtered().length) {
          <div class="table-wrap"><table><thead><tr><th>Schedule</th><th>Worker</th><th>Frequency</th><th>Next payment</th><th>Amount</th><th>Mode</th><th>Status</th><th></th></tr></thead><tbody>
            @for (schedule of filtered(); track schedule.id) {
              <tr>
                <td><div class="schedule-name"><span>{{ schedule.frequency === 'ONE_TIME' ? '1×' : '↻' }}</span><div><strong>{{ schedule.scheduleName }}</strong><small>Created {{ schedule.createdAt | date:'d MMM y' }}</small></div></div></td>
                <td class="cell-primary">{{ schedule.workerName }}</td><td>{{ formatLabel(schedule.frequency) }}</td><td>{{ schedule.nextDueDate | date:'d MMM y' }}</td><td class="amount">{{ schedule.amount | currency:schedule.currency:'symbol-narrow':'1.0-2' }}</td>
                <td><span class="mode-chip" [class.auto]="schedule.paymentMode === 'AUTOMATIC'">{{ schedule.paymentMode === 'AUTOMATIC' ? 'Automatic' : 'Manual approval' }}</span></td><td><app-status-pill [status]="schedule.status" /></td>
                <td><div class="row-actions"><button class="btn btn-ghost" (click)="openEdit(schedule)">Edit</button>@if (schedule.status === 'ACTIVE') { <button class="icon-btn" title="Pause schedule" (click)="pause(schedule)">Ⅱ</button> } @else if (schedule.status === 'PAUSED') { <button class="icon-btn" title="Resume schedule" (click)="resume(schedule)">▶</button> }</div></td>
              </tr>
            }
          </tbody></table></div>
        } @else if (schedules().length) { <app-empty-state title="No matching schedules" message="Try changing the search or status filter." icon="⌕" /> }
        @else { <app-empty-state title="Plan your first payment" message="Once a worker has verified payment details, create a schedule for them here." icon="□" /><div class="empty-action"><button class="btn btn-primary" (click)="openCreate()">Create schedule</button></div> }
      </section>
    </div>

    @if (panelOpen()) {
      <button class="panel-backdrop" aria-label="Close schedule form" (click)="closePanel()"></button>
      <aside class="side-panel" role="dialog" aria-modal="true">
        <div class="panel-header"><div><p class="eyebrow">{{ editing() ? 'Update plan' : 'New payment plan' }}</p><h2>{{ editing() ? 'Edit payment schedule' : 'Create a payment schedule' }}</h2><p>Manual approval is the safest option for a new schedule.</p></div><button class="icon-btn" aria-label="Close" (click)="closePanel()">×</button></div>
        <form id="schedule-form" class="panel-content" [formGroup]="form" (ngSubmit)="save()">
          @if (!editing() && eligibleWorkers().length === 0) { <div class="alert alert-warning no-workers">Add and verify a worker’s payment details before creating a schedule.</div> }
          <div class="form-grid">
            @if (!editing()) { <div class="field field-span"><label for="scheduleWorker">Worker *</label><select id="scheduleWorker" formControlName="workerId" (change)="workerChanged()"><option [ngValue]="0" disabled>Select a payment-ready worker</option>@for (worker of eligibleWorkers(); track worker.id) { <option [ngValue]="worker.id">{{ worker.fullName }} · {{ worker.preferredCurrency }}</option> }</select></div> }
            <div class="field field-span"><label for="scheduleName">Schedule name *</label><input id="scheduleName" formControlName="scheduleName" placeholder="e.g. Monthly household payroll"></div>
            <div class="field"><label for="amount">Amount *</label><input id="amount" type="number" min="0.01" step="0.01" formControlName="amount" placeholder="0.00"></div>
            <div class="field"><label for="scheduleCurrency">Currency</label><select id="scheduleCurrency" formControlName="currency"><option value="EUR">EUR — Euro</option><option value="NGN">NGN — Nigerian Naira</option></select></div>
            <div class="field"><label for="frequency">Frequency *</label><select id="frequency" formControlName="frequency"><option value="ONE_TIME">One time</option><option value="WEEKLY">Weekly</option><option value="BI_WEEKLY">Every two weeks</option><option value="MONTHLY">Monthly</option></select></div>
            <div class="field"><label for="mode">Payment mode *</label><select id="mode" formControlName="paymentMode"><option value="MANUAL_APPROVAL">Manual approval</option><option value="AUTOMATIC">Automatic</option></select></div>
            @if (form.controls.paymentMode.value === 'AUTOMATIC') { <div class="alert alert-warning field-span">Automatic payments execute at the due date through the configured provider. Use this only when the destination has been carefully reviewed.</div> }
            @if (!editing()) {
              <div class="field"><label for="startDate">Start date *</label><input id="startDate" type="date" formControlName="startDate"></div>
              <div class="field"><label for="firstDueDate">First due date *</label><input id="firstDueDate" type="date" formControlName="firstDueDate"></div>
            } @else { <div class="field field-span"><label for="nextDueDate">Next due date *</label><input id="nextDueDate" type="date" formControlName="nextDueDate"></div> }
            <div class="field"><label for="endDate">End date</label><input id="endDate" type="date" formControlName="endDate"><span class="field-hint">Leave blank for an ongoing schedule.</span></div>
            <div class="field"><label for="reminder">Reminder</label><select id="reminder" formControlName="reminderDaysBefore"><option [ngValue]="1">1 day before</option><option [ngValue]="2">2 days before</option><option [ngValue]="3">3 days before</option><option [ngValue]="7">1 week before</option></select></div>
          </div>
        </form>
        <div class="panel-footer">@if (editing(); as schedule) { <button class="btn btn-danger cancel-plan" type="button" (click)="cancel(schedule)">Cancel schedule</button> }<button class="btn btn-secondary" (click)="closePanel()">Close</button><button class="btn btn-primary" type="submit" form="schedule-form" [disabled]="saving() || (!editing() && !eligibleWorkers().length)">@if (saving()) { <span class="spinner"></span> Saving… } @else { {{ editing() ? 'Save changes' : 'Create schedule' }} }</button></div>
      </aside>
    }
  `,
  styleUrl: './schedules.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SchedulesComponent implements OnInit {
  private readonly api = inject(PayrollApiService); private readonly toast = inject(ToastService); private readonly fb = inject(FormBuilder);
  readonly schedules = signal<PaymentSchedule[]>([]); readonly workers = signal<Worker[]>([]); readonly loading = signal(true); readonly saving = signal(false); readonly panelOpen = signal(false); readonly editing = signal<PaymentSchedule | null>(null); readonly search = signal(''); readonly filter = signal('ALL');
  readonly eligibleWorkers = computed(() => this.workers().filter(worker => worker.status === 'ACTIVE' && worker.paymentDetailsVerified));
  readonly activeCount = computed(() => this.schedules().filter(schedule => schedule.status === 'ACTIVE').length);
  readonly pausedCount = computed(() => this.schedules().filter(schedule => schedule.status === 'PAUSED').length);
  readonly nextDue = computed(() => this.schedules().filter(schedule => schedule.status === 'ACTIVE').map(schedule => schedule.nextDueDate).sort()[0] || '');
  readonly filtered = computed(() => { const query = this.search().toLowerCase(); return this.schedules().filter(schedule => (this.filter() === 'ALL' || schedule.status === this.filter()) && (!query || schedule.scheduleName.toLowerCase().includes(query) || schedule.workerName.toLowerCase().includes(query))); });
  readonly form = this.fb.nonNullable.group({
    workerId: [0, Validators.min(1)], scheduleName: ['', Validators.required], amount: [0, Validators.min(.01)], currency: ['EUR' as CurrencyCode, Validators.required], frequency: ['MONTHLY' as PaymentFrequency, Validators.required], paymentMode: ['MANUAL_APPROVAL' as PaymentMode, Validators.required], startDate: ['', Validators.required], firstDueDate: ['', Validators.required], nextDueDate: [''], endDate: [''], reminderDaysBefore: [1, Validators.min(1)]
  });

  constructor() {
    this.form.controls.currency.disable();
  }

  ngOnInit(): void { this.load(); }
  load(): void { this.loading.set(true); forkJoin({ schedules: this.api.schedules(), workers: this.api.workers() }).subscribe({ next: data => { this.schedules.set(data.schedules); this.workers.set(data.workers); this.loading.set(false); }, error: (error: Error) => { this.toast.error(error.message); this.loading.set(false); } }); }
  openCreate(): void { const today = this.localDate(new Date()); const firstWorker = this.eligibleWorkers()[0]; this.editing.set(null); this.form.reset({ workerId: firstWorker?.id || 0, scheduleName: '', amount: 0, currency: firstWorker?.preferredCurrency || 'EUR', frequency: 'MONTHLY', paymentMode: 'MANUAL_APPROVAL', startDate: today, firstDueDate: today, nextDueDate: '', endDate: '', reminderDaysBefore: 1 }); this.panelOpen.set(true); }
  openEdit(schedule: PaymentSchedule): void { this.editing.set(schedule); this.form.reset({ workerId: schedule.workerId, scheduleName: schedule.scheduleName, amount: schedule.amount, currency: schedule.currency, frequency: schedule.frequency, paymentMode: schedule.paymentMode, startDate: schedule.startDate, firstDueDate: schedule.nextDueDate, nextDueDate: schedule.nextDueDate, endDate: schedule.endDate || '', reminderDaysBefore: schedule.reminderDaysBefore }); this.panelOpen.set(true); }
  closePanel(): void { if (!this.saving()) this.panelOpen.set(false); }
  workerChanged(): void { const worker = this.workers().find(item => item.id === this.form.controls.workerId.value); if (worker) this.form.controls.currency.setValue(worker.preferredCurrency); }
  save(): void {
    this.form.markAllAsTouched(); const value = this.form.getRawValue(); const current = this.editing();
    if (this.form.invalid || this.saving() || (!current && (!value.startDate || !value.firstDueDate))) { this.toast.error('Complete the required schedule fields.'); return; }
    this.saving.set(true);
    const request = current
      ? this.api.updateSchedule(current.id, { scheduleName: value.scheduleName, amount: value.amount, frequency: value.frequency, paymentMode: value.paymentMode, nextDueDate: value.nextDueDate, endDate: value.endDate || undefined, reminderDaysBefore: value.reminderDaysBefore } as UpdateScheduleInput)
      : this.api.createSchedule({ workerId: value.workerId, scheduleName: value.scheduleName, amount: value.amount, currency: value.currency, frequency: value.frequency, paymentMode: value.paymentMode, startDate: value.startDate, firstDueDate: value.firstDueDate, endDate: value.endDate || undefined, reminderDaysBefore: value.reminderDaysBefore } as CreateScheduleInput);
    request.subscribe({ next: schedule => { this.schedules.update(items => current ? items.map(item => item.id === schedule.id ? schedule : item) : [schedule, ...items]); this.saving.set(false); this.panelOpen.set(false); this.toast.success(current ? 'Schedule updated.' : 'Payment schedule created.'); }, error: (error: Error) => { this.toast.error(error.message); this.saving.set(false); } });
  }
  pause(schedule: PaymentSchedule): void { this.api.pauseSchedule(schedule.id).subscribe({ next: item => { this.replace(item); this.toast.success('Schedule paused.'); }, error: (error: Error) => this.toast.error(error.message) }); }
  resume(schedule: PaymentSchedule): void { this.api.resumeSchedule(schedule.id).subscribe({ next: item => { this.replace(item); this.toast.success('Schedule resumed.'); }, error: (error: Error) => this.toast.error(error.message) }); }
  cancel(schedule: PaymentSchedule): void { if (!confirm(`Cancel ${schedule.scheduleName}? Existing payment records will remain in history.`)) return; this.api.cancelSchedule(schedule.id).subscribe({ next: () => { this.schedules.update(items => items.filter(item => item.id !== schedule.id)); this.panelOpen.set(false); this.toast.success('Schedule cancelled.'); }, error: (error: Error) => this.toast.error(error.message) }); }
  replace(schedule: PaymentSchedule): void { this.schedules.update(items => items.map(item => item.id === schedule.id ? schedule : item)); }
  setSearch(event: Event): void { this.search.set((event.target as HTMLInputElement).value); }
  setFilter(event: Event): void { this.filter.set((event.target as HTMLSelectElement).value); }
  formatLabel(value: string): string { return value.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, letter => letter.toUpperCase()); }
  localDate(date: Date): string { const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000); return local.toISOString().slice(0, 10); }
}
