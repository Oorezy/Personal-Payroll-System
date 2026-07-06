import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found', imports: [RouterLink],
  template: `<main class="not-found"><div class="mark">404</div><p class="eyebrow">Page not found</p><h1>This page has wandered off payroll.</h1><p>Let’s take you back to somewhere useful.</p><a class="btn btn-primary" routerLink="/dashboard">Back to dashboard</a></main>`,
  styles: [`.not-found { display: grid; min-height: 100vh; place-items: center; align-content: center; padding: 30px; text-align: center; }.mark { margin-bottom: 20px; color: var(--brand-100); font-size: 8rem; font-weight: 900; letter-spacing: -.08em; }.not-found h1 { max-width: 550px; }.not-found > p:not(.eyebrow) { margin-bottom: 24px; color: var(--ink-600); }`],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotFoundComponent {}
