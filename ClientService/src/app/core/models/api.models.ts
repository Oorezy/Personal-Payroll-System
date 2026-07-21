export type CurrencyCode = 'EUR' | 'NGN';
export type WorkerStatus = 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
export type ScheduleStatus = 'ACTIVE' | 'PAUSED' | 'CANCELLED' | 'COMPLETED';
export type PaymentFrequency = 'ONE_TIME' | 'WEEKLY' | 'BI_WEEKLY' | 'MONTHLY';
export type PaymentMode = 'MANUAL_APPROVAL' | 'AUTOMATIC';
export type PaymentAccountStatus = 'ACTIVE' | 'DISABLED' | 'REMOVED';
export type PaymentStatus =
  | 'SCHEDULED' | 'DUE' | 'AWAITING_APPROVAL' | 'PROCESSING' | 'PAID'
  | 'FAILED' | 'OVERDUE' | 'SKIPPED' | 'CANCELLED' | 'REVERSED';

export interface ApiEnvelope<T> {
  status: boolean;
  message: string;
  data?: T;
}

export interface JwtTokenResponse {
  accessToken: string;
  tokenType: string;
  refreshToken: string;
  expiresIn: number;
}

export interface LoginRequest { email: string; password: string; }
export interface VerifyOtpRequest { email: string; otp: string; }
export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
}

export interface UserProfile {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  role: 'USER' | 'ADMIN';
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Worker {
  id: number;
  fullName: string;
  email?: string;
  phoneNumber?: string;
  jobTitle?: string;
  category?: string;
  address?: string;
  notes?: string;
  preferredCurrency: CurrencyCode;
  paymentRegion?: string;
  paymentDetailsVerified: boolean;
  status: WorkerStatus;
  createdAt: string;
  updatedAt: string;
}

export interface WorkerInput {
  fullName: string;
  email?: string;
  phoneNumber?: string;
  jobTitle?: string;
  category?: string;
  address?: string;
  notes?: string;
  preferredCurrency: CurrencyCode;
  paymentRegion: string;
}

export interface PaymentDetails {
  workerId: number;
  workerName: string;
  currency: CurrencyCode;
  paymentRegion: string;
  accountHolderName: string;
  bankName?: string;
  maskedBankAccountNumber?: string;
  maskedIban?: string;
  paymentDetailsVerified: boolean;
  providerRecipientId?: string;
}

export interface PaymentDetailsInput {
  currency: CurrencyCode;
  paymentRegion: string;
  accountHolderName: string;
  bankName?: string;
  bankAccountNumber?: string;
  iban?: string;
}

export interface PaymentAccount {
  id: number;
  currency: CurrencyCode;
  paymentRegion: string;
  providerName: string;
  accountHolderName: string;
  bankName?: string;
  maskedAccountNumber?: string;
  maskedIban?: string;
  verified: boolean;
  defaultAccount: boolean;
  status: PaymentAccountStatus;
  createdDate: string;
  lastModifiedDate: string;
}

export interface CreatePaymentAccountInput {
  currency: CurrencyCode;
  paymentRegion: string;
  providerName?: string;
  accountHolderName: string;
  bankName?: string;
  bankAccountNumber?: string;
  iban?: string;
  defaultAccount: boolean;
}

export interface PaymentSchedule {
  id: number;
  workerId: number;
  workerName: string;
  scheduleName: string;
  amount: number;
  currency: CurrencyCode;
  frequency: PaymentFrequency;
  paymentMode: PaymentMode;
  startDate: string;
  nextDueDate: string;
  endDate?: string;
  reminderDaysBefore: number;
  status: ScheduleStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateScheduleInput {
  workerId: number;
  scheduleName: string;
  amount: number;
  currency: CurrencyCode;
  frequency: PaymentFrequency;
  paymentMode: PaymentMode;
  startDate: string;
  firstDueDate: string;
  endDate?: string;
  reminderDaysBefore: number;
}

export interface UpdateScheduleInput {
  scheduleName?: string;
  amount?: number;
  frequency?: PaymentFrequency;
  paymentMode?: PaymentMode;
  nextDueDate?: string;
  endDate?: string;
  reminderDaysBefore?: number;
}

export interface PaymentRecord {
  id: number;
  workerId: number;
  workerName: string;
  scheduleId?: number;
  scheduleName?: string;
  amount: number;
  currency: CurrencyCode;
  dueDate: string;
  paidDate?: string;
  status: PaymentStatus;
  paymentMethod?: string;
  providerName?: string;
  providerTransferReference?: string;
  failureReason?: string;
  approvedAt?: string;
  processedAt?: string;
  confirmedAt?: string;
  notes?: string;
  retryCount: number;
  createdDate: string;
  lastModifiedDate: string;
}

export interface DashboardSummary {
  totalWorkers: number;
  activeWorkers: number;
  totalSchedules: number;
  activeSchedules: number;
  awaitingApprovalPayments: number;
  processingPayments: number;
  failedPayments: number;
  paidThisMonth: Record<CurrencyCode, number>;
  upcomingPayments: PaymentRecord[];
  recentPayments: PaymentRecord[];
}

export interface MonthlyPayrollReport {
  year: number;
  month: number;
  paymentCount: number;
  paidCount: number;
  failedCount: number;
  pendingCount: number;
  scheduledTotals: Record<CurrencyCode, number>;
  paidTotals: Record<CurrencyCode, number>;
  payments: PaymentRecord[];
}
