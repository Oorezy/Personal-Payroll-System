package com.introtech.introtechservice.entity;

import com.introtech.introtechservice.common.AuditableEntity;
import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.PaymentAccountStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaymentAccount extends AuditableEntity {

    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(name = "payment_region",/* nullable = false,*/ length = 50)
    private String paymentRegion;

    @Column(name = "provider_name", /*nullable = false,*/ length = 80)
    private String providerName;

    @Column(name = "account_holder_name", length = 150)
    private String accountHolderName;

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(name = "masked_account_number", length = 50)
    private String maskedAccountNumber;

    @Column(name = "masked_iban", length = 80)
    private String maskedIban;

    @Column(name = "provider_customer_id", length = 150)
    private String providerCustomerId;

    @Column(name = "provider_account_id", length = 150)
    private String providerAccountId;

    @Column(name = "provider_authorization_id", length = 150)
    private String providerAuthorizationId;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "default_account", nullable = false)
    private boolean defaultAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentAccountStatus status;


}