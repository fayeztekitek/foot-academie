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
class PlayerIntegrationTest {

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

        String response = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    void createPlayerReturns201() throws Exception {
        String playerJson = """
                {
                    "prenom": "Ahmed",
                    "nom": "Ben Ali",
                    "dateNaissance": "2012-05-15",
                    "categorieId": null,
                    "parentId": null
                }
                """;

        mockMvc.perform(post("/players")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prenom").value("Ahmed"))
                .andExpect(jsonPath("$.nom").value("Ben Ali"));
    }

    @Test
    void listPlayersReturnsPaginatedResults() throws Exception {
        mockMvc.perform(get("/players")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createAndRetrievePlayer() throws Exception {
        String playerJson = """
                {
                    "prenom": "Mohamed",
                    "nom": "Sassi",
                    "dateNaissance": "2010-08-20"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/players")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long playerId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/players/" + playerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenom").value("Mohamed"))
                .andExpect(jsonPath("$.nom").value("Sassi"));
    }

    @Test
    void parentCannotAccessOtherPlayers() throws Exception {
        LoginRequest parentLogin = new LoginRequest();
        parentLogin.setEmail("parent@nadi.tn");
        parentLogin.setMotDePasse("parent123");

        MvcResult parentResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parentLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String parentToken = objectMapper.readTree(parentResult.getResponse().getContentAsString()).get("accessToken").asText();

        String playerJson = """
                {
                    "prenom": "Test",
                    "nom": "Player",
                    "dateNaissance": "2012-01-01"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/players")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long otherPlayerId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/players/" + otherPlayerId)
                        .header("Authorization", "Bearer " + parentToken))
                .andExpect(status().isForbidden());
    }
}
