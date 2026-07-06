import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreatePaymentAccountInput, CreateScheduleInput, DashboardSummary, MonthlyPayrollReport,
  PaymentAccount, PaymentDetails, PaymentDetailsInput, PaymentRecord, PaymentSchedule, PaymentStatus,
  UpdateScheduleInput, Worker, WorkerInput
} from '../models/api.models';
import { ApiClientService } from './api-client.service';

@Injectable({ providedIn: 'root' })
export class PayrollApiService {
  constructor(private readonly api: ApiClientService) {}

  dashboard(): Observable<DashboardSummary> { return this.api.get('/dashboard'); }

  workers(): Observable<Worker[]> { return this.api.get('/workers'); }
  worker(id: number): Observable<Worker> { return this.api.get(`/workers/${id}`); }
  createWorker(input: WorkerInput): Observable<Worker> { return this.api.post('/workers', input); }
  updateWorker(id: number, input: Partial<WorkerInput>): Observable<Worker> { return this.api.put(`/workers/${id}`, input); }
  activateWorker(id: number): Observable<Worker> { return this.api.patch(`/workers/${id}/activate`); }
  deactivateWorker(id: number): Observable<Worker> { return this.api.patch(`/workers/${id}/deactivate`); }
  archiveWorker(id: number): Observable<unknown> { return this.api.delete(`/workers/${id}`); }
  paymentDetails(workerId: number): Observable<PaymentDetails> { return this.api.get(`/workers/${workerId}/payment-details`); }
  savePaymentDetails(workerId: number, input: PaymentDetailsInput, updating: boolean): Observable<PaymentDetails> {
    return updating ? this.api.put(`/workers/${workerId}/payment-details`, input) : this.api.post(`/workers/${workerId}/payment-details`, input);
  }

  paymentAccounts(): Observable<PaymentAccount[]> { return this.api.get('/payment-accounts'); }
  createPaymentAccount(input: CreatePaymentAccountInput): Observable<PaymentAccount> { return this.api.post('/payment-accounts', input); }
  setDefaultPaymentAccount(id: number): Observable<PaymentAccount> { return this.api.patch(`/payment-accounts/${id}/set-default`); }
  removePaymentAccount(id: number): Observable<void> { return this.api.delete(`/payment-accounts/${id}`); }

  schedules(): Observable<PaymentSchedule[]> { return this.api.get('/payment-schedules'); }
  createSchedule(input: CreateScheduleInput): Observable<PaymentSchedule> { return this.api.post('/payment-schedules', input); }
  updateSchedule(id: number, input: UpdateScheduleInput): Observable<PaymentSchedule> { return this.api.put(`/payment-schedules/${id}`, input); }
  pauseSchedule(id: number): Observable<PaymentSchedule> { return this.api.patch(`/payment-schedules/${id}/pause`); }
  resumeSchedule(id: number): Observable<PaymentSchedule> { return this.api.patch(`/payment-schedules/${id}/resume`); }
  cancelSchedule(id: number): Observable<void> { return this.api.delete(`/payment-schedules/${id}`); }

  payments(status?: PaymentStatus | ''): Observable<PaymentRecord[]> { return this.api.get('/payments', { status: status || undefined }); }
  approvePayment(id: number, note?: string): Observable<PaymentRecord> { return this.api.post(`/payments/${id}/approve`, { note }); }
  retryPayment(id: number, note?: string): Observable<PaymentRecord> { return this.api.post(`/payments/${id}/retry`, { note }); }
  cancelPayment(id: number): Observable<PaymentRecord> { return this.api.post(`/payments/${id}/cancel`); }
  skipPayment(id: number): Observable<PaymentRecord> { return this.api.post(`/payments/${id}/skip`); }

  monthlyReport(year: number, month: number): Observable<MonthlyPayrollReport> {
    return this.api.get('/reports/monthly', { year, month });
  }
}
