import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AuthFrameComponent } from './auth-frame.component';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, AuthFrameComponent],
  template: `
    <app-auth-frame eyebrow="Create your workspace" title="Start managing payroll with confidence" copy="Set up your employer account. You can add workers and payment details next.">
      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        @if (error()) { <div class="form-error" role="alert">{{ error() }}</div> }
        <div class="form-grid compact">
          <div class="field"><label for="firstName">First name</label><input id="firstName" formControlName="firstName" autocomplete="given-name" placeholder="Ada"></div>
          <div class="field"><label for="lastName">Last name</label><input id="lastName" formControlName="lastName" autocomplete="family-name" placeholder="Okafor"></div>
          <div class="field field-span"><label for="email">Email address</label><input id="email" type="email" formControlName="email" autocomplete="email" placeholder="ada@example.com"></div>
          <div class="field field-span"><label for="phone">Phone number <span>(optional)</span></label><input id="phone" type="tel" formControlName="phoneNumber" autocomplete="tel" placeholder="447700900123"><small class="field-hint">Digits only, including your country code.</small></div>
          <div class="field field-span"><label for="password">Create password</label><div class="password-wrap"><input id="password" [type]="showPassword() ? 'text' : 'password'" formControlName="password" autocomplete="new-password" placeholder="At least 8 characters"><button type="button" (click)="showPassword.update(value => !value)">{{ showPassword() ? 'Hide' : 'Show' }}</button></div></div>
          <div class="field field-span"><label for="confirmPassword">Confirm password</label><input id="confirmPassword" type="password" formControlName="confirmPassword" autocomplete="new-password" placeholder="Repeat your password"></div>
        </div>
        @if (form.touched && form.invalid) { <p class="validation">Complete all required fields with valid information.</p> }
        <label class="terms"><input type="checkbox" formControlName="terms"><span>I agree to the <a href="#">Terms of Service</a> and <a href="#">Privacy Policy</a>.</span></label>
        <button class="btn btn-primary submit" type="submit" [disabled]="submitting()">
          @if (submitting()) { <span class="spinner"></span> Creating workspace… } @else { Create account <span>→</span> }
        </button>
        <p class="switch">Already have an account? <a routerLink="/auth/login">Sign in</a></p>
      </form>
    </app-auth-frame>
  `,
  styles: [`
    form { display: grid; gap: 18px; }.compact { gap: 14px; }.field label span { color: var(--ink-500); font-weight: 500; }.form-error { padding: 12px 14px; color: var(--danger); background: var(--danger-soft); border-radius: 10px; font-size: .82rem; }.validation { margin: -5px 0 0; color: var(--danger); font-size: .76rem; }
    .password-wrap { position: relative; }.password-wrap input { padding-right: 62px; }.password-wrap button { position: absolute; right: 7px; top: 7px; height: 30px; padding: 0 8px; color: var(--brand-700); background: none; border: 0; font-size: .72rem; font-weight: 750; }
    .terms { display: flex; align-items: flex-start; gap: 9px; color: var(--ink-600); font-size: .76rem; line-height: 1.45; }.terms input { width: 16px; min-height: 16px; margin-top: 2px; accent-color: var(--brand-700); }.terms a, .switch a { color: var(--brand-700); font-weight: 800; }.submit { width: 100%; min-height: 48px; }.switch { margin: 0; color: var(--ink-600); font-size: .82rem; text-align: center; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly submitting = signal(false);
  readonly showPassword = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', Validators.pattern(/^\d{0,15}$/)],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required],
    terms: [false, Validators.requiredTrue]
  });

  submit(): void {
    this.form.markAllAsTouched();
    const value = this.form.getRawValue();
    if (this.form.invalid || value.password !== value.confirmPassword || this.submitting()) {
      if (value.password !== value.confirmPassword) this.error.set('The passwords do not match.');
      return;
    }
    this.error.set('');
    this.submitting.set(true);
    const { confirmPassword: _, terms: __, ...request } = value;
    this.auth.register(request).subscribe({
      next: () => void this.router.navigate(['/auth/login'], { queryParams: { registered: 'true' } }),
      error: (error: Error) => { this.error.set(error.message); this.submitting.set(false); }
    });
  }
}
