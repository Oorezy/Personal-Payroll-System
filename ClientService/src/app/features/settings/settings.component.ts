import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreatePaymentAccountInput, CurrencyCode, PaymentAccount } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { PayrollApiService } from '../../core/services/payroll-api.service';
import { ToastService } from '../../core/services/toast.service';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { StatusPillComponent } from '../../shared/status-pill/status-pill.component';

@Component({
  selector: 'app-settings',
  imports: [DatePipe, ReactiveFormsModule, EmptyStateComponent, StatusPillComponent],
  template: `
    <div class="page">
      <header class="page-header"><div><p class="eyebrow">Workspace preferences</p><h1>Settings</h1><p>Manage your profile, payment accounts, provider readiness and security.</p></div></header>
      <div class="settings-grid">
        <nav class="settings-nav card" aria-label="Settings sections">
          <button [class.active]="section() === 'profile'" (click)="section.set('profile')"><span>♙</span><div><strong>Personal profile</strong><small>Your employer details</small></div></button>
          <button [class.active]="section() === 'accounts'" (click)="section.set('accounts')"><span>▤</span><div><strong>Payment accounts</strong><small>Your funding accounts</small></div></button>
          <button [class.active]="section() === 'providers'" (click)="section.set('providers')"><span>↗</span><div><strong>Payment providers</strong><small>Transfer connections</small></div></button>
          <button [class.active]="section() === 'security'" (click)="section.set('security')"><span>▣</span><div><strong>Security</strong><small>Account protection</small></div></button>
        </nav>
        <section class="settings-content">
          @if (section() === 'profile') {
            <article class="card profile-settings"><div class="card-header"><div><h2>Personal profile</h2><p class="muted">Used to personalise your payroll workspace</p></div></div><form class="card-body" [formGroup]="profileForm" (ngSubmit)="saveProfile()"><div class="profile-banner"><span class="large-avatar">{{ initials() }}</span><div><strong>{{ displayName() }}</strong><small>{{ auth.profile()?.email }}</small></div><span class="member-since">Member since {{ auth.profile()?.createdAt | date:'MMM y' }}</span></div><div class="form-grid"><div class="field"><label for="firstName">First name</label><input id="firstName" formControlName="firstName"></div><div class="field"><label for="lastName">Last name</label><input id="lastName" formControlName="lastName"></div><div class="field field-span"><label for="profileEmail">Email address</label><input id="profileEmail" [value]="auth.profile()?.email || ''" disabled><span class="field-hint">Contact support if you need to change your sign-in email.</span></div><div class="field field-span"><label for="profilePhone">Phone number</label><input id="profilePhone" formControlName="phoneNumber" placeholder="+44 7700 900000"></div></div><div class="form-actions"><button class="btn btn-primary" [disabled]="saving()" type="submit">@if (saving()) { <span class="spinner"></span> Saving… } @else { Save profile }</button></div></form></article>
          }
          @if (section() === 'accounts') {
            <article class="card accounts-card">
              <div class="card-header">
                <div><h2>Payment accounts</h2><p class="muted">Accounts used to fund payroll transfers in each currency</p></div>
                <button class="btn btn-primary" (click)="openAccountPanel()">Add account ＋</button>
              </div>
              <div class="account-info">
                <span>▣</span>
                <p><strong>Your account details are protected</strong><small>IntroTech stores masked details after validation. A default verified account is required for each payment currency.</small></p>
              </div>
              @if (loadingAccounts()) {
                <div class="account-loading">@for (item of [1, 2]; track item) { <div class="skeleton"></div> }</div>
              } @else if (paymentAccounts().length) {
                <div class="account-list">
                  @for (account of paymentAccounts(); track account.id) {
                    <article class="payment-account" [class.default]="account.defaultAccount">
                      <div class="account-top">
                        <span class="currency-mark" [class.ngn]="account.currency === 'NGN'">{{ account.currency === 'EUR' ? '€' : '₦' }}</span>
                        <div class="account-title"><div><h3>{{ account.accountHolderName }}</h3>@if (account.defaultAccount) { <span class="default-pill">Default</span> }</div><p>{{ account.bankName || 'European bank account' }} · {{ account.currency }}</p></div>
                        <app-status-pill [status]="account.status" />
                      </div>
                      <div class="account-number"><span>{{ account.currency === 'NGN' ? 'Account number' : 'IBAN' }}</span><strong>{{ account.maskedAccountNumber || account.maskedIban }}</strong></div>
                      <div class="account-meta">
                        <span><small>Region</small><strong>{{ account.paymentRegion }}</strong></span>
                        <span><small>Provider</small><strong>{{ providerLabel(account.providerName) }}</strong></span>
                        <span><small>Verification</small><strong class="verified">{{ account.verified ? '✓ Verified' : 'Pending' }}</strong></span>
                      </div>
                      <div class="account-actions">
                        @if (!account.defaultAccount) { <button class="btn btn-secondary" [disabled]="accountActionId() === account.id" (click)="setDefault(account)">Set as default</button> }
                        <button class="btn btn-ghost remove-account" [disabled]="accountActionId() === account.id" (click)="removeAccount(account)">Remove</button>
                      </div>
                    </article>
                  }
                </div>
              } @else {
                <app-empty-state title="No payment accounts yet" message="Add a EUR or NGN account to fund payroll transfers in that currency." icon="＋" />
                <div class="empty-account-action"><button class="btn btn-primary" (click)="openAccountPanel()">Add payment account</button></div>
              }
            </article>
          }
          @if (section() === 'providers') {
            <article class="card"><div class="card-header"><div><h2>Payment providers</h2><p class="muted">Provider abstraction is ready; live credentials are configured on the server.</p></div><span class="sandbox-pill">Sandbox mode</span></div><div class="provider-list card-body"><div class="provider-row"><span class="provider-logo paystack">P</span><div><strong>NGN transfers</strong><small>Paystack or Flutterwave compatible connection</small></div><span class="provider-status">Mock provider active</span></div><div class="provider-row"><span class="provider-logo euro">€</span><div><strong>EUR transfers</strong><small>Stripe or Wise compatible connection</small></div><span class="provider-status">Mock provider active</span></div><div class="alert alert-info"><strong>Before going live</strong><br>Choose the production providers, add credentials through environment secrets, and complete webhook verification. The frontend never stores provider secret keys.</div></div></article>
          }
          @if (section() === 'security') {
            <article class="card"><div class="card-header"><div><h2>Security</h2><p class="muted">How your workspace and payment data are protected</p></div></div><div class="security-list card-body"><div><span class="security-icon">▣</span><p><strong>JWT-secured access</strong><small>Every payroll API request requires your authenticated access token.</small></p><span class="good">Active</span></div><div><span class="security-icon">◈</span><p><strong>Masked payment details</strong><small>Full account numbers and IBANs are never returned after saving.</small></p><span class="good">Active</span></div><div><span class="security-icon">↻</span><p><strong>Idempotent transfers</strong><small>Unique payment references protect against duplicate execution.</small></p><span class="good">Active</span></div><div class="danger-zone"><p><strong>Sign out of this device</strong><small>Remove locally stored access and refresh tokens.</small></p><button class="btn btn-secondary" (click)="auth.logout()">Sign out</button></div></div></article>
          }
        </section>
      </div>
    </div>

    @if (accountPanelOpen()) {
      <button class="panel-backdrop" aria-label="Close payment account form" (click)="closeAccountPanel()"></button>
      <aside class="side-panel" role="dialog" aria-modal="true" aria-label="Add payment account">
        <div class="panel-header"><div><p class="eyebrow">Funding source</p><h2>Add a payment account</h2><p>Use an account that can fund payroll in the selected currency.</p></div><button class="icon-btn" aria-label="Close" (click)="closeAccountPanel()">×</button></div>
        <form id="payment-account-form" class="panel-content account-form" [formGroup]="accountForm" (ngSubmit)="savePaymentAccount()">
          <div class="alert alert-info">The current backend uses its mock provider. Your account will be locally validated, masked, and marked verified for sandbox testing.</div>
          <div class="form-grid">
            <div class="field"><label for="accountCurrency">Currency *</label><select id="accountCurrency" formControlName="currency" (change)="accountCurrencyChanged()"><option value="EUR">EUR — Euro</option><option value="NGN">NGN — Nigerian Naira</option></select></div>
            <div class="field"><label for="accountRegion">Payment region *</label><select id="accountRegion" formControlName="paymentRegion"><option value="Europe">Europe</option><option value="Nigeria">Nigeria</option></select></div>
            <div class="field field-span"><label for="accountHolderName">Account holder name *</label><input id="accountHolderName" formControlName="accountHolderName" autocomplete="name" placeholder="Name exactly as shown on the account"></div>
            @if (accountForm.controls.currency.value === 'NGN') {
              <div class="field"><label for="fundingBank">Bank name *</label><input id="fundingBank" formControlName="bankName" placeholder="e.g. Guaranty Trust Bank"></div>
              <div class="field"><label for="fundingAccountNumber">10-digit account number *</label><input id="fundingAccountNumber" inputmode="numeric" maxlength="10" autocomplete="off" formControlName="bankAccountNumber" placeholder="0123456789"></div>
            } @else {
              <div class="field field-span"><label for="fundingIban">IBAN *</label><input id="fundingIban" autocomplete="off" formControlName="iban" placeholder="e.g. DE89 3704 0044 0532 0130 00"><span class="field-hint">Spaces are accepted and removed during validation.</span></div>
            }
            <label class="default-check field-span"><input type="checkbox" formControlName="defaultAccount"><span><strong>Use as my default {{ accountForm.controls.currency.value }} account</strong><small>Payroll in this currency will use this account unless you change the default.</small></span></label>
          </div>
        </form>
        <div class="panel-footer"><button class="btn btn-secondary" type="button" (click)="closeAccountPanel()">Cancel</button><button class="btn btn-primary" type="submit" form="payment-account-form" [disabled]="savingAccount()">@if (savingAccount()) { <span class="spinner"></span> Securing account… } @else { Add payment account }</button></div>
      </aside>
    }
  `,
  styleUrl: './settings.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsComponent {
  readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly toast = inject(ToastService);
  private readonly api = inject(PayrollApiService);

  readonly section = signal<'profile' | 'accounts' | 'providers' | 'security'>('profile');
  readonly saving = signal(false);
  readonly paymentAccounts = signal<PaymentAccount[]>([]);
  readonly loadingAccounts = signal(true);
  readonly accountPanelOpen = signal(false);
  readonly savingAccount = signal(false);
  readonly accountActionId = signal<number | null>(null);

  readonly profileForm = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    phoneNumber: ['', Validators.pattern(/^[0-9+()\-\s]{0,30}$/)]
  });

  readonly accountForm = this.fb.nonNullable.group({
    currency: ['EUR' as CurrencyCode, Validators.required],
    paymentRegion: ['Europe', Validators.required],
    providerName: ['MOCK_PROVIDER'],
    accountHolderName: ['', Validators.required],
    bankName: [''],
    bankAccountNumber: [''],
    iban: [''],
    defaultAccount: [true]
  });

  constructor() {
    effect(() => {
      const profile = this.auth.profile();
      if (profile) {
        this.profileForm.patchValue({
          firstName: profile.firstName,
          lastName: profile.lastName,
          phoneNumber: profile.phoneNumber || ''
        }, { emitEvent: false });
      }
    });
    this.loadPaymentAccounts();
  }

  saveProfile(): void {
    this.profileForm.markAllAsTouched();
    if (this.profileForm.invalid || this.saving()) return;
    this.saving.set(true);
    this.auth.updateProfile(this.profileForm.getRawValue()).subscribe({
      next: () => { this.saving.set(false); this.toast.success('Profile updated.'); },
      error: (error: Error) => { this.saving.set(false); this.toast.error(error.message); }
    });
  }

  loadPaymentAccounts(): void {
    this.loadingAccounts.set(true);
    this.api.paymentAccounts().subscribe({
      next: accounts => { this.paymentAccounts.set(accounts); this.loadingAccounts.set(false); },
      error: (error: Error) => { this.loadingAccounts.set(false); this.toast.error(error.message); }
    });
  }

  openAccountPanel(): void {
    this.accountForm.reset({
      currency: 'EUR', paymentRegion: 'Europe', providerName: 'MOCK_PROVIDER',
      accountHolderName: '', bankName: '', bankAccountNumber: '', iban: '', defaultAccount: true
    });
    this.accountPanelOpen.set(true);
  }

  closeAccountPanel(): void {
    if (!this.savingAccount()) this.accountPanelOpen.set(false);
  }

  accountCurrencyChanged(): void {
    const currency = this.accountForm.controls.currency.value;
    this.accountForm.patchValue({
      paymentRegion: currency === 'NGN' ? 'Nigeria' : 'Europe',
      bankName: '', bankAccountNumber: '', iban: ''
    });
  }

  savePaymentAccount(): void {
    this.accountForm.markAllAsTouched();
    const value = this.accountForm.getRawValue();
    const accountDetailsInvalid = value.currency === 'NGN'
      ? !value.bankName.trim() || !/^\d{10}$/.test(value.bankAccountNumber.replaceAll(' ', ''))
      : !value.iban.trim();

    if (this.accountForm.invalid || accountDetailsInvalid || this.savingAccount()) {
      this.toast.error('Complete the required payment account fields.');
      return;
    }

    this.savingAccount.set(true);
    this.api.createPaymentAccount(value as CreatePaymentAccountInput).subscribe({
      next: account => {
        this.paymentAccounts.update(accounts => [
          account,
          ...accounts.map(item => item.currency === account.currency && account.defaultAccount
            ? { ...item, defaultAccount: false }
            : item)
        ]);
        this.savingAccount.set(false);
        this.accountPanelOpen.set(false);
        this.toast.success(`${account.currency} payment account added.`);
      },
      error: (error: Error) => { this.savingAccount.set(false); this.toast.error(error.message); }
    });
  }

  setDefault(account: PaymentAccount): void {
    this.accountActionId.set(account.id);
    this.api.setDefaultPaymentAccount(account.id).subscribe({
      next: updated => {
        this.paymentAccounts.update(accounts => accounts.map(item => item.id === updated.id
          ? updated
          : item.currency === updated.currency ? { ...item, defaultAccount: false } : item));
        this.accountActionId.set(null);
        this.toast.success(`${updated.currency} default account updated.`);
      },
      error: (error: Error) => { this.accountActionId.set(null); this.toast.error(error.message); }
    });
  }

  removeAccount(account: PaymentAccount): void {
    if (!confirm(`Remove the ${account.currency} payment account ending ${this.accountEnding(account)}?`)) return;
    this.accountActionId.set(account.id);
    this.api.removePaymentAccount(account.id).subscribe({
      next: () => {
        this.paymentAccounts.update(accounts => accounts.filter(item => item.id !== account.id));
        this.accountActionId.set(null);
        this.toast.success('Payment account removed.');
      },
      error: (error: Error) => { this.accountActionId.set(null); this.toast.error(error.message); }
    });
  }

  displayName(): string {
    const profile = this.auth.profile();
    return profile ? `${profile.firstName} ${profile.lastName}` : 'Employer';
  }

  initials(): string {
    const profile = this.auth.profile();
    return profile ? `${profile.firstName[0] ?? ''}${profile.lastName[0] ?? ''}`.toUpperCase() : 'IT';
  }

  providerLabel(provider: string): string {
    return provider.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, letter => letter.toUpperCase());
  }

  accountEnding(account: PaymentAccount): string {
    const masked = account.maskedAccountNumber || account.maskedIban || '';
    return masked.slice(-4);
  }
}
