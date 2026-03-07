package com.example.NotsHub.service;

import com.example.NotsHub.payload.NotesCreateRequest;
import com.example.NotsHub.payload.NotesDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface NotesService {

    NotesDTO uploadPdfNote(String title, String description, UUID subjectId, MultipartFile file, String uploaderUsername);
    NotesDTO updateNotes(UUID notesId, @Valid NotesCreateRequest request, String updaterUsername);
    NotesDTO approveNotes(UUID notesId, String approverUsername);
    NotesDTO rejectNotes(UUID notesId, String rejectionNote, String rejectorUsername);
    Map<String, String> generateDownloadLink(UUID notesId, @Nullable String requesterUsername);
    Page<NotesDTO> getAllNotes(int page, int size);
    Page<NotesDTO> getNotesBySubject(UUID subjectId, int page, int size);
    NotesDTO getBySlug(String slug);
    Page<NotesDTO> searchNotes(String query, int page, int size);
    void deleteNotes(UUID notesId, String deleterUsername);
}
