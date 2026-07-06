import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CurrencyCode, Worker, WorkerInput, WorkerStatus } from '../../core/models/api.models';
import { PayrollApiService } from '../../core/services/payroll-api.service';
import { ToastService } from '../../core/services/toast.service';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { StatusPillComponent } from '../../shared/status-pill/status-pill.component';

@Component({
  selector: 'app-workers',
  imports: [ReactiveFormsModule, RouterLink, EmptyStateComponent, StatusPillComponent],
  template: `
    <div class="page">
      <header class="page-header">
        <div><p class="eyebrow">People directory</p><h1>Your workers</h1><p>Keep worker details and payment destinations organised.</p></div>
        <div class="page-actions"><button class="btn btn-primary" (click)="openCreate()">Add worker <span>＋</span></button></div>
      </header>

      <section class="overview-strip card">
        <div><span class="overview-icon">♙</span><p><strong>{{ workers().length }}</strong><small>Total workers</small></p></div>
        <div><span class="overview-icon green">✓</span><p><strong>{{ verifiedCount() }}</strong><small>Payment-ready</small></p></div>
        <div><span class="overview-icon amber">!</span><p><strong>{{ attentionCount() }}</strong><small>Need payment details</small></p></div>
      </section>

      <section class="toolbar">
        <div class="search"><input type="search" placeholder="Search workers…" aria-label="Search workers" (input)="setSearch($event)"></div>
        <select class="filter-select" aria-label="Filter by worker status" (change)="setFilter($event)">
          <option value="ALL">All statuses</option><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option>
        </select>
      </section>

      <section class="card workers-card">
        @if (loading()) {
          <div class="loading-list">@for (item of [1,2,3,4,5]; track item) { <div><span class="skeleton"></span><p><i class="skeleton"></i><i class="skeleton"></i></p></div> }</div>
        } @else if (filteredWorkers().length) {
          <div class="table-wrap"><table>
            <thead><tr><th>Worker</th><th>Role</th><th>Payment region</th><th>Currency</th><th>Payment details</th><th>Status</th><th><span class="sr-only">Actions</span></th></tr></thead>
            <tbody>
              @for (worker of filteredWorkers(); track worker.id) {
                <tr>
                  <td><a class="person-cell" [routerLink]="['/workers', worker.id]"><span class="avatar">{{ initials(worker.fullName) }}</span><span><span class="cell-primary">{{ worker.fullName }}</span><span class="cell-secondary">{{ worker.email || 'No email added' }}</span></span></a></td>
                  <td>{{ worker.jobTitle || worker.category || '—' }}</td><td>{{ worker.paymentRegion || '—' }}</td><td><span class="currency-chip">{{ worker.preferredCurrency }}</span></td>
                  <td><span class="readiness" [class.ready]="worker.paymentDetailsVerified"><i>{{ worker.paymentDetailsVerified ? '✓' : '!' }}</i>{{ worker.paymentDetailsVerified ? 'Verified' : 'Action needed' }}</span></td>
                  <td><app-status-pill [status]="worker.status" /></td>
                  <td><div class="row-actions"><a class="btn btn-ghost" [routerLink]="['/workers', worker.id]">View</a><button class="icon-btn" aria-label="Edit worker" (click)="openEdit(worker)">···</button></div></td>
                </tr>
              }
            </tbody>
          </table></div>
        } @else if (workers().length) {
          <app-empty-state title="No matching workers" message="Try a different name or status filter." icon="⌕" />
        } @else {
          <app-empty-state title="Add your first worker" message="Create a worker profile, then add their verified payment details." icon="＋" />
          <div class="empty-action"><button class="btn btn-primary" (click)="openCreate()">Add worker</button></div>
        }
      </section>
    </div>

    @if (panelOpen()) {
      <button class="panel-backdrop" aria-label="Close worker form" (click)="closePanel()"></button>
      <aside class="side-panel" role="dialog" aria-modal="true" [attr.aria-label]="editing() ? 'Edit worker' : 'Add worker'">
        <div class="panel-header"><div><p class="eyebrow">{{ editing() ? 'Update profile' : 'New worker' }}</p><h2>{{ editing() ? 'Edit worker details' : 'Add someone to payroll' }}</h2><p>Payment details are added securely on the worker profile.</p></div><button class="icon-btn" aria-label="Close" (click)="closePanel()">×</button></div>
        <form id="worker-form" class="panel-content" [formGroup]="form" (ngSubmit)="save()">
          <div class="form-grid">
            <div class="field field-span"><label for="fullName">Full name *</label><input id="fullName" formControlName="fullName" placeholder="e.g. Amara Mensah">@if (invalid('fullName')) { <span class="field-error">Full name is required.</span> }</div>
            <div class="field"><label for="workerEmail">Email</label><input id="workerEmail" type="email" formControlName="email" placeholder="worker@example.com"></div>
            <div class="field"><label for="phoneNumber">Phone number</label><input id="phoneNumber" formControlName="phoneNumber" placeholder="+44 7700 900000"></div>
            <div class="field"><label for="jobTitle">Job title</label><input id="jobTitle" formControlName="jobTitle" placeholder="e.g. Personal assistant"></div>
            <div class="field"><label for="category">Category</label><select id="category" formControlName="category"><option value="">Select category</option><option>Household</option><option>Care</option><option>Professional</option><option>Contractor</option><option>Other</option></select></div>
            <div class="field"><label for="currency">Preferred currency *</label><select id="currency" formControlName="preferredCurrency"><option value="EUR">EUR — Euro</option><option value="NGN">NGN — Nigerian Naira</option></select></div>
            <div class="field"><label for="region">Payment region *</label><select id="region" formControlName="paymentRegion"><option value="Europe">Europe</option><option value="Nigeria">Nigeria</option></select></div>
            <div class="field field-span"><label for="address">Address</label><textarea id="address" formControlName="address" placeholder="Optional worker address"></textarea></div>
            <div class="field field-span"><label for="notes">Notes</label><textarea id="notes" formControlName="notes" placeholder="Private notes about this worker"></textarea></div>
          </div>
          @if (editing() && currencyChanged()) { <div class="alert alert-warning currency-warning">Changing currency clears the worker’s verified payment status. New payment details will be required.</div> }
        </form>
        <div class="panel-footer">
          @if (editing(); as worker) { <button class="btn btn-danger archive" type="button" (click)="archive(worker)">Archive</button> }
          <button class="btn btn-secondary" type="button" (click)="closePanel()">Cancel</button>
          <button class="btn btn-primary" type="submit" form="worker-form" [disabled]="saving()">@if (saving()) { <span class="spinner"></span> Saving… } @else { {{ editing() ? 'Save changes' : 'Add worker' }} }</button>
        </div>
      </aside>
    }
  `,
  styleUrl: './workers.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkersComponent implements OnInit {
  private readonly api = inject(PayrollApiService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  readonly workers = signal<Worker[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly panelOpen = signal(false);
  readonly editing = signal<Worker | null>(null);
  readonly search = signal('');
  readonly filter = signal<'ALL' | WorkerStatus>('ALL');
  readonly verifiedCount = computed(() => this.workers().filter(worker => worker.paymentDetailsVerified).length);
  readonly attentionCount = computed(() => this.workers().filter(worker => !worker.paymentDetailsVerified).length);
  readonly filteredWorkers = computed(() => {
    const query = this.search().trim().toLowerCase();
    return this.workers().filter(worker => (this.filter() === 'ALL' || worker.status === this.filter())
      && (!query || [worker.fullName, worker.email, worker.jobTitle, worker.category].some(value => value?.toLowerCase().includes(query))));
  });
  readonly form = this.fb.nonNullable.group({
    fullName: ['', Validators.required], email: ['', Validators.email], phoneNumber: [''], jobTitle: [''], category: [''],
    address: [''], notes: [''], preferredCurrency: ['EUR' as CurrencyCode, Validators.required], paymentRegion: ['Europe', Validators.required]
  });

  ngOnInit(): void { this.load(); }
  load(): void { this.loading.set(true); this.api.workers().subscribe({ next: workers => { this.workers.set(workers); this.loading.set(false); }, error: (error: Error) => { this.toast.error(error.message); this.loading.set(false); } }); }
  openCreate(): void { this.editing.set(null); this.form.reset({ fullName: '', email: '', phoneNumber: '', jobTitle: '', category: '', address: '', notes: '', preferredCurrency: 'EUR', paymentRegion: 'Europe' }); this.panelOpen.set(true); }
  openEdit(worker: Worker): void { this.editing.set(worker); this.form.reset({ fullName: worker.fullName, email: worker.email || '', phoneNumber: worker.phoneNumber || '', jobTitle: worker.jobTitle || '', category: worker.category || '', address: worker.address || '', notes: worker.notes || '', preferredCurrency: worker.preferredCurrency, paymentRegion: worker.paymentRegion || (worker.preferredCurrency === 'NGN' ? 'Nigeria' : 'Europe') }); this.panelOpen.set(true); }
  closePanel(): void { if (!this.saving()) this.panelOpen.set(false); }
  save(): void {
    this.form.markAllAsTouched(); if (this.form.invalid || this.saving()) return;
    this.saving.set(true); const input = this.form.getRawValue() as WorkerInput; const current = this.editing();
    const request = current ? this.api.updateWorker(current.id, input) : this.api.createWorker(input);
    request.subscribe({ next: worker => { this.workers.update(items => current ? items.map(item => item.id === worker.id ? worker : item) : [worker, ...items]); this.toast.success(current ? 'Worker updated.' : 'Worker added.'); this.saving.set(false); this.panelOpen.set(false); }, error: (error: Error) => { this.toast.error(error.message); this.saving.set(false); } });
  }
  archive(worker: Worker): void { if (!confirm(`Archive ${worker.fullName}? Their historical payments will remain available.`)) return; this.api.archiveWorker(worker.id).subscribe({ next: () => { this.workers.update(items => items.filter(item => item.id !== worker.id)); this.panelOpen.set(false); this.toast.success('Worker archived.'); }, error: (error: Error) => this.toast.error(error.message) }); }
  invalid(control: keyof typeof this.form.controls): boolean { const field = this.form.controls[control]; return field.touched && field.invalid; }
  currencyChanged(): boolean { return Boolean(this.editing() && this.form.controls.preferredCurrency.value !== this.editing()?.preferredCurrency); }
  initials(name: string): string { return name.split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase(); }
  setSearch(event: Event): void { this.search.set((event.target as HTMLInputElement).value); }
  setFilter(event: Event): void { this.filter.set((event.target as HTMLSelectElement).value as 'ALL' | WorkerStatus); }
}
