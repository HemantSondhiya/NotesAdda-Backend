package com.example.NotsHub.service;

import com.example.NotsHub.Repository.CollegeRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.College;
import com.example.NotsHub.payload.CollegeCreateRequest;
import com.example.NotsHub.payload.CollegeDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollegeServiceImpl implements CollegeService {
    @Autowired
    private CollegeRepository collegeRepository;
    @Autowired
    private ModelMapper modelMapper;
    
    // ── Create ──────────────────────────────────────────────
    @Override
    public CollegeDTO createCollege(CollegeCreateRequest request) {

        if (collegeRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new APIException(
                    "College with code '" + request.getCode() + "' already exists");
        }

        College college = new College();
        college.setName(request.getName());
        college.setCode(request.getCode().toUpperCase());
        college.setCity(request.getCity());
        college.setState(request.getState());
        college.setLogoUrl(request.getLogoUrl());
        college.setIsActive(true);

        College saved = collegeRepository.save(college);
        return mapToDTO(saved);
    }

    @Override
    public List<CollegeDTO> getAllColleges() {
        return collegeRepository.findAllByIsActiveTrue()
                .stream()
                .map(college -> mapToDTO((College) college))
                .collect(Collectors.toList());
    }

    private CollegeDTO mapToDTO(College college) {
        CollegeDTO dto = new CollegeDTO();
        dto.setId(college.getId());
        dto.setName(college.getName());
        dto.setCode(college.getCode());
        dto.setCity(college.getCity());
        dto.setState(college.getState());
        dto.setLogoUrl(college.getLogoUrl());
        dto.setIsActive(college.getIsActive());
        dto.setCreatedAt(college.getCreatedAt());
        return dto;
    }
}



