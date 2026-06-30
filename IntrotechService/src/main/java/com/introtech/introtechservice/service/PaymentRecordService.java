package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.PaymentStatus;
import com.introtech.introtechservice.dto.PaymentRecordResponse;
import com.introtech.introtechservice.entity.PaymentRecord;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.mappers.BaseMapper;
import com.introtech.introtechservice.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentRecordService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final UserContextService userContextService;
    private final BaseMapper<PaymentRecord, PaymentRecordResponse> paymentRecordMapper;


    @Transactional(readOnly = true)
    public List<PaymentRecordResponse> getMyPayments(PaymentStatus status) {
        User currentUser = userContextService.getCurrentUser();

        List<PaymentRecord> payments;

        if (status == null) {
            payments = paymentRecordRepository.findByUserIdOrderByDueDateDesc(
                    currentUser.getId()
            );
        } else {
            payments = paymentRecordRepository.findByUserIdAndStatusOrderByDueDateDesc(
                    currentUser.getId(),
                    status
            );
        }

        return payments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentRecordResponse getPayment(Long paymentId) {
        User currentUser = userContextService.getCurrentUser();

        PaymentRecord paymentRecord = paymentRecordRepository
                .findByIdAndUserId(paymentId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Payment not found"));

        return mapToResponse(paymentRecord);
    }

    private PaymentRecordResponse mapToResponse(PaymentRecord paymentRecord) {
        return paymentRecordMapper.toDto(paymentRecord);
//        return new PaymentRecordResponse(
//                paymentRecord.getId(),
//                paymentRecord.getWorker().getId(),
//                paymentRecord.getWorker().getFullName(),
//                paymentRecord.getSchedule() == null ? null : paymentRecord.getSchedule().getId(),
//                paymentRecord.getSchedule() == null ? null : paymentRecord.getSchedule().getScheduleName(),
//                paymentRecord.getAmount(),
//                paymentRecord.getCurrency(),
//                paymentRecord.getDueDate(),
//                paymentRecord.getPaidDate(),
//                paymentRecord.getStatus(),
//                paymentRecord.getPaymentMethod(),
//                paymentRecord.getProviderName(),
//                paymentRecord.getProviderTransferReference(),
//                paymentRecord.getFailureReason(),
//                paymentRecord.getApprovedAt(),
//                paymentRecord.getProcessedAt(),
//                paymentRecord.getConfirmedAt(),
//                paymentRecord.getNotes(),
//                paymentRecord.getCreatedDate(),
//                paymentRecord.getLastModifiedDate()
//        );
    }
}
