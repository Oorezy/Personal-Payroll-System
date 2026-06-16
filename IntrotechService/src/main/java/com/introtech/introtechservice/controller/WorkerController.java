package com.introtech.introtechservice.controller;

import com.introtech.introtechservice.common.AppResponse;
import com.introtech.introtechservice.common.BaseController;
import com.introtech.introtechservice.dto.CreateWorkerRequest;
import com.introtech.introtechservice.dto.UpdateWorkerRequest;
import com.introtech.introtechservice.dto.WorkerResponse;
import com.introtech.introtechservice.entity.Worker;
import com.introtech.introtechservice.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController extends BaseController<Worker, Long> {

    private final WorkerService workerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppResponse<WorkerResponse> createWorker(@Valid @RequestBody CreateWorkerRequest request) {
        return toAppResponse(workerService.createWorker(request));
    }

    @GetMapping
    public AppResponse<List<WorkerResponse>> getMyWorkers() {
        return toAppResponse(workerService.getMyWorkers());
    }

    @GetMapping("/{workerId}")
    public AppResponse<WorkerResponse> getWorker(@PathVariable Long workerId) {
        return toAppResponse(workerService.getWorker(workerId));
    }

    @PutMapping("/{workerId}")
    public AppResponse<WorkerResponse> updateWorker(
            @PathVariable Long workerId,
            @Valid @RequestBody UpdateWorkerRequest request
    ) {
        return toAppResponse(workerService.updateWorker(workerId, request));
    }

    @PatchMapping("/{workerId}/deactivate")
    public AppResponse<WorkerResponse> deactivateWorker(@PathVariable Long workerId) {
        return toAppResponse(workerService.deactivateWorker(workerId));
    }

    @PatchMapping("/{workerId}/activate")
    public AppResponse<WorkerResponse> activateWorker(@PathVariable Long workerId) {
        return toAppResponse(workerService.activateWorker(workerId));
    }

    @DeleteMapping("/{workerId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
    public AppResponse<?> archiveWorker(@PathVariable Long workerId) {
        workerService.archiveWorker(workerId);
        return toAppResponse("Worker archived");
    }
}
