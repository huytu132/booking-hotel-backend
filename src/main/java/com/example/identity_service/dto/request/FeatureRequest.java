package com.example.identity_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureRequest {
    @NotBlank(message = "Feature name is required")
    @Size(max = 100, message = "Feature name must not exceed 100 characters")
    private String featureName;
}
