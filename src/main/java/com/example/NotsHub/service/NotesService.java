package com.example.NotsHub.service;

import com.example.NotsHub.payload.NotesDTO;
import com.example.NotsHub.payload.NotesUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface NotesService {

    NotesDTO uploadPdfNote(String title, String description, UUID subjectId, MultipartFile file, String uploaderUsername);
    NotesDTO updateNotes(UUID notesId, @Valid NotesUpdateRequest request, String updaterUsername);
    NotesDTO approveNotes(UUID notesId, String approverUsername);
    NotesDTO rejectNotes(UUID notesId, String rejectionNote, String rejectorUsername);
    Map<String, String> generateAdminViewLink(UUID notesId);
    Map<String, String> generateDownloadLink(UUID notesId, @Nullable String requesterUsername);

    @Transactional(readOnly = true)
    Page<NotesDTO> getAllNotes(int page, int size);

    Page<NotesDTO> getAllNotesForAdmin(@Nullable Boolean isApproved, @Nullable Boolean reject, int page, int size);
    Page<NotesDTO> getAdminNotes(@Nullable String query, @Nullable Boolean approved, int page, int size);
    Page<NotesDTO> getMyNotes(String requesterUsername, @Nullable String query, @Nullable Boolean isApproved, @Nullable Boolean reject, int page, int size);
    Page<NotesDTO> getNotesBySubject(UUID subjectId, int page, int size);
    NotesDTO getBySlug(String slug);
    Page<NotesDTO> searchNotes(String query, int page, int size);
    void deleteNotes(UUID notesId, String deleterUsername);
}
