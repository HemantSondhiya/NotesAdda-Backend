package com.example.NotsHub.controller;

import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.NotesCreateRequest;
import com.example.NotsHub.payload.NotesDTO;
import com.example.NotsHub.payload.PagedResponse;
import com.example.NotsHub.service.NotesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NotesController {

    @Autowired
    private NotesService notesService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createNotes(@Valid @RequestBody NotesCreateRequest request,
                                         Authentication authentication) {
        NotesDTO notesDTO = notesService.createNotes(request, authentication.getName());
        return ResponseEntity.ok(new APIResponse<>("Notes created successfully", true, notesDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> updateNotes(@PathVariable UUID id,
                                         @Valid @RequestBody NotesCreateRequest request,
                                         Authentication authentication) {
        NotesDTO notesDTO = notesService.updateNotes(id, request, authentication.getName());
        return ResponseEntity.ok(new APIResponse<>("Notes updated successfully", true, notesDTO));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<NotesDTO> notes = notesService.getAllNotes(page, size);
        return ResponseEntity.ok(new APIResponse<>("Notes retrieved successfully", true, PagedResponse.from(notes)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> approveNotes(@PathVariable UUID id, Authentication authentication) {
        NotesDTO notesDTO = notesService.approveNotes(id, authentication.getName());
        return ResponseEntity.ok(new APIResponse<>("Notes approved successfully", true, notesDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> deleteNotes(@PathVariable UUID id, Authentication authentication) {
        notesService.deleteNotes(id, authentication.getName());
        return ResponseEntity.ok(new APIResponse<>("Notes deleted successfully", true, null));
    }
}
