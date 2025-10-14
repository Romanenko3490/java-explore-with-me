package ru.practicum.requests;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    RequestStatus status;
}
