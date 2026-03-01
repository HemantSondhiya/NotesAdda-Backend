package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class SemesterCreateRequest {

    @NotNull(message = "Semester number is required")
    private Short number;

    @NotNull(message = "Branch ID is required")
    private UUID branchId;
}

