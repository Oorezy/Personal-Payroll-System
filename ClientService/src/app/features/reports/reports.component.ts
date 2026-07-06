import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { MonthlyPayrollReport } from '../../core/models/api.models';
import { PayrollApiService } from '../../core/services/payroll-api.service';
import { ToastService } from '../../core/services/toast.service';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { StatusPillComponent } from '../../shared/status-pill/status-pill.component';

@Component({
  selector: 'app-reports',
  imports: [CurrencyPipe, DatePipe, EmptyStateComponent, StatusPillComponent],
  template: `
    <div class="page">
      <header class="page-header"><div><p class="eyebrow">Payroll insights</p><h1>Monthly reports</h1><p>Understand what was scheduled, completed and still needs attention.</p></div><div class="page-actions"><input class="month-picker" type="month" [value]="selectedMonth()" (change)="monthChanged($event)" aria-label="Report month"><button class="btn btn-secondary" [disabled]="!report()?.payments?.length" (click)="exportCsv()">Export CSV ↓</button></div></header>
      @if (loading()) { <section class="report-loading"><div class="card skeleton"></div><div class="card skeleton"></div><div class="card skeleton"></div></section> }
      @else if (report(); as data) {
        <section class="report-hero card">
          <div><p class="eyebrow">Report period</p><h2>{{ reportDate() | date:'MMMM y' }}</h2><p>{{ data.paymentCount }} payment records included</p></div>
          <div class="completion"><span>Completion rate</span><strong>{{ completionRate(data) }}%</strong><div><i [style.width.%]="completionRate(data)"></i></div></div>
        </section>
        <section class="report-metrics">
          <article class="card"><span>Scheduled payroll</span><strong>{{ data.scheduledTotals.EUR | currency:'EUR':'symbol-narrow':'1.0-2' }}</strong><small>{{ data.scheduledTotals.NGN | currency:'NGN':'symbol-narrow':'1.0-0' }}</small></article>
          <article class="card paid"><span>Successfully paid</span><strong>{{ data.paidTotals.EUR | currency:'EUR':'symbol-narrow':'1.0-2' }}</strong><small>{{ data.paidTotals.NGN | currency:'NGN':'symbol-narrow':'1.0-0' }}</small></article>
          <article class="card"><span>Payment outcomes</span><div class="outcome-row"><p><strong>{{ data.paidCount }}</strong><small>Paid</small></p><p><strong>{{ data.pendingCount }}</strong><small>Pending</small></p><p><strong>{{ data.failedCount }}</strong><small>Failed</small></p></div></article>
        </section>
        <section class="chart-grid">
          <article class="card currency-chart">
            <div class="card-header"><div><h2>Payroll by currency</h2><p class="muted">Scheduled compared with paid</p></div></div>
            <div class="bar-area">
              @for (currency of currencies; track currency) {
                <div class="bar-group"><div class="bar-label"><strong>{{ currency }}</strong><span>{{ data.paidTotals[currency] | currency:currency:'symbol-narrow':'1.0-2' }} paid</span></div><div class="bars"><i class="scheduled" [style.width.%]="barWidth(data.scheduledTotals[currency], maxTotal(data))"></i><i class="paid-bar" [style.width.%]="barWidth(data.paidTotals[currency], maxTotal(data))"></i></div></div>
              }
              <div class="legend"><span><i class="scheduled-dot"></i>Scheduled</span><span><i class="paid-dot"></i>Paid</span></div>
            </div>
          </article>
          <article class="card report-note"><span class="note-icon">⌁</span><h2>Monthly snapshot</h2><p>{{ insight(data) }}</p><div class="snapshot"><span><b>{{ data.paymentCount }}</b> total payments</span><span><b>{{ data.pendingCount + data.failedCount }}</b> need attention</span></div></article>
        </section>
        <section class="card report-table"><div class="card-header"><div><h2>Payment breakdown</h2><p class="muted">Every payment due in this report period</p></div></div>
          @if (data.payments.length) { <div class="table-wrap"><table><thead><tr><th>Worker</th><th>Due date</th><th>Schedule</th><th>Amount</th><th>Status</th></tr></thead><tbody>@for (payment of data.payments; track payment.id) { <tr><td class="cell-primary">{{ payment.workerName }}</td><td>{{ payment.dueDate | date:'d MMM y' }}</td><td>{{ payment.scheduleName || 'Payroll payment' }}</td><td class="amount">{{ payment.amount | currency:payment.currency:'symbol-narrow':'1.0-2' }}</td><td><app-status-pill [status]="payment.status" /></td></tr> }</tbody></table></div> }
          @else { <app-empty-state title="No payroll activity" message="There are no payment records for this month." icon="⌁" /> }
        </section>
      }
    </div>
  `,
  styleUrl: './reports.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportsComponent implements OnInit {
  private readonly api = inject(PayrollApiService); private readonly toast = inject(ToastService);
  readonly report = signal<MonthlyPayrollReport | null>(null); readonly loading = signal(true); readonly selectedMonth = signal(this.toMonthValue(new Date())); readonly currencies = ['EUR', 'NGN'] as const;
  ngOnInit(): void { this.load(); }
  load(): void { const [year, month] = this.selectedMonth().split('-').map(Number); this.loading.set(true); this.api.monthlyReport(year, month).subscribe({ next: report => { this.report.set(report); this.loading.set(false); }, error: (error: Error) => { this.toast.error(error.message); this.loading.set(false); } }); }
  monthChanged(event: Event): void { this.selectedMonth.set((event.target as HTMLInputElement).value); this.load(); }
  reportDate(): Date { const [year, month] = this.selectedMonth().split('-').map(Number); return new Date(year, month - 1, 1); }
  completionRate(report: MonthlyPayrollReport): number { return report.paymentCount ? Math.round((report.paidCount / report.paymentCount) * 100) : 0; }
  maxTotal(report: MonthlyPayrollReport): number { return Math.max(1, ...this.currencies.map(currency => report.scheduledTotals[currency])); }
  barWidth(value: number, max: number): number { return Math.max(value ? 3 : 0, (value / max) * 100); }
  insight(report: MonthlyPayrollReport): string { if (!report.paymentCount) return 'No payments were due this month. Future schedules will appear when they generate payment records.'; if (report.failedCount) return `${report.failedCount} failed ${report.failedCount === 1 ? 'payment needs' : 'payments need'} attention. Review the failure reason before retrying.`; if (report.pendingCount) return `${report.pendingCount} ${report.pendingCount === 1 ? 'payment is' : 'payments are'} still moving through payroll. Keep an eye on approvals and provider confirmations.`; return 'Every payment due this month has been completed. Your payroll is fully up to date.'; }
  exportCsv(): void { const report = this.report(); if (!report) return; const rows = [['Worker','Schedule','Due date','Currency','Amount','Status'], ...report.payments.map(payment => [payment.workerName, payment.scheduleName || '', payment.dueDate, payment.currency, String(payment.amount), payment.status])]; const csv = rows.map(row => row.map(cell => `"${cell.replaceAll('"','""')}"`).join(',')).join('\n'); const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' })); const link = document.createElement('a'); link.href = url; link.download = `introtech-report-${this.selectedMonth()}.csv`; link.click(); URL.revokeObjectURL(url); this.toast.success('Report exported.'); }
  private toMonthValue(date: Date): string { return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`; }
}
