package com.example.identity_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BedTypeRequest {
    @NotBlank(message = "Bed type name is required")
    @Size(max = 50, message = "Bed type name must not exceed 50 characters")
    private String bedName;
}
