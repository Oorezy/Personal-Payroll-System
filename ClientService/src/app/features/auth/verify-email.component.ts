import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AuthFrameComponent } from './auth-frame.component';

@Component({
  selector: 'app-verify-email',
  imports: [ReactiveFormsModule, RouterLink, AuthFrameComponent],
  template: `
    <app-auth-frame eyebrow="Email verification" title="Enter the code we sent you" copy="Check your inbox for the six-digit IntroTech code. Once verified, we will take you straight to your dashboard.">
      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        @if (notice()) { <div class="form-notice" role="status">{{ notice() }}</div> }
        @if (error()) { <div class="form-error" role="alert">{{ error() }}</div> }

        <div class="field">
          <label for="email">Email address</label>
          <input id="email" type="email" formControlName="email" autocomplete="email" placeholder="you@example.com">
          @if (form.controls.email.touched && form.controls.email.invalid) { <span class="field-error">Enter the email you registered with.</span> }
        </div>

        <div class="field">
          <label for="otp">Verification code</label>
          <input id="otp" class="otp-input" inputmode="numeric" maxlength="6" formControlName="otp" autocomplete="one-time-code" placeholder="000000">
          @if (form.controls.otp.touched && form.controls.otp.invalid) { <span class="field-error">Enter the six-digit code from your email.</span> }
        </div>

        <button class="btn btn-primary submit" type="submit" [disabled]="submitting()">
          @if (submitting()) { <span class="spinner"></span> Verifying... } @else { Verify and continue <span>-></span> }
        </button>

        <button class="text-button resend" type="button" [disabled]="resending()" (click)="resendCode()">
          @if (resending()) { Sending a new code... } @else { Send a new code }
        </button>
        <p class="switch">Already verified? <a routerLink="/login">Sign in</a></p>
      </form>
    </app-auth-frame>
  `,
  styles: [`
    form { display: grid; gap: 19px; }.form-error, .form-notice { padding: 12px 14px; border-radius: 10px; font-size: .82rem; }.form-error { color: var(--danger); background: var(--danger-soft); }.form-notice { color: var(--brand-700); background: color-mix(in srgb, var(--brand-100) 76%, white); }
    .otp-input { text-align: center; letter-spacing: .34em; font-size: 1.2rem; font-weight: 900; }.submit { width: 100%; min-height: 48px; margin-top: 2px; }.resend { justify-self: center; }.switch { margin: 0; color: var(--ink-600); font-size: .8rem; line-height: 1.45; text-align: center; }.switch a { color: var(--brand-700); font-weight: 800; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerifyEmailComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  readonly submitting = signal(false);
  readonly resending = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  constructor() {
    const email = this.route.snapshot.queryParamMap.get('email') || '';
    if (email) this.form.controls.email.setValue(email);
    if (this.route.snapshot.queryParamMap.get('registered') === 'true') {
      this.notice.set('Account created. We sent a verification code to your email.');
    } else if (this.route.snapshot.queryParamMap.get('reason') === 'login') {
      this.notice.set('Please verify your email before signing in.');
    }
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.submitting()) return;
    this.error.set('');
    this.submitting.set(true);
    this.auth.verifyEmail(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigate(['/dashboard']),
      error: (error: Error) => { this.error.set(error.message); this.submitting.set(false); }
    });
  }

  resendCode(): void {
    this.form.controls.email.markAsTouched();
    if (this.form.controls.email.invalid || this.resending()) return;
    this.error.set('');
    this.resending.set(true);
    this.auth.resendVerificationEmail(this.form.controls.email.value).subscribe({
      next: () => {
        this.notice.set('We sent a fresh verification code to your email.');
        this.resending.set(false);
      },
      error: (error: Error) => { this.error.set(error.message); this.resending.set(false); }
    });
  }
}
