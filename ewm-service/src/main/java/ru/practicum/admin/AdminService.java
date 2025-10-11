package ru.practicum.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.*;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AdminService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    //Users
    public UserDto addUser(UserRequest request) {
        log.debug("Adding user {}", request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DataConflictException(
                    "could not execute statement; SQL [n/a];" +
                            " constraint " +  request.getEmail() + ";" +
                            " nested exception is org.hibernate.exception." +
                            "ConstraintViolationException: could not execute statement");
        }

        User user = userRepository.save(User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build());
        log.debug("Saved user: {}", user);

        return userMapper.userToUserDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAllUsersByIds(List<Long> ids, Integer from, Integer size) {
        log.debug("Finding all users by ids {}", ids);
        Pageable pageable = PageRequest.of(from / size, size);

        if (ids != null && !ids.isEmpty()) {
            List<User> users = userRepository.findUsersByIds(ids, pageable);
            log.debug("Found {} users by ids {}", users.size(), users);

            return users.stream()
                    .map(userMapper::userToUserDto)
                    .collect(Collectors.toList());
        }
        List<User> users = userRepository.findAllBy(pageable);
        log.debug("Found {} users by ids {}", users.size(), users);

        return users.stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id=" + id + " was not found");
        }

        userRepository.deleteById(id);
    }

    //Categories
    public CategoryDto saveCategory(NewCategoryRequest request) {
        log.debug("Saving category {}", request);

        if (categoryRepository.existsByName((request.getName()))) {
            throw new DataConflictException("could not execute statement; SQL [n/a];" +
                    " constraint " + request.getName() + "; " +
                    "nested exception is org.hibernate.exception" +
                    ".ConstraintViolationException: could not execute statement");
        }

        Category category = categoryRepository.save(Category.builder()
                .name(request.getName())
                .build()
        );

        return categoryMapper.categoryToCategoryDto(category);
    }

    public void deleteCategory(Long id) {
        log.debug("Deleting category {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }

        // if (существуют события связаные с категорией) {
        //          throw new DataConflictException()

//                "status": "CONFLICT",
//                "reason": "For the requested operation the conditions are not met.",
//                "message": "The category is not empty",
//                "timestamp": "2023-01-21 16:56:19
//             }

        categoryRepository.deleteById(id);
    }

    public CategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        log.debug("Updating category {}", id);

        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Category with id=" + id + " was not found")
        );

        if (!request.getName().equals(category.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DataConflictException("could not execute statement; SQL [n/a];" +
                    " constraint " + request.getName() + "; " +
                    "nested exception is org.hibernate.exception" +
                    ".ConstraintViolationException: could not execute statement");
        }

        category.setName(request.getName());
        return categoryMapper.categoryToCategoryDto(categoryRepository.save(category));
    }

}
