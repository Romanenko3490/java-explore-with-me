package ru.practicum.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.categories.CategoryDto;
import ru.practicum.categories.NewCategoryRequest;
import ru.practicum.categories.UpdateCategoryRequest;
import ru.practicum.compilations.CompilationDto;
import ru.practicum.compilations.NewCompilationRequest;
import ru.practicum.compilations.UpdateCompilationRequest;
import ru.practicum.events.EventDto;
import ru.practicum.events.UpdateEventRequest;
import ru.practicum.user.UserDto;
import ru.practicum.user.UserRequest;
import ru.practicum.util.RequestsValidator;

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
    private final AdminCompilationClient adminCompilationClient;

    //Users

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(
            @Valid
            @RequestBody UserRequest request) {

        RequestsValidator.validateEmail(request.getEmail());

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
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
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
            @RequestBody @Valid UpdateEventRequest request) throws BadRequestException {

        RequestsValidator.dateValidation(request);

        return adminWebEventClient.updateEvent(eventId, request);
    }

    //Admin: Подборки событий
    //API для работы с подборками событий

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationRequest request) {
        return adminCompilationClient.addCompilation(request);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCompilation(@PathVariable @Min(1) Long compId) {
        adminCompilationClient.deleteCompilation(compId);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilation(@PathVariable @Min(1) Long compId) {
        return adminCompilationClient.getCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCompilation( @PathVariable @Min(1) Long compId,
            @RequestBody @Valid UpdateCompilationRequest request) {
        return adminCompilationClient.updateCompilation(compId ,request);
    }


}
