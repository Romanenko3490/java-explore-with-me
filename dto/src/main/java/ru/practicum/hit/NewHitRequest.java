package ru.practicum.hit;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;


@Value // Работает так же как и @Data только создает immutable класс с final полями и без сеттеров
@Jacksonized // @Value конфликтует с джексеном, и он не может десериальизвать
// LocalDateTime, нужна зависимость jackson-databind
@Builder // @Jacksonized работает через @Buildeer
public class NewHitRequest {

    @NotBlank
    String app;

    @NotBlank
    @URL
    String uri;

    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.)" +
            "{3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:)" +
            "{7}[0-9a-fA-F]{1,4}$",
            message = "Invalid IP address")
    String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;


}
