package com.example.NotsHub.service;

import com.example.NotsHub.payload.CollegeCreateRequest;
import com.example.NotsHub.payload.CollegeDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface CollegeService {
    CollegeDTO createCollege(@Valid CollegeCreateRequest request);

    List<CollegeDTO> getAllColleges();
}
