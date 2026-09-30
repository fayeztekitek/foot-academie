package com.nadi.service;

import com.nadi.dto.CreneauRequest;
import com.nadi.dto.CreneauResponse;
import com.nadi.model.Categorie;
import com.nadi.model.Creneau;
import com.nadi.model.Entraineur;
import com.nadi.model.JourSemaine;
import com.nadi.repository.AbsenceRepository;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.CreneauRepository;
import com.nadi.repository.EntraineurRepository;
import com.nadi.repository.JoueurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreneauServiceLegacyCoachTest {

    @Mock
    private CreneauRepository creneauRepository;
    @Mock
    private CategorieRepository categorieRepository;
    @Mock
    private EntraineurRepository entraineurRepository;
    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private NotificationService notificationService;

    private CreneauService service() {
        return new CreneauService(creneauRepository, categorieRepository, entraineurRepository,
                absenceRepository, joueurRepository, notificationService);
    }

    private Categorie category() {
        Categorie cat = Categorie.builder().nom("U13").build();
        cat.setId(4L);
        return cat;
    }

    private Entraineur coach() {
        Entraineur coach = Entraineur.builder().prenom("Karim").nom("Amri").build();
        coach.setId(5L);
        return coach;
    }

    private CreneauRequest request() {
        CreneauRequest request = new CreneauRequest();
        request.setJourSemaine("LUNDI");
        request.setHeureDebut(LocalTime.of(16, 0));
        request.setHeureFin(LocalTime.of(17, 30));
        request.setCategorieId(4L);
        request.setTerrain("Terrain A");
        request.setEntraineurId(5L);
        return request;
    }

    @Test
    void createBackfillsLegacyCoachField() {
        when(categorieRepository.findById(4L)).thenReturn(Optional.of(category()));
        when(entraineurRepository.findById(5L)).thenReturn(Optional.of(coach()));
        when(creneauRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(joueurRepository.findByCategorieId(4L)).thenReturn(List.of());

        service().create(request());

        ArgumentCaptor<Creneau> captor = ArgumentCaptor.forClass(Creneau.class);
        verify(creneauRepository).save(captor.capture());
        assertNotNull(captor.getValue().getEntraineur());
        assertEquals(5L, captor.getValue().getEntraineur().getId());
    }

    @Test
    void createWithoutCoachLeavesLegacyFieldNull() {
        when(categorieRepository.findById(4L)).thenReturn(Optional.of(category()));
        when(creneauRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(joueurRepository.findByCategorieId(4L)).thenReturn(List.of());

        CreneauRequest noCoach = request();
        noCoach.setEntraineurId(null);
        CreneauResponse response = service().create(noCoach);

        assertNotNull(response);
        ArgumentCaptor<Creneau> captor = ArgumentCaptor.forClass(Creneau.class);
        verify(creneauRepository).save(captor.capture());
        assertNull(captor.getValue().getEntraineur());
    }
}
