package com.example.NotsHub.service;

import com.example.NotsHub.Repository.NotesRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.Notes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotesUploadProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotesUploadProcessingService.class);

    private final NotesRepository notesRepository;
    private final S3StorageService s3StorageService;

    public NotesUploadProcessingService(
            NotesRepository notesRepository,
            S3StorageService s3StorageService
    ) {
        this.notesRepository = notesRepository;
        this.s3StorageService = s3StorageService;
    }

    @Async("uploadTaskExecutor")
    @Transactional
    public void processUploadedNoteAsync(String requestId, UUID notesId) {
        try {
            Notes note = notesRepository.findById(notesId)
                    .orElseThrow(() -> new APIException("Notes not found with id: " + notesId));

            String checksum = s3StorageService.calculateSha256ForObject(note.getFileKey());
            note.setFileChecksum(checksum);

            notesRepository.save(note);
            LOGGER.info("Upload background processing completed requestId={}, noteId={}", requestId, notesId);
        } catch (Exception ex) {
            LOGGER.error("Upload background processing failed requestId={}, noteId={}, error={}",
                    requestId, notesId, ex.getMessage(), ex);
        }
    }
}
