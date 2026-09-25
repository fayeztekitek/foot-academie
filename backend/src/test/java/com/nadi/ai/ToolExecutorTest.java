package com.nadi.ai;

import com.nadi.dto.JoueurResponse;
import com.nadi.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolExecutorTest {

    @Mock
    private DashboardService dashboardService;
    @Mock
    private JoueurService joueurService;
    @Mock
    private PaiementService paiementService;
    @Mock
    private CreneauService creneauService;
    @Mock
    private EvenementService evenementService;
    @Mock
    private AttendanceService attendanceService;

    private ToolExecutor executor() {
        return new ToolExecutor(dashboardService, joueurService, paiementService,
                creneauService, evenementService, attendanceService);
    }

    @Test
    void unknownToolReturnsErrorJson() {
        String result = executor().execute("drop_database", Map.of());

        assertTrue(result.contains("Outil inconnu"));
    }

    @Test
    void playerListReturnsBoundedPage() {
        JoueurResponse player = JoueurResponse.builder().id(1L).prenom("J").nom("N").build();
        when(joueurService.getAll(any())).thenReturn(new PageImpl<>(List.of(player)));

        String result = executor().execute("get_player_list", Map.of());

        assertTrue(result.contains("\"count\":1"));
        assertTrue(result.contains("\"players\""));
    }

    @Test
    void overduePaymentsAreSerialized() {
        when(paiementService.getOverdue()).thenReturn(List.of());

        String result = executor().execute("get_overdue_payments", Map.of());

        assertTrue(result.contains("\"count\":0"));
    }

    @Test
    void playerStatsSearchByName() {
        when(joueurService.search(eq("Amine"), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        String result = executor().execute("get_player_stats", Map.of("playerName", "Amine"));

        assertTrue(result.contains("\"count\":0"));
    }

    @Test
    void dashboardSummaryIsSerialized() {
        when(dashboardService.getStats()).thenReturn(null);

        String result = executor().execute("get_dashboard_summary", Map.of());

        assertEquals("null", result);
    }

    @Test
    void createReminderIsSideEffectFreeStub() {
        String result = executor().execute("create_reminder",
                Map.of("playerName", "Amine", "amount", 60));

        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("Amine"));
    }
}
