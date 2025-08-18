package com.example.identity_service.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddonResponse {
    private int id;
    private String addonName;
    private BigDecimal price;
    private String description;
    private Boolean active;
}
