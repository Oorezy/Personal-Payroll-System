package com.introtech.introtechservice.repository;

import com.introtech.introtechservice.common.BaseRepository;
import com.introtech.introtechservice.common.enums.WorkerStatus;
import com.introtech.introtechservice.entity.Worker;

import java.util.List;
import java.util.Optional;

public interface WorkerRepository extends BaseRepository<Worker, Long> {


    List<Worker> findByUserIdAndStatusNotOrderByCreatedDateDesc(Long userId, WorkerStatus status);

    Optional<Worker> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatusNot(Long userId, WorkerStatus status);

    long countByUserIdAndStatus(Long userId, WorkerStatus status);
}
