import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { CurrencyCode, PaymentDetails, PaymentDetailsInput, PaymentSchedule, Worker } from '../../core/models/api.models';
import { PayrollApiService } from '../../core/services/payroll-api.service';
import { ToastService } from '../../core/services/toast.service';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { StatusPillComponent } from '../../shared/status-pill/status-pill.component';

@Component({
  selector: 'app-worker-detail',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink, EmptyStateComponent, StatusPillComponent],
  template: `
    <div class="page">
      <a class="back-link" routerLink="/workers">← Back to workers</a>
      @if (loading()) {
        <div class="detail-skeleton card"><div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div></div>
      } @else if (worker(); as person) {
        <header class="profile-hero card">
          <div class="profile-main"><span class="big-avatar">{{ initials(person.fullName) }}</span><div><div class="profile-name"><h1>{{ person.fullName }}</h1><app-status-pill [status]="person.status" /></div><p>{{ person.jobTitle || person.category || 'Worker' }} · Added {{ person.createdAt | date:'d MMMM y' }}</p></div></div>
          <div class="profile-actions"><button class="btn btn-secondary" (click)="toggleStatus(person)">{{ person.status === 'ACTIVE' ? 'Deactivate' : 'Activate' }}</button><a class="btn btn-primary" href="#payment-details">Manage payment details</a></div>
        </header>

        <section class="detail-grid">
          <div class="main-column">
            <article id="payment-details" class="card payment-card">
              <div class="card-header"><div><p class="eyebrow">Payment destination</p><h2>{{ details()?.paymentDetailsVerified ? 'Verified payment details' : 'Complete payment setup' }}</h2><p class="muted">Sensitive account information is masked after it is saved.</p></div>@if (details()?.paymentDetailsVerified) { <span class="verified-seal">✓ Verified</span> }</div>
              @if (details()?.paymentDetailsVerified && !editingPayment()) {
                <div class="saved-details card-body">
                  <div><span>Account holder</span><strong>{{ details()?.accountHolderName }}</strong></div>
                  <div><span>Currency & region</span><strong>{{ details()?.currency }} · {{ details()?.paymentRegion }}</strong></div>
                  @if (details()?.currency === 'NGN') { <div><span>Bank</span><strong>{{ details()?.bankName }}</strong></div><div><span>Account number</span><strong class="mono">{{ details()?.maskedBankAccountNumber }}</strong></div> }
                  @if (details()?.currency === 'EUR') { <div class="wide"><span>IBAN</span><strong class="mono">{{ details()?.maskedIban }}</strong></div> }
                  <div class="wide security-note"><span>▣</span><p><strong>Your data stays protected</strong><small>Full account numbers are never returned by IntroTech after saving.</small></p></div>
                  <div class="wide"><button class="btn btn-secondary" (click)="editingPayment.set(true)">Update payment details</button></div>
                </div>
              } @else {
                <form class="card-body payment-form" [formGroup]="paymentForm" (ngSubmit)="savePaymentDetails()">
                  @if (details()?.paymentDetailsVerified) { <div class="alert alert-warning field-span">For security, enter the complete account details again when making an update.</div> }
                  <div class="form-grid">
                    <div class="field"><label for="detailCurrency">Currency</label><select id="detailCurrency" formControlName="currency" (change)="currencyChanged()"><option value="EUR">EUR — Euro</option><option value="NGN">NGN — Nigerian Naira</option></select></div>
                    <div class="field"><label for="detailRegion">Payment region</label><select id="detailRegion" formControlName="paymentRegion"><option value="Europe">Europe</option><option value="Nigeria">Nigeria</option></select></div>
                    <div class="field field-span"><label for="accountHolder">Account holder name *</label><input id="accountHolder" formControlName="accountHolderName" placeholder="Name exactly as shown on the account"></div>
                    @if (paymentForm.controls.currency.value === 'NGN') {
                      <div class="field"><label for="bankName">Bank name *</label><input id="bankName" formControlName="bankName" placeholder="e.g. Guaranty Trust Bank"></div>
                      <div class="field"><label for="accountNumber">10-digit account number *</label><input id="accountNumber" inputmode="numeric" maxlength="10" formControlName="bankAccountNumber" placeholder="0123456789"></div>
                    } @else {
                      <div class="field field-span"><label for="iban">IBAN *</label><input id="iban" formControlName="iban" autocomplete="off" placeholder="e.g. DE89 3704 0044 0532 0130 00"><span class="field-hint">Spaces are fine; we will normalise the IBAN securely.</span></div>
                    }
                  </div>
                  <div class="form-actions">@if (details()?.paymentDetailsVerified) { <button class="btn btn-secondary" type="button" (click)="editingPayment.set(false)">Cancel</button> }<button class="btn btn-primary" type="submit" [disabled]="saving()">@if (saving()) { <span class="spinner"></span> Securing details… } @else { Save and verify }</button></div>
                </form>
              }
            </article>

            <article class="card schedule-card">
              <div class="card-header"><div><h2>Payment schedules</h2><p class="muted">Plans currently linked to this worker</p></div><a class="text-button" routerLink="/schedules">Manage schedules</a></div>
              @if (schedules().length) {
                <div class="schedule-list">@for (schedule of schedules(); track schedule.id) { <div><span class="schedule-icon">□</span><p><strong>{{ schedule.scheduleName }}</strong><small>{{ schedule.frequency.replaceAll('_', ' ') }} · Next {{ schedule.nextDueDate | date:'d MMM y' }}</small></p><span class="amount">{{ schedule.amount | currency:schedule.currency:'symbol-narrow':'1.0-2' }}</span><app-status-pill [status]="schedule.status" /></div> }</div>
              } @else { <app-empty-state title="No schedules yet" message="Create a schedule when this worker is ready to be paid." icon="□" /> }
            </article>
          </div>

          <aside class="side-column">
            <article class="card info-card"><div class="card-header"><h2>Worker information</h2></div><dl><div><dt>Email</dt><dd>{{ person.email || 'Not provided' }}</dd></div><div><dt>Phone</dt><dd>{{ person.phoneNumber || 'Not provided' }}</dd></div><div><dt>Category</dt><dd>{{ person.category || 'Not provided' }}</dd></div><div><dt>Preferred currency</dt><dd>{{ person.preferredCurrency }}</dd></div><div><dt>Payment region</dt><dd>{{ person.paymentRegion || 'Not provided' }}</dd></div></dl></article>
            @if (person.notes) { <article class="card note-card"><p class="eyebrow">Private note</p><p>{{ person.notes }}</p></article> }
          </aside>
        </section>
      } @else { <section class="card"><app-empty-state title="Worker not found" message="This worker may have been archived or removed." icon="!" /></section> }
    </div>
  `,
  styleUrl: './worker-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkerDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(PayrollApiService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  readonly worker = signal<Worker | null>(null);
  readonly details = signal<PaymentDetails | null>(null);
  readonly schedules = signal<PaymentSchedule[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly editingPayment = signal(false);
  readonly paymentForm = this.fb.nonNullable.group({
    currency: ['EUR' as CurrencyCode, Validators.required], paymentRegion: ['Europe', Validators.required], accountHolderName: ['', Validators.required],
    bankName: [''], bankAccountNumber: [''], iban: ['']
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) { void this.router.navigate(['/workers']); return; }
    forkJoin({
      worker: this.api.worker(id),
      details: this.api.paymentDetails(id).pipe(catchError(() => of(null))),
      schedules: this.api.schedules()
    }).subscribe({
      next: result => {
        this.worker.set(result.worker); this.details.set(result.details); this.schedules.set(result.schedules.filter(schedule => schedule.workerId === id));
        const currency = result.details?.currency || result.worker.preferredCurrency;
        this.paymentForm.patchValue({ currency, paymentRegion: result.details?.paymentRegion || result.worker.paymentRegion || (currency === 'NGN' ? 'Nigeria' : 'Europe'), accountHolderName: result.details?.accountHolderName || '', bankName: result.details?.bankName || '' });
        this.loading.set(false);
      },
      error: (error: Error) => { this.toast.error(error.message); this.loading.set(false); }
    });
  }

  savePaymentDetails(): void {
    this.paymentForm.markAllAsTouched(); const value = this.paymentForm.getRawValue();
    const regionalInvalid = value.currency === 'NGN' ? !value.bankName || !/^\d{10}$/.test(value.bankAccountNumber) : !value.iban;
    if (this.paymentForm.invalid || regionalInvalid || this.saving() || !this.worker()) { this.toast.error('Complete the required payment fields.'); return; }
    this.saving.set(true);
    this.api.savePaymentDetails(this.worker()!.id, value as PaymentDetailsInput, Boolean(this.details()?.paymentDetailsVerified)).subscribe({
      next: details => { this.details.set(details); this.worker.update(worker => worker ? { ...worker, paymentDetailsVerified: true, preferredCurrency: details.currency, paymentRegion: details.paymentRegion } : worker); this.editingPayment.set(false); this.saving.set(false); this.paymentForm.patchValue({ bankAccountNumber: '', iban: '' }); this.toast.success('Payment details verified and secured.'); },
      error: (error: Error) => { this.toast.error(error.message); this.saving.set(false); }
    });
  }
  currencyChanged(): void { const currency = this.paymentForm.controls.currency.value; this.paymentForm.patchValue({ paymentRegion: currency === 'NGN' ? 'Nigeria' : 'Europe', bankAccountNumber: '', iban: '' }); }
  toggleStatus(worker: Worker): void { const request = worker.status === 'ACTIVE' ? this.api.deactivateWorker(worker.id) : this.api.activateWorker(worker.id); request.subscribe({ next: updated => { this.worker.set(updated); this.toast.success(`Worker ${updated.status === 'ACTIVE' ? 'activated' : 'deactivated'}.`); }, error: (error: Error) => this.toast.error(error.message) }); }
  initials(name: string): string { return name.split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase(); }
}
