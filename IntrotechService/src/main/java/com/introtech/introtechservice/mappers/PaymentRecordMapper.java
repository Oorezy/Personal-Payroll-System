package com.introtech.introtechservice.mappers;

import com.introtech.introtechservice.dto.PaymentRecordResponse;
import com.introtech.introtechservice.entity.PaymentRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord, PaymentRecordResponse> {

    @Mapping(source = "worker.id", target = "workerId")
    @Mapping(source = "worker.fullName", target = "workerName")
    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "schedule.scheduleName", target = "scheduleName")
    PaymentRecordResponse toDto(PaymentRecord paymentRecord);

}