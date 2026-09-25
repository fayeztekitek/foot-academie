package com.nadi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadi.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    private static final AtomicInteger IP_SEQ = new AtomicInteger(200);

    private static RequestPostProcessor freshIp() {
        String ip = "10.20.50." + IP_SEQ.incrementAndGet();
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    @BeforeEach
    void setUp() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@nadi.tn");
        login.setMotDePasse("admin123");
        login.setTenantId(1L);

        MvcResult result = mockMvc.perform(post("/api/auth/login").contextPath("/api")
                        .with(freshIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void createCategoryReturns201() throws Exception {
        String catJson = """
                {
                    "nom": "U10-U12",
                    "description": "Catégorie intermédiaire",
                    "ageMin": 10,
                    "ageMax": 12
                }
                """;

        mockMvc.perform(post("/api/categories").contextPath("/api")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(catJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("U10-U12"));
    }

    @Test
    void listCategoriesReturnsResults() throws Exception {
        mockMvc.perform(get("/api/categories").contextPath("/api")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updateCategory() throws Exception {
        String createJson = """
                {
                    "nom": "U14-U17",
                    "ageMin": 14,
                    "ageMax": 17
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/categories").contextPath("/api")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long catId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateJson = """
                {
                    "nom": "U14-U17 Avancé",
                    "description": "Catégorie avancée",
                    "ageMin": 14,
                    "ageMax": 17
                }
                """;

        mockMvc.perform(put("/api/categories/" + catId).contextPath("/api")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("U14-U17 Avancé"));
    }

    @Test
    void deleteCategory() throws Exception {
        String catJson = """
                {
                    "nom": "To Delete",
                    "ageMin": 6,
                    "ageMax": 8
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/categories").contextPath("/api")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(catJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long catId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/categories/" + catId).contextPath("/api")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void coachCannotDeleteCategory() throws Exception {
        LoginRequest coachLogin = new LoginRequest();
        coachLogin.setEmail("coach@nadi.tn");
        coachLogin.setMotDePasse("coach123");
        coachLogin.setTenantId(1L);

        MvcResult coachResult = mockMvc.perform(post("/api/auth/login").contextPath("/api")
                        .with(freshIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coachLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String coachToken = objectMapper.readTree(coachResult.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(delete("/api/categories/1").contextPath("/api")
                        .header("Authorization", "Bearer " + coachToken))
                .andExpect(status().isForbidden());
    }
}
