package com.introtech.introtechservice.entity;

import com.introtech.introtechservice.common.AuditableEntity;
import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_records", uniqueConstraints = {
        @UniqueConstraint(name = "unique_schedule_due_date", columnNames = {"schedule_id", "due_date"})})
public class PaymentRecord extends AuditableEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private PaymentSchedule schedule;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.AWAITING_APPROVAL;

    @Column(name = "payment_method", length = 80)
    private String paymentMethod;

    @Column(name = "provider_name", length = 80)
    private String providerName;

    @Column(name = "provider_transaction_id", length = 150)
    private String providerTransactionId;

    @Column(name = "provider_transfer_reference", length = 150)
    private String providerTransferReference;

    @Column(name = "idempotency_key", unique = true, length = 150)
    private String idempotencyKey;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "retry_count")
    private int retryCount;

}
