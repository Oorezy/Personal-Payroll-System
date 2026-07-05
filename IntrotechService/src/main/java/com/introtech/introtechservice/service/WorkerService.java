package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.WorkerStatus;
import com.introtech.introtechservice.dto.CreateWorkerRequest;
import com.introtech.introtechservice.dto.UpdateWorkerRequest;
import com.introtech.introtechservice.dto.WorkerResponse;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.entity.Worker;
import com.introtech.introtechservice.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final UserContextService userContextService;

    @Transactional
    public WorkerResponse createWorker(CreateWorkerRequest request) {
        User currentUser = userContextService.getCurrentUser();

        Worker worker = Worker.builder()
                .user(currentUser)
                .fullName(request.fullName())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .jobTitle(request.jobTitle())
                .category(request.category())
                .address(request.address())
                .notes(request.notes())
                .currency(request.preferredCurrency())
                .paymentRegion(request.paymentRegion())
                .paymentDetailsVerified(false)
                .status(WorkerStatus.ACTIVE)
                .build();

        Worker savedWorker = workerRepository.save(worker);

        return mapToResponse(savedWorker);
    }

    @Transactional(readOnly = true)
    public List<WorkerResponse> getMyWorkers() {
        User currentUser = userContextService.getCurrentUser();

        return workerRepository
                .findByUserIdAndStatusNotOrderByCreatedDateDesc(
                        currentUser.getId(),
                        WorkerStatus.ARCHIVED
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkerResponse getWorker(Long workerId) {
        User currentUser = userContextService.getCurrentUser();

        Worker worker = getWorkerOwnedByCurrentUser(workerId, currentUser.getId());

        return mapToResponse(worker);
    }

    @Transactional
    public WorkerResponse updateWorker(Long workerId, UpdateWorkerRequest request) {
        User currentUser = userContextService.getCurrentUser();

        Worker worker = getWorkerOwnedByCurrentUser(workerId, currentUser.getId());

        if (request.fullName() != null && !request.fullName().isBlank()) {
            worker.setFullName(request.fullName());
        }

        if (request.email() != null) {
            worker.setEmail(request.email());
        }

        if (request.phoneNumber() != null) {
            worker.setPhoneNumber(request.phoneNumber());
        }

        if (request.jobTitle() != null) {
            worker.setJobTitle(request.jobTitle());
        }

        if (request.category() != null) {
            worker.setCategory(request.category());
        }

        if (request.address() != null) {
            worker.setAddress(request.address());
        }

        if (request.notes() != null) {
            worker.setNotes(request.notes());
        }

        if (request.preferredCurrency() != null) {
            Currency previousCurrency = worker.getCurrency();
            worker.setCurrency(request.preferredCurrency());

            if (previousCurrency != request.preferredCurrency()) {
                worker.setPaymentDetailsVerified(false);
                worker.setBankAccountNumberMasked(null);
                worker.setIbanMasked(null);
                worker.setProviderRecipientId(null);
            }
        }

        if (request.paymentRegion() != null) {
            worker.setPaymentRegion(request.paymentRegion());
        }

        Worker updatedWorker = workerRepository.save(worker);

        return mapToResponse(updatedWorker);
    }

    @Transactional
    public WorkerResponse deactivateWorker(Long workerId) {
        User currentUser = userContextService.getCurrentUser();

        Worker worker = getWorkerOwnedByCurrentUser(workerId, currentUser.getId());

        worker.setStatus(WorkerStatus.INACTIVE);

        return mapToResponse(workerRepository.save(worker));
    }

    @Transactional
    public WorkerResponse activateWorker(Long workerId) {
        User currentUser = userContextService.getCurrentUser();

        Worker worker = getWorkerOwnedByCurrentUser(workerId, currentUser.getId());

        worker.setStatus(WorkerStatus.ACTIVE);

        return mapToResponse(workerRepository.save(worker));
    }

    @Transactional
    public void archiveWorker(Long workerId) {
        User currentUser = userContextService.getCurrentUser();

        Worker worker = getWorkerOwnedByCurrentUser(workerId, currentUser.getId());

        worker.setStatus(WorkerStatus.ARCHIVED);

        workerRepository.save(worker);
    }

    private Worker getWorkerOwnedByCurrentUser(Long workerId, Long userId) {
        return workerRepository.findByIdAndUserId(workerId, userId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
    }

    private WorkerResponse mapToResponse(Worker worker) {
        return new WorkerResponse(
                worker.getId(),
                worker.getFullName(),
                worker.getEmail(),
                worker.getPhoneNumber(),
                worker.getJobTitle(),
                worker.getCategory(),
                worker.getAddress(),
                worker.getNotes(),
                worker.getCurrency(),
                worker.getPaymentRegion(),
                worker.isPaymentDetailsVerified(),
                worker.getStatus(),
                worker.getCreatedDate(),
                worker.getLastModifiedDate()
        );
    }
}
