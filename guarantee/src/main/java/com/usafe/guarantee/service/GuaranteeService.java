package com.usafe.guarantee.service;

import com.usafe.guarantee.domain.Guarantee;
import com.usafe.guarantee.domain.GuaranteeStatus;
import com.usafe.guarantee.repo.GuaranteeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuaranteeService {
    private final GuaranteeRepository repo;

    public Guarantee issue(Guarantee g) {
        g.setStatus(GuaranteeStatus.ISSUED);
        g.setIssuedAt(OffsetDateTime.now());
        return repo.save(g);
    }

    public Guarantee get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found: " + id));
    }

    public List<Guarantee> list() {
        return repo.findAll();
    }
}
