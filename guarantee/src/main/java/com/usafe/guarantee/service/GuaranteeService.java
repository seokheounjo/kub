package com.usafe.guarantee.service;

import com.usafe.guarantee.domain.Guarantee;
import com.usafe.guarantee.domain.GuaranteeStatus;
import com.usafe.guarantee.repo.GuaranteeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GuaranteeService {
    private final GuaranteeRepository repo;

    public Guarantee issue(Guarantee g) {
        g.setStatus(GuaranteeStatus.ISSUED);
        return repo.save(g);  // @PrePersist에서 시간 설정
    }

    @Transactional(readOnly = true)
    public Guarantee get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("보증서를 찾을 수 없습니다: " + id));
    }

    @Transactional(readOnly = true)
    public Guarantee getByGuaranteeNumber(String guaranteeNumber) {
        return repo.findByGuaranteeNumber(guaranteeNumber)
                .orElseThrow(() -> new IllegalArgumentException("보증서를 찾을 수 없습니다: " + guaranteeNumber));
    }

    @Transactional(readOnly = true)
    public List<Guarantee> list() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Guarantee> searchByOrderId(String orderId) {
        return repo.findByOrderIdContainingIgnoreCase(orderId);
    }

    @Transactional(readOnly = true)
    public List<Guarantee> searchByBuyerName(String buyerName) {
        return repo.findByBuyerNameContainingIgnoreCase(buyerName);
    }

    @Transactional(readOnly = true)
    public List<Guarantee> search(String orderId, String buyerName) {
        if ((orderId == null || orderId.trim().isEmpty()) && 
            (buyerName == null || buyerName.trim().isEmpty())) {
            return list();
        }
        return repo.findByOrderIdAndBuyerName(
            orderId != null && !orderId.trim().isEmpty() ? orderId.trim() : null,
            buyerName != null && !buyerName.trim().isEmpty() ? buyerName.trim() : null
        );
    }

    public Guarantee update(Long id, Guarantee updateRequest) {
        Guarantee existing = get(id);
        
        existing.setOrderId(updateRequest.getOrderId());
        existing.setBuyerName(updateRequest.getBuyerName());
        existing.setAmount(updateRequest.getAmount());
        existing.setStatus(updateRequest.getStatus());
        
        return repo.save(existing);  // @PreUpdate에서 updatedAt 설정
    }

    public void delete(Long id) {
        Guarantee existing = get(id);
        repo.delete(existing);
    }

    public void cancel(Long id) {
        Guarantee existing = get(id);
        existing.setStatus(GuaranteeStatus.CANCELLED);
        repo.save(existing);
    }
}
