import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  template: `
    <div class="toast-stack" aria-live="polite">
      @for (toast of toastService.messages(); track toast.id) {
        <button class="toast" [class]="'toast ' + toast.tone" (click)="toastService.dismiss(toast.id)">
          <span class="toast-mark">{{ toast.tone === 'success' ? '✓' : toast.tone === 'error' ? '!' : 'i' }}</span>
          <span>{{ toast.message }}</span>
          <span class="toast-close">×</span>
        </button>
      }
    </div>
  `,
  styles: [`
    .toast-stack { position: fixed; right: 22px; bottom: 22px; z-index: 200; display: grid; width: min(390px, calc(100vw - 28px)); gap: 10px; }
    .toast { display: grid; grid-template-columns: 25px 1fr auto; align-items: center; gap: 10px; padding: 13px 14px; color: var(--ink-950); background: white; border: 1px solid var(--line); border-radius: 13px; box-shadow: var(--shadow-lg); text-align: left; animation: toast-in .25s ease; }
    .toast-mark { display: grid; width: 24px; height: 24px; place-items: center; color: white; background: var(--info); border-radius: 50%; font-size: .75rem; font-weight: 900; }
    .success .toast-mark { background: var(--success); } .error .toast-mark { background: var(--danger); }
    .toast-close { color: var(--ink-500); font-size: 1.2rem; }
    @keyframes toast-in { from { opacity: 0; transform: translateY(8px); } }
    @media (max-width: 600px) { .toast-stack { right: 14px; bottom: 14px; } }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToastComponent {
  readonly toastService = inject(ToastService);
}
