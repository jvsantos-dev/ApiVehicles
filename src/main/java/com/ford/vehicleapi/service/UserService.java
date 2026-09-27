package com.ford.vehicleapi.service;

import com.ford.vehicleapi.domain.Role;
import com.ford.vehicleapi.domain.User;
import com.ford.vehicleapi.dto.user.UserResponse;
import com.ford.vehicleapi.exception.BusinessRuleException;
import com.ford.vehicleapi.exception.ResourceNotFoundException;
import com.ford.vehicleapi.repository.UserRepository;
import com.ford.vehicleapi.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAllByOrderByIdAsc().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUser(id));
    }

    @Transactional
    public UserResponse updateRole(Long id, Role role, CurrentUser currentUser) {
        if (id.equals(currentUser.id())) {
            throw new BusinessRuleException("Um administrador não pode alterar o próprio perfil");
        }
        User user = getUser(id);
        user.setRole(role);
        return UserResponse.from(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }
}
