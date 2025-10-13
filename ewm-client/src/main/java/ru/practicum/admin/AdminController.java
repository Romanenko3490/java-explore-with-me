package ru.practicum.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.categories.CategoryDto;
import ru.practicum.categories.NewCategoryRequest;
import ru.practicum.categories.UpdateCategoryRequest;
import ru.practicum.events.EventDto;
import ru.practicum.events.UpdateEventRequest;
import ru.practicum.user.UserDto;
import ru.practicum.user.UserRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {
    private final AdminWebUserClient adminUserClient;
    private final AdminWebCategoriesClient adminCategoriesClient;
    private final AdminWebEventClient adminWebEventClient;

    //Users

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(
            @Valid
            @RequestBody UserRequest request) {
        return adminUserClient.addUser(request);
    }

    @GetMapping("/users")
    public Mono<List<UserDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return adminUserClient.getUsers(ids, from, size);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable @Min(1) Long id
    ) {
        adminUserClient.deleteUser(id);
    }

    //Categories

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(
            @Valid
            @RequestBody NewCategoryRequest request
    ) {
        return adminCategoriesClient.addCategory(request);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @PathVariable @Min(1) Long id
    ) {
        adminCategoriesClient.deleteCategory(id);
    }

    @PatchMapping("/categories/{id}")
    public CategoryDto updateCategory(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid UpdateCategoryRequest request
    ) {
        return adminCategoriesClient.updateCategory(id, request);
    }

    //События

    @GetMapping("/events")
    public Mono<List<EventDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size
    ) {
        return adminWebEventClient.getEvents(users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                from, size);
    }

    @PatchMapping("/events/{eventId}")
    public EventDto updateEvent(
            @PathVariable Long eventId,
            @RequestBody UpdateEventRequest request) {
        return adminWebEventClient.updateEvent(eventId, request);
    }


}
