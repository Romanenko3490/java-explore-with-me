package ru.practicum.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.CategoryDto;
import ru.practicum.categories.NewCategoryRequest;
import ru.practicum.categories.UpdateCategoryRequest;
import ru.practicum.user.UserRequest;
import ru.practicum.user.UserDto;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminInternalController {
    private final AdminService adminService;

    //users
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody UserRequest request) {
        log.debug("Admin controller get new user request: {}", request);
        return adminService.addUser(request);
    }

    @GetMapping("/users")
    public List<UserDto> findUsersByIds(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.debug("Admin controller get users request: {}, from: {}, size: {}", ids, from, size);
        return adminService.findAllUsersByIds(ids, from, size);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(
            @PathVariable Long id
    ) {
        adminService.deleteUser(id);
    }

    //categories

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody NewCategoryRequest request) {
        return adminService.saveCategory(request);
    }

    @DeleteMapping("/categories/{id}")
    public void deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
    }

    @PatchMapping("/categories/{id}")
    public CategoryDto updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request
    ) {
        return adminService.updateCategory(id, request);
    }

}
