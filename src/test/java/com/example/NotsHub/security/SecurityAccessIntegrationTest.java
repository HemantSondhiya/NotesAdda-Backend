package com.example.NotsHub.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createBranch_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student1", roles = {"STUDENT"})
    void createBranch_shouldReturn403_forNonAdminRole() throws Exception {
        mockMvc.perform(post("/api/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "CSE",
                                  "code": "CSE",
                                  "programId": "6730fd14-b6b6-460e-8ec2-a0bf7d00f216"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"UNIVERSITY_ADMIN"})
    void createBranch_shouldReturn400_forAdminWithInvalidBody() throws Exception {
        mockMvc.perform(post("/api/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "student1", roles = {"STUDENT"})
    void approveNotes_shouldReturn403_forNonAdminRole() throws Exception {
        mockMvc.perform(put("/api/notes/{id}/approve", "6730fd14-b6b6-460e-8ec2-a0bf7d00f216"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authUsername_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/username"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student1", roles = {"STUDENT"})
    void authUsername_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/username"))
                .andExpect(status().isOk());
    }

    @Test
    void authUser_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student1", roles = {"STUDENT"})
    void authUser_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student1"));
    }

    @Test
    void authUsers_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student1", roles = {"STUDENT"})
    void authUsers_shouldReturn403_forStudentRole() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"UNIVERSITY_ADMIN"})
    void authUsers_shouldReturn200_forAdminRole() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isOk());
    }

    @Test
    void downloadNote_shouldNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/notes/{id}/download", "6730fd14-b6b6-460e-8ec2-a0bf7d00f216"))
                .andExpect(status().isBadRequest());
    }
}
