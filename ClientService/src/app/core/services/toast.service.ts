import { Injectable, signal } from '@angular/core';

export interface ToastMessage { id: number; message: string; tone: 'success' | 'error' | 'info'; }

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 0;
  readonly messages = signal<ToastMessage[]>([]);

  success(message: string): void { this.show(message, 'success'); }
  error(message: string): void { this.show(message, 'error'); }
  info(message: string): void { this.show(message, 'info'); }

  dismiss(id: number): void {
    this.messages.update(messages => messages.filter(message => message.id !== id));
  }

  private show(message: string, tone: ToastMessage['tone']): void {
    const toast = { id: ++this.nextId, message, tone };
    this.messages.update(messages => [...messages, toast]);
    window.setTimeout(() => this.dismiss(toast.id), 4500);
  }
}
