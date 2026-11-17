package com.vsblog.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import com.jayway.jsonpath.JsonPath;
import com.vsblog.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(AdminSessionController.class)
@Import({AdminSessionController.class, SecurityConfig.class, AdminSessionControllerTest.TestSecurityConfig.class})
class AdminSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginCreatesSessionAndCurrentSessionReturnsAuthenticatedUser() throws Exception {
        mockMvc.perform(post("/api/admin/login")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"StrongPass123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.username").value("admin"));

        mockMvc.perform(get("/api/admin/session").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void wrongPasswordDoesNotCreateSession() throws Exception {
        mockMvc.perform(post("/api/admin/login")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUserCannotAccessSession() throws Exception {
        mockMvc.perform(get("/api/admin/session"))
                .andExpect(status().isForbidden());
    }

    @Test
    void csrfEndpointProvidesMatchingRequestHeaderAndToken() throws Exception {
        MvcResult tokenResponse = mockMvc.perform(get("/api/admin/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.headerName").isNotEmpty())
                .andReturn();
        MockHttpSession session = (MockHttpSession) tokenResponse.getRequest().getSession(false);
        assertNotNull(session);
        String token = JsonPath.read(tokenResponse.getResponse().getContentAsString(), "$.token");
        String headerName = JsonPath.read(tokenResponse.getResponse().getContentAsString(), "$.headerName");

        mockMvc.perform(post("/api/admin/logout")
                        .with(user("admin").roles("ADMIN"))
                        .session(session)
                        .header(headerName, token))
                .andExpect(status().isOk());
    }

    @Configuration
    static class TestSecurityConfig {

        @Bean
        AdminUserDetailsService adminUserDetailsService() {
            return new AdminUserDetailsService(null) {
                @Override
                public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                    if (!"admin".equals(username)) {
                        throw new UsernameNotFoundException("Unknown user");
                    }
                    return new User("admin", new BCryptPasswordEncoder().encode("StrongPass123!"), List.of());
                }
            };
        }
    }
}
