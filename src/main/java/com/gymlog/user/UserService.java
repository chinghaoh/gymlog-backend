package com.gymlog.user;

import com.gymlog.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        return mapToDto(findUserOrThrow(id));
    }

    @Transactional
    public UserDto createUser(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new AppException(
                    HttpStatus.CONFLICT, "Email already exists: " + dto.getEmail());
        }
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .role(dto.getRole() != null ? dto.getRole() : Role.USER)
                .build();
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        User user = findUserOrThrow(id);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        findUserOrThrow(id);
        userRepository.deleteById(id);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}