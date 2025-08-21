package com.hashedin.huspark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortRequest {
    private String field;
    private String direction = "ASC"; // ASC or DESC
}
