package com.hashedin.huspark.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    private LocalDateTime dueDate; // Optional, will use default if not provided
}
