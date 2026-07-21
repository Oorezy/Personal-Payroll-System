import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AuthFrameComponent } from './auth-frame.component';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, AuthFrameComponent],
  template: `
    <app-auth-frame eyebrow="Welcome back" title="Sign in to your workspace" copy="Review upcoming payroll, manage workers and keep every payment on track.">
      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        @if (notice()) { <div class="form-notice" role="status">{{ notice() }}</div> }
        @if (error()) { <div class="form-error" role="alert">{{ error() }}</div> }
        <div class="field">
          <label for="email">Email address</label>
          <input id="email" type="email" formControlName="email" autocomplete="email" placeholder="you@example.com">
          @if (form.controls.email.touched && form.controls.email.invalid) { <span class="field-error">Enter a valid email address.</span> }
        </div>
        <div class="field">
          <div class="label-row"><label for="password">Password</label><button type="button" class="text-button">Forgot password?</button></div>
          <div class="password-wrap"><input id="password" [type]="showPassword() ? 'text' : 'password'" formControlName="password" autocomplete="current-password" placeholder="Your password"><button type="button" (click)="showPassword.update(value => !value)">{{ showPassword() ? 'Hide' : 'Show' }}</button></div>
          @if (form.controls.password.touched && form.controls.password.invalid) { <span class="field-error">Password is required.</span> }
        </div>
        <label class="remember"><input type="checkbox"> <span>Keep me signed in on this device</span></label>
        <button class="btn btn-primary submit" type="submit" [disabled]="submitting()">
          @if (submitting()) { <span class="spinner"></span> Signing in… } @else { Sign in <span>→</span> }
        </button>
        <p class="switch">New to IntroTech? <a routerLink="/register">Create your account</a></p>
      </form>
    </app-auth-frame>
  `,
  styles: [`
    form { display: grid; gap: 19px; }.label-row { display: flex; align-items: center; justify-content: space-between; }.form-error, .form-notice { padding: 12px 14px; border-radius: 10px; font-size: .82rem; }.form-error { color: var(--danger); background: var(--danger-soft); }.form-notice { color: var(--brand-700); background: color-mix(in srgb, var(--brand-100) 76%, white); }
    .password-wrap { position: relative; }.password-wrap input { padding-right: 62px; }.password-wrap button { position: absolute; right: 7px; top: 7px; height: 30px; padding: 0 8px; color: var(--brand-700); background: none; border: 0; font-size: .72rem; font-weight: 750; }
    .remember { display: flex; align-items: center; gap: 8px; color: var(--ink-600); font-size: .78rem; }.remember input { width: 16px; min-height: 16px; accent-color: var(--brand-700); }.submit { width: 100%; min-height: 48px; margin-top: 2px; }.switch { margin: 4px 0 0; color: var(--ink-600); font-size: .82rem; text-align: center; }.switch a { color: var(--brand-700); font-weight: 800; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly submitting = signal(false);
  readonly showPassword = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  constructor() {
    if (this.route.snapshot.queryParamMap.get('verified') === 'true') {
      this.notice.set('Email verified. You can now sign in securely.');
    }
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.submitting()) return;
    this.error.set('');
    this.submitting.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigate(['/dashboard']),
      error: (error: Error) => {
        this.submitting.set(false);
        console.log("error message" + error.message);
        if (error.message.toLowerCase().includes('not verified')) {
          void this.router.navigate(['/verify-email'], { queryParams: { email: this.form.controls.email.value, reason: 'login' } });
          return;
        }
        this.error.set(error.message);
      }
    });
  }
}
