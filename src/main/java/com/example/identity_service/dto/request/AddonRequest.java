package com.example.identity_service.dto.request;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddonRequest {
    private String addonName;
    private BigDecimal price;
    private String description;
    private Boolean active;
}

