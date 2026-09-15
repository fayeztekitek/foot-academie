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

    @BeforeEach
    void setUp() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@nadi.tn");
        login.setMotDePasse("admin123");

        MvcResult result = mockMvc.perform(post("/auth/login")
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

        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(catJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("U10-U12"));
    }

    @Test
    void listCategoriesReturnsResults() throws Exception {
        mockMvc.perform(get("/categories")
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

        MvcResult result = mockMvc.perform(post("/categories")
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

        mockMvc.perform(put("/categories/" + catId)
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

        MvcResult result = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(catJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long catId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/categories/" + catId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void coachCannotDeleteCategory() throws Exception {
        LoginRequest coachLogin = new LoginRequest();
        coachLogin.setEmail("coach@nadi.tn");
        coachLogin.setMotDePasse("coach123");

        MvcResult coachResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coachLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String coachToken = objectMapper.readTree(coachResult.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(delete("/categories/1")
                        .header("Authorization", "Bearer " + coachToken))
                .andExpect(status().isForbidden());
    }
}
