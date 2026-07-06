import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardSummary, PaymentRecord } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { PayrollApiService } from '../../core/services/payroll-api.service';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { StatusPillComponent } from '../../shared/status-pill/status-pill.component';

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DatePipe, RouterLink, EmptyStateComponent, StatusPillComponent],
  template: `
    <div class="page">
      <header class="page-header hero-header">
        <div>
          <p class="eyebrow">{{ today | date:'EEEE, d MMMM' }}</p>
          <h1>{{ greeting() }}, {{ auth.profile()?.firstName || 'there' }}.</h1>
          <p>Here is what needs your attention across payroll today.</p>
        </div>
        <div class="page-actions">
          <a class="btn btn-secondary" routerLink="/workers">Add worker</a>
          <a class="btn btn-primary" routerLink="/schedules">New schedule <span>＋</span></a>
        </div>
      </header>

      @if (loading()) {
        <section class="metrics-grid">
          @for (item of [1,2,3,4]; track item) { <div class="metric-card card"><div class="skeleton metric-skeleton"></div><div class="skeleton value-skeleton"></div></div> }
        </section>
      } @else if (summary(); as data) {
        <section class="metrics-grid">
          <article class="metric-card card accent-card">
            <div class="metric-top"><span class="metric-icon">✓</span><span class="trend">This month</span></div>
            <p>Payroll paid</p>
            <div class="split-value"><strong>{{ data.paidThisMonth.EUR | currency:'EUR':'symbol-narrow':'1.0-2' }}</strong><small>{{ data.paidThisMonth.NGN | currency:'NGN':'symbol-narrow':'1.0-0' }}</small></div>
          </article>
          <article class="metric-card card">
            <div class="metric-top"><span class="metric-icon green">♙</span><span class="trend neutral">{{ data.activeWorkers }} active</span></div>
            <p>Total workers</p><strong>{{ data.totalWorkers }}</strong><small>People in your payroll directory</small>
          </article>
          <article class="metric-card card">
            <div class="metric-top"><span class="metric-icon blue">□</span><span class="trend neutral">{{ data.activeSchedules }} active</span></div>
            <p>Payment schedules</p><strong>{{ data.totalSchedules }}</strong><small>Recurring and one-time plans</small>
          </article>
          <article class="metric-card card" [class.needs-attention]="data.awaitingApprovalPayments > 0">
            <div class="metric-top"><span class="metric-icon amber">!</span><span class="trend warning">Needs review</span></div>
            <p>Awaiting approval</p><strong>{{ data.awaitingApprovalPayments }}</strong><small>{{ data.failedPayments }} failed · {{ data.processingPayments }} processing</small>
          </article>
        </section>

        @if (data.awaitingApprovalPayments > 0) {
          <section class="attention-banner">
            <div class="attention-icon">!</div>
            <div><strong>{{ data.awaitingApprovalPayments }} {{ data.awaitingApprovalPayments === 1 ? 'payment is' : 'payments are' }} waiting for approval</strong><p>Review each payment before funds are sent.</p></div>
            <a class="btn btn-secondary" routerLink="/payments">Review payments →</a>
          </section>
        }

        <section class="dashboard-grid">
          <article class="card upcoming-card">
            <div class="card-header"><div><h2>Upcoming payroll</h2><p class="muted">Payments that are due or in progress</p></div><a class="text-button" routerLink="/payments">View all</a></div>
            @if (data.upcomingPayments.length) {
              <div class="payment-list">
                @for (payment of data.upcomingPayments; track payment.id) {
                  <a class="payment-item" routerLink="/payments">
                    <div class="date-tile"><strong>{{ payment.dueDate | date:'dd' }}</strong><span>{{ payment.dueDate | date:'MMM' }}</span></div>
                    <div class="payment-copy"><strong>{{ payment.workerName }}</strong><span>{{ payment.scheduleName || 'Payroll payment' }}</span></div>
                    <div class="payment-value"><strong>{{ payment.amount | currency:payment.currency:'symbol-narrow':'1.0-2' }}</strong><app-status-pill [status]="payment.status" /></div>
                  </a>
                }
              </div>
            } @else { <app-empty-state title="Nothing due just yet" message="Create a payment schedule and upcoming payroll will appear here." icon="□" /> }
          </article>

          <aside class="side-column">
            <article class="card quick-card">
              <div class="card-header"><h2>Quick actions</h2></div>
              <div class="quick-list">
                <a routerLink="/workers"><span class="quick-icon">＋</span><div><strong>Add a worker</strong><small>Create their payroll profile</small></div><b>›</b></a>
                <a routerLink="/schedules"><span class="quick-icon gold">□</span><div><strong>Schedule a payment</strong><small>Set an amount and due date</small></div><b>›</b></a>
                <a routerLink="/reports"><span class="quick-icon blue">⌁</span><div><strong>View monthly report</strong><small>Review payroll totals</small></div><b>›</b></a>
              </div>
            </article>
            <article class="card health-card">
              <div class="health-top"><span>Workspace health</span><strong>{{ healthScore(data) }}%</strong></div>
              <div class="health-bar"><i [style.width.%]="healthScore(data)"></i></div>
              <p>{{ healthMessage(data) }}</p>
            </article>
          </aside>
        </section>

        <section class="card recent-card desktop-only">
          <div class="card-header"><div><h2>Recent activity</h2><p class="muted">Latest changes across your payment records</p></div></div>
          @if (data.recentPayments.length) {
            <div class="table-wrap"><table><thead><tr><th>Worker</th><th>Schedule</th><th>Due date</th><th>Amount</th><th>Status</th></tr></thead><tbody>
              @for (payment of data.recentPayments; track payment.id) {
                <tr><td class="cell-primary">{{ payment.workerName }}</td><td>{{ payment.scheduleName || 'Payroll payment' }}</td><td>{{ payment.dueDate | date:'d MMM y' }}</td><td class="amount">{{ payment.amount | currency:payment.currency:'symbol-narrow':'1.0-2' }}</td><td><app-status-pill [status]="payment.status" /></td></tr>
              }
            </tbody></table></div>
          } @else { <app-empty-state title="No activity yet" message="Your latest payment activity will appear here." /> }
        </section>
      } @else {
        <section class="card error-card"><h2>We could not load your dashboard</h2><p>{{ error() }}</p><button class="btn btn-primary" (click)="load()">Try again</button></section>
      }
    </div>
  `,
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(PayrollApiService);
  readonly summary = signal<DashboardSummary | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly today = new Date();

  ngOnInit(): void { this.load(); }
  load(): void {
    this.loading.set(true); this.error.set('');
    this.api.dashboard().subscribe({
      next: data => { this.summary.set(data); this.loading.set(false); },
      error: (error: Error) => { this.error.set(error.message); this.loading.set(false); }
    });
  }
  greeting(): string { const hour = new Date().getHours(); return hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'; }
  healthScore(data: DashboardSummary): number {
    if (!data.totalWorkers) return 70;
    const issuePenalty = Math.min(30, (data.failedPayments * 10) + (data.awaitingApprovalPayments * 2));
    return Math.max(65, 100 - issuePenalty);
  }
  healthMessage(data: DashboardSummary): string { return data.failedPayments ? 'Resolve failed payments to get your workspace back in shape.' : 'Your payroll workspace is looking healthy.'; }
}
