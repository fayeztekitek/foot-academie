package com.nadi.service;

import com.nadi.model.ConsentementRGPD;
import com.nadi.model.Parent;
import com.nadi.repository.ConsentementRGPDRepository;
import com.nadi.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsentementRGPDService {

    private final ConsentementRGPDRepository consentementRepository;
    private final ParentRepository parentRepository;

    @Transactional(readOnly = true)
    public List<ConsentementRGPD> getAll() {
        return consentementRepository.findAllByOrderByDateConsentementDesc();
    }

    @Transactional(readOnly = true)
    public List<ConsentementRGPD> getByParent(Long parentId) {
        return consentementRepository.findByParentId(parentId);
    }

    @Transactional
    public ConsentementRGPD record(Long parentId, String type, boolean accord, String ipAddress, String details) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé: " + parentId));

        ConsentementRGPD consent = ConsentementRGPD.builder()
                .parent(parent)
                .typeConsentement(type)
                .accord(accord)
                .adresseIP(ipAddress)
                .details(details)
                .build();
        return consentementRepository.save(consent);
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        List<ConsentementRGPD> consentements = consentementRepository.findAllByOrderByDateConsentementDesc();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringWriter writer = new StringWriter();
        writer.write("ID,Parent,Email,Type Consentement,Accord,Date,Adresse IP,Details\n");

        for (ConsentementRGPD c : consentements) {
            writer.write(String.format("%d,%s %s,%s,%s,%s,%s,%s,%s\n",
                    c.getId(),
                    c.getParent().getPrenom(), c.getParent().getNom(),
                    c.getParent().getEmail() != null ? c.getParent().getEmail() : "",
                    c.getTypeConsentement(),
                    c.isAccord() ? "OUI" : "NON",
                    c.getDateConsentement().format(fmt),
                    c.getAdresseIP() != null ? c.getAdresseIP() : "",
                    c.getDetails() != null ? c.getDetails() : ""
            ));
        }
        return writer.toString();
    }

    @Transactional
    public void markExported(List<Long> ids) {
        for (Long id : ids) {
            ConsentementRGPD c = consentementRepository.findById(id).orElse(null);
            if (c != null) {
                c.setExporte(true);
                consentementRepository.save(c);
            }
        }
    }
}
