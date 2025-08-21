package com.example.identity_service.dto.response;

import com.example.identity_service.enums.PaymentStatus;
import com.example.identity_service.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Integer id;
    private Integer bookingId;
    private PaymentStatus paymentStatus;
    private String transId;
    private BigDecimal paymentAmount;
    private PaymentType paymentType;
    private LocalDateTime paymentDate;
}