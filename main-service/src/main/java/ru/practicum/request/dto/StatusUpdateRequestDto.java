package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.model.RequestStatus;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequestDto {
    Set<Integer> requestIds;
    RequestStatus status;
}
