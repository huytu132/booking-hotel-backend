package com.example.identity_service.dto.response;

import com.example.identity_service.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    int id;
    String email;
    String firstName;
    String lastName;
    String phoneNo;
    Set<RoleResponse> roles;
}

