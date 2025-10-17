package ru.practicum.util;

import org.apache.coyote.BadRequestException;
import ru.practicum.events.Dateable;
import ru.practicum.exception.EmailValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RequestsValidator {

    public static void dateValidation(Dateable request) throws BadRequestException {


        if (request.getEventDate() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (request.getEventDate().isBefore(now)) {
            throw new BadRequestException("Field: eventDate. Error: должно содержать дату," +
                    " которая еще не наступила. Value: " + request.getEventDate().format(formatter));
        } else if (!request.getEventDate().isAfter(now.plusHours(2))) {
            throw new BadRequestException("Field: eventDate. Error: " +
                    "Event date must be at least 2 hours from now. Value: " + request.getEventDate().format(formatter));
        }
    }

    public static void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new EmailValidationException("Field: email. Error: must not be empty. Value: null");
        }

        if (email.length() > 254) {
            throw new EmailValidationException("Field: email. Error: max length of email 254ch." +
                    " Value: " + email.length());
        }

        String[] atParts = email.split("@");
        if (atParts.length != 2) {
            throw new EmailValidationException("Field: email. Error: should two prats divided by @. Value: " + email);
        }

        String localPart = atParts[0];
        String domainPart = atParts[1];

        if (localPart.length() > 64) {
            throw new EmailValidationException("Field: email. Error: max size of local part = 64. Value: " + localPart.length());
        }

        String[] domainParts = domainPart.split("\\.");

        for (String part : domainParts) {
            if (part.length() > 63) {
                throw new EmailValidationException("Field: email. Error: max size of each domain part = 63." +
                        " Value: " + part.length());
            }
        }
    }
}
