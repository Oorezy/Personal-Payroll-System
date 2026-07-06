import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  template: `
    <div class="empty">
      <div class="empty-icon">{{ icon() }}</div>
      <h3>{{ title() }}</h3>
      <p>{{ message() }}</p>
    </div>
  `,
  styles: [`
    .empty { display: grid; min-height: 270px; place-items: center; align-content: center; padding: 35px; text-align: center; }
    .empty-icon { display: grid; width: 54px; height: 54px; place-items: center; margin-bottom: 15px; color: var(--brand-700); background: var(--brand-100); border-radius: 16px; font-size: 1.25rem; }
    h3 { margin: 0 0 7px; font-size: 1rem; } p { max-width: 360px; margin: 0; color: var(--ink-600); font-size: .85rem; line-height: 1.55; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly icon = input('◇');
}
