package com.example.identity_service.dto.request;

import com.example.identity_service.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Integer bookingId;
    private BigDecimal amount;
    private PaymentType paymentType;
}