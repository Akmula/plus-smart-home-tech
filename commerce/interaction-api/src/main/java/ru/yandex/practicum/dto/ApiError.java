package ru.yandex.practicum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiError {
    Throwable cause;
    List<StackTraceElement> stackTrace;
    HttpStatus httpStatus;
    String userMessage;
    String message;
    List<Throwable> suppressed;
    String localizedMessage;
}