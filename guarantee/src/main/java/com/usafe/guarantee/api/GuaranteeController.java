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

    // CREATE
    @PostMapping("/guarantees")
    public Guarantee issue(@RequestBody Guarantee req) {
        return service.issue(req);
    }

    // READ
    @GetMapping("/guarantees/{id}")
    public Guarantee get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/guarantees/number/{guaranteeNumber}")
    public Guarantee getByGuaranteeNumber(@PathVariable String guaranteeNumber) {
        return service.getByGuaranteeNumber(guaranteeNumber);
    }

    @GetMapping("/guarantees")
    public List<Guarantee> list(@RequestParam(required = false) String orderId,
                               @RequestParam(required = false) String buyerName) {
        if (orderId != null || buyerName != null) {
            return service.search(orderId, buyerName);
        }
        return service.list();
    }

    @GetMapping("/guarantees/search/order/{orderId}")
    public List<Guarantee> searchByOrderId(@PathVariable String orderId) {
        return service.searchByOrderId(orderId);
    }

    @GetMapping("/guarantees/search/buyer/{buyerName}")
    public List<Guarantee> searchByBuyerName(@PathVariable String buyerName) {
        return service.searchByBuyerName(buyerName);
    }

    // UPDATE
    @PutMapping("/guarantees/{id}")
    public Guarantee update(@PathVariable Long id, @RequestBody Guarantee req) {
        return service.update(id, req);
    }

    @PatchMapping("/guarantees/{id}/cancel")
    public Guarantee cancel(@PathVariable Long id) {
        service.cancel(id);
        return service.get(id);
    }

    // DELETE
    @DeleteMapping("/guarantees/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
