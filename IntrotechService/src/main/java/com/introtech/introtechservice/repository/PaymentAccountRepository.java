package com.introtech.introtechservice.repository;

import com.introtech.introtechservice.common.BaseRepository;
import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.PaymentAccountStatus;
import com.introtech.introtechservice.entity.PaymentAccount;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentAccountRepository extends BaseRepository<PaymentAccount, Long> {


    List<PaymentAccount> findByOwnerIdAndStatusOrderByCreatedDateDesc(
            Long ownerId,
            PaymentAccountStatus status
    );

    Optional<PaymentAccount> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<PaymentAccount> findByOwnerIdAndCurrencyAndDefaultAccountTrueAndStatus(
            Long ownerId,
            Currency currency,
            PaymentAccountStatus status
    );

    boolean existsByOwnerIdAndCurrencyAndStatus(
            Long ownerId,
            Currency currency,
            PaymentAccountStatus status
    );

    @Modifying
    @Query("""
            UPDATE PaymentAccount p
            SET p.defaultAccount = false
            WHERE p.ownerId = :ownerId
            AND p.currency = :currency
            AND p.status = 'ACTIVE'
            """)
    void clearDefaultForCurrency(
            @Param("ownerId") Long ownerId,
            @Param("currency") Currency currency
    );
}