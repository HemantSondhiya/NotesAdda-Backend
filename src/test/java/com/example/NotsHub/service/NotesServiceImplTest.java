package com.example.NotsHub.service;

import com.example.NotsHub.Repository.NotesRepository;
import com.example.NotsHub.Repository.SubjectRepository;
import com.example.NotsHub.Repository.UserRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.AppRole;
import com.example.NotsHub.model.Notes;
import com.example.NotsHub.model.Role;
import com.example.NotsHub.model.Subject;
import com.example.NotsHub.model.User;
import com.example.NotsHub.payload.NotesCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotesServiceImplTest {

    @Mock
    private NotesRepository notesRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private NotesServiceImpl notesService;

    private Subject subject;
    private NotesCreateRequest request;

    @BeforeEach
    void setUp() {
        subject = new Subject();
        subject.setId(UUID.randomUUID());

        request = new NotesCreateRequest();
        request.setTitle("t");
        request.setDescription("d");
        request.setFileUrl("https://example.com/a.pdf");
        request.setFileKey("notes/a.pdf");
        request.setFileType("pdf");
        request.setSubjectId(subject.getId());
    }



    @Test
    void approveNotes_shouldFail_forNonAdminApprover() {
        UUID notesId = UUID.randomUUID();
        User student = userWithRole(3L, AppRole.ROLE_STUDENT);
        Notes notes = new Notes();
        notes.setId(notesId);

        when(notesRepository.findById(notesId)).thenReturn(Optional.of(notes));
        when(userRepository.findByUserName("student2")).thenReturn(Optional.of(student));

        assertThrows(APIException.class, () -> notesService.approveNotes(notesId, "student2"));
    }

    @Test
    void generateDownloadLink_shouldAllowAnonymous_forApprovedNote() {
        UUID notesId = UUID.randomUUID();
        Notes notes = new Notes();
        notes.setId(notesId);
        notes.setTitle("approved-note");
        notes.setFileKey("notes/u/approved.pdf");
        notes.setIsApproved(true);

        when(notesRepository.findById(notesId)).thenReturn(Optional.of(notes));
        when(s3StorageService.createPresignedDownloadUrl("notes/u/approved.pdf", "approved-note.pdf"))
                .thenReturn("https://download.example");
        when(s3StorageService.getPresignedExpiryMinutes()).thenReturn(5);

        Map<String, String> result = notesService.generateDownloadLink(notesId, null);

        assertEquals("https://download.example", result.get("downloadUrl"));
        assertEquals("5", result.get("expiresInMinutes"));
        verify(userRepository, never()).findByUserName(any());
    }

    @Test
    void generateDownloadLink_shouldRejectAnonymous_forUnapprovedNote() {
        UUID notesId = UUID.randomUUID();
        Notes notes = new Notes();
        notes.setId(notesId);
        notes.setTitle("draft-note");
        notes.setFileKey("notes/u/draft.pdf");
        notes.setIsApproved(false);

        when(notesRepository.findById(notesId)).thenReturn(Optional.of(notes));

        APIException ex = assertThrows(APIException.class, () -> notesService.generateDownloadLink(notesId, null));
        assertEquals("Only approved notes are available for public download", ex.getMessage());
        verify(s3StorageService, never()).createPresignedDownloadUrl(any(), any());
    }

    @Test
    void uploadPdfNote_shouldDeleteUploadedFile_whenSaveFails() throws Exception {
        User student = userWithRole(1L, AppRole.ROLE_STUDENT);
        String key = "notes/student1/temp.pdf";

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("dummy".getBytes());

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findByUserName("student1")).thenReturn(Optional.of(student));
        when(s3StorageService.uploadPdfToPending(any(), eq("student1")))
                .thenReturn(new S3StorageService.UploadResult(key, "s3://bucket/" + key));
        when(notesRepository.save(any(Notes.class))).thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class,
                () -> notesService.uploadPdfNote("t", "d", subject.getId(), mockFile, "student1"));
        verify(s3StorageService).deleteFile(key);
    }



    @Test
    void searchNotes_shouldSearchByTitleOrDescription_whenQueryProvided() {
        Notes note = new Notes();
        note.setId(UUID.randomUUID());
        note.setTitle("Data Structures Unit 1");
        note.setDescription("linked list basics");
        note.setFileUrl("https://example.com/note.pdf");
        note.setFileKey("notes/note.pdf");
        note.setFileType("PDF");
        note.setIsApproved(true);
        note.setSubject(subject);

        PageRequest pageable = PageRequest.of(0, 20);
        Page<Notes> results = new PageImpl<>(List.of(note), pageable, 1);

        when(notesRepository.findByIsApprovedTrueAndTitleContainingIgnoreCaseOrIsApprovedTrueAndDescriptionContainingIgnoreCase(
                eq("linked"),
                eq("linked"),
                any(PageRequest.class)
        )).thenReturn(results);

        Page<?> response = notesService.searchNotes("linked", 0, 20);

        assertEquals(1, response.getTotalElements());
    }

    @Test
    void searchNotes_shouldFallbackToAllNotes_whenQueryBlank() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Notes> results = new PageImpl<>(List.of(), pageable, 0);

        when(notesRepository.findByIsApprovedTrue(any(PageRequest.class))).thenReturn(results);

        Page<?> response = notesService.searchNotes("   ", 0, 20);

        assertEquals(0, response.getTotalElements());
        verify(notesRepository, never()).findByIsApprovedTrueAndTitleContainingIgnoreCaseOrIsApprovedTrueAndDescriptionContainingIgnoreCase(
                any(),
                any(),
                any(PageRequest.class)
        );
    }

    private User userWithRole(Long id, AppRole roleName) {
        User user = new User();
        user.setUserId(id);
        user.setUserName("u" + id);
        Role role = new Role();
        role.setRoleName(roleName);
        user.setRoles(Set.of(role));
        return user;
    }
}
