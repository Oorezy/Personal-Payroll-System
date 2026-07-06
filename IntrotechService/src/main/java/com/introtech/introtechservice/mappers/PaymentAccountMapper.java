package com.introtech.introtechservice.mappers;

import com.introtech.introtechservice.dto.PaymentAccountResponse;
import com.introtech.introtechservice.entity.PaymentAccount;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentAccountMapper extends BaseMapper<PaymentAccount, PaymentAccountResponse> {
}