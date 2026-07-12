import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-auth-frame',
  imports: [RouterLink],
  template: `
    <main class="auth-page">
      <section class="story">
        <a class="brand" routerLink="/login"><span class="brand-mark"><i></i><i></i><i></i></span>IntroTech</a>
        <div class="story-copy">
          <p class="eyebrow">Personal payroll, simplified</p>
          <h1>Pay people on time.<br><em>Keep your peace of mind.</em></h1>
          <p>One calm workspace for workers, schedules and payments across Europe and Nigeria.</p>
          <div class="mini-dashboard">
            <div class="mini-top"><span>Next payroll</span><strong>Ready to review</strong></div>
            <div class="mini-total"><small>Due this week</small><strong>€3,840.00</strong></div>
            <div class="mini-row"><span class="mini-avatar">AM</span><div><b>Amara Mensah</b><small>Monthly household payroll</small></div><strong>€1,200</strong></div>
            <div class="mini-row"><span class="mini-avatar gold">TK</span><div><b>Tunde Kareem</b><small>Weekly services</small></div><strong>₦185,000</strong></div>
          </div>
        </div>
        <div class="trust"><span>✓ Secure by design</span><span>✓ EUR & NGN ready</span><span>✓ You stay in control</span></div>
        <span class="orb orb-one"></span><span class="orb orb-two"></span>
      </section>
      <section class="form-side">
        <div class="form-card">
          <p class="eyebrow">{{ eyebrow() }}</p>
          <h2>{{ title() }}</h2>
          <p class="intro">{{ copy() }}</p>
          <ng-content />
        </div>
        <p class="legal">Protected with encrypted authentication and secure payment practices.</p>
      </section>
    </main>
  `,
  styleUrl: './auth-frame.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuthFrameComponent {
  readonly eyebrow = input.required<string>();
  readonly title = input.required<string>();
  readonly copy = input.required<string>();
}
