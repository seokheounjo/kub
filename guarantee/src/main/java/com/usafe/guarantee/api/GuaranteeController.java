package com.usafe.guarantee.api;

import com.usafe.guarantee.domain.Guarantee;
import com.usafe.guarantee.service.GuaranteeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GuaranteeController {

    private final GuaranteeService service;

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/version")
    public ResponseEntity<String> version() {
        return ResponseEntity.ok(appVersion);
    }

    @PostMapping("/guarantees")
    public Guarantee issue(@RequestBody Guarantee req) {
        return service.issue(req);
    }

    @GetMapping("/guarantees/{id}")
    public Guarantee get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/guarantees")
    public List<Guarantee> list() {
        return service.list();
    }
}
