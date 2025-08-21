package com.example.identity_service.mapper;

import com.example.identity_service.dto.response.PaymentResponse;
import com.example.identity_service.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentResponse toResponse(Payment payment);
}