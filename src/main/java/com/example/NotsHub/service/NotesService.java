package com.example.NotsHub.service;

import com.example.NotsHub.payload.NotesCreateRequest;
import com.example.NotsHub.payload.NotesDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotesService {
    NotesDTO createNotes(@Valid NotesCreateRequest request, String uploaderUsername);
    NotesDTO updateNotes(UUID notesId, @Valid NotesCreateRequest request, String updaterUsername);
    NotesDTO approveNotes(UUID notesId, String approverUsername);
    Page<NotesDTO> getAllNotes(int page, int size);
    void deleteNotes(UUID notesId, String deleterUsername);
}
