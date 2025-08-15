package com.example.identity_service.service;

import com.example.identity_service.dto.request.RoleRequest;
import com.example.identity_service.dto.response.RoleResponse;
import com.example.identity_service.entity.Role;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.RoleMapper;
import com.example.identity_service.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;

    public List<RoleResponse> getAllRoles(){
        return roleRepository.findAll()
                .stream().map(role -> roleMapper.toRoleResonse(role))
                .collect(Collectors.toList());
    }

    public RoleResponse createRole(RoleRequest request){
        if (roleRepository.existsById(request.getName())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Role role = roleMapper.toRole(request);
        return roleMapper.toRoleResonse(roleRepository.save(role));
    }

    public String deleteRole(String role){
        roleRepository.deleteById(role);
        return "role was deleted";
    }
}
