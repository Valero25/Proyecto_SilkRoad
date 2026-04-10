package edu.dosw.parcial.core.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.dosw.parcial.dto.request.RegisterUserRequest;
import edu.dosw.parcial.dto.response.UserResponse;
import edu.dosw.parcial.core.models.User;
import edu.dosw.parcial.core.repositories.UserRepository;
import edu.dosw.parcial.core.utils.BusinessException;
import edu.dosw.parcial.core.utils.ErrorCodes;
import edu.dosw.parcial.core.utils.UtilHelper;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        String normalizedEmail = UtilHelper.normalizeEmail(request.getEmail());

        if (!UtilHelper.isInstitutionalEmail(normalizedEmail)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCodes.INVALID_INSTITUTIONAL_EMAIL,
                    "El correo debe ser institucional (dominio con .edu)");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    ErrorCodes.EMAIL_ALREADY_REGISTERED,
                    "El correo ya se encuentra registrado");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getId(), savedUser.getFullName(), savedUser.getEmail(), savedUser.getRole());
    }
}
