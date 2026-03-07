package com.example.NotsHub.controller;

import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.NotesCreateRequest;
import com.example.NotsHub.payload.NotesDTO;
import com.example.NotsHub.payload.PagedResponse;
import com.example.NotsHub.service.NotesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes", description = "Endpoints for managing, uploading, and searching notes")
public class NotesController {

    @Autowired
    private NotesService notesService;



    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadPdf(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("subjectId") UUID subjectId,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        NotesDTO notesDTO = notesService.uploadPdfNote(title, description, subjectId, file, authentication.getName());
        return ResponseEntity.ok(new APIResponse<>("PDF uploaded successfully", true, notesDTO));
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
    public ResponseEntity<?> getAllNotes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String searchText = (q != null && !q.isBlank()) ? q : query;
        Page<NotesDTO> notes = notesService.searchNotes(searchText, page, size);
        return ResponseEntity.ok(new APIResponse<>("Notes retrieved successfully", true, PagedResponse.from(notes)));
    }

    @Operation(summary = "Get notes by their slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getNotesBySlug(@PathVariable String slug) {
        NotesDTO notes = notesService.getBySlug(slug);
        return ResponseEntity.ok(new APIResponse<>("Notes retrieved successfully", true, notes));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchNotes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String searchText = (q != null && !q.isBlank()) ? q : query;
        Page<NotesDTO> notes = notesService.searchNotes(searchText, page, size);
        return ResponseEntity.ok(new APIResponse<>("Notes search completed successfully", true, PagedResponse.from(notes)));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> getDownloadLink(@PathVariable UUID id, Authentication authentication) {
        String requesterUsername = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(new APIResponse<>(
                "Download link generated successfully",
                true,
                notesService.generateDownloadLink(id, requesterUsername)
        ));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> approveNotes(@PathVariable UUID id, Authentication authentication) {
        NotesDTO notesDTO = notesService.approveNotes(id, authentication.getName());
        return ResponseEntity.ok(new APIResponse<>("Notes approved successfully", true, notesDTO));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> rejectNotes(@PathVariable UUID id, 
                                        @RequestParam(required = false) String rejectionNote, 
                                        Authentication authentication) {
        NotesDTO notesDTO = notesService.rejectNotes(id, rejectionNote, authentication.getName());
        return ResponseEntity.ok(new APIResponse<>("Notes rejected successfully", true, notesDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> deleteNotes(@PathVariable UUID id, Authentication authentication) {
        notesService.deleteNotes(id, authentication.getName());
        return ResponseEntity.ok(new APIResponse<>("Notes deleted successfully", true, null));
    }
}
