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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
    void createNotes_shouldStayPending_forNonAdminUser() {
        User student = userWithRole(1L, AppRole.ROLE_STUDENT);
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findByUserName("student1")).thenReturn(Optional.of(student));
        when(notesRepository.save(any(Notes.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Notes> captor = ArgumentCaptor.forClass(Notes.class);
        notesService.createNotes(request, "student1");
        verify(notesRepository).save(captor.capture());

        Notes saved = captor.getValue();
        assertTrue(!saved.getIsApproved());
        assertTrue(saved.getApprovedBy() == null);
        assertTrue(saved.getApprovedAt() == null);
    }

    @Test
    void createNotes_shouldAutoApprove_forAdminUser() {
        User admin = userWithRole(2L, AppRole.ROLE_UNIVERSITY_ADMIN);
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findByUserName("admin1")).thenReturn(Optional.of(admin));
        when(notesRepository.save(any(Notes.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Notes> captor = ArgumentCaptor.forClass(Notes.class);
        notesService.createNotes(request, "admin1");
        verify(notesRepository).save(captor.capture());

        Notes saved = captor.getValue();
        assertTrue(saved.getIsApproved());
        assertTrue(saved.getApprovedBy() != null);
        assertTrue(saved.getApprovedAt() != null);
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
