package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateResultDto {
    List<RequestDto> confirmedRequests;
    List<RequestDto> rejectedRequests;
}
