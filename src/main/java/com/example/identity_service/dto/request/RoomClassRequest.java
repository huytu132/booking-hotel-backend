package com.example.identity_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class RoomClassRequest {
    @NotBlank(message = "Room class name is required")
    @Size(max = 50, message = "Room class name must not exceed 50 characters")
    private String roomClassName;

    @NotBlank(message = "Quantity is required")
    private String quantity;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive or zero")
    private BigDecimal priceOriginal;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Min(value = 0, message = "Discount percent must be at least 0")
    @Max(value = 100, message = "Discount percent must not exceed 100")
    private Integer discountPercent;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    @NotNull(message = "Hotel ID is required")
    private Integer hotelId;

    @NotEmpty(message = "Bed type IDs list cannot be empty")
    private List<Integer> bedTypeIds;

    @NotEmpty(message = "Feature IDs list cannot be empty")
    private List<Integer> featureIds;

    private List<String> roomImagePaths;
}
