package com.introtech.introtechservice.entity;

import com.introtech.introtechservice.common.AuditableEntity;
import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.WorkerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workers")
public class Worker extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 150)
    private String email;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 10)
    private Currency currency;

    @Column(name = "payment_region", length = 50)
    private String paymentRegion;

    // Payment details will be managed later through separate endpoints.
    @Column(name = "account_holder_name", length = 150)
    private String accountHolderName;

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(name = "bank_account_number_masked", length = 50)
    private String bankAccountNumberMasked;

    @Column(name = "iban_masked", length = 80)
    private String ibanMasked;

    @Column(name = "provider_recipient_id", length = 150)
    private String providerRecipientId;

    @Column(name = "payment_details_verified", nullable = false)
    private boolean paymentDetailsVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkerStatus status;
}