import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-status-pill',
  template: `<span class="pill" [class]="'pill ' + tone()"><i></i>{{ label() }}</span>`,
  styles: [`
    .pill { display: inline-flex; align-items: center; gap: 7px; padding: 5px 9px; color: var(--ink-600); background: var(--surface-warm); border-radius: 999px; font-size: .69rem; font-weight: 850; letter-spacing: .035em; white-space: nowrap; }
    i { width: 6px; height: 6px; background: currentColor; border-radius: 50%; }
    .success { color: var(--success); background: var(--success-soft); }
    .warning { color: var(--warning); background: var(--warning-soft); }
    .danger { color: var(--danger); background: var(--danger-soft); }
    .info { color: var(--info); background: var(--info-soft); }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatusPillComponent {
  readonly status = input.required<string>();
  readonly label = computed(() => this.status().replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, letter => letter.toUpperCase()));
  readonly tone = computed(() => {
    const status = this.status();
    if (['ACTIVE', 'PAID', 'COMPLETED'].includes(status)) return 'success';
    if (['FAILED', 'CANCELLED', 'REVERSED', 'ARCHIVED'].includes(status)) return 'danger';
    if (['PROCESSING', 'SCHEDULED', 'DUE'].includes(status)) return 'info';
    if (['AWAITING_APPROVAL', 'OVERDUE', 'PAUSED', 'INACTIVE'].includes(status)) return 'warning';
    return 'neutral';
  });
}
