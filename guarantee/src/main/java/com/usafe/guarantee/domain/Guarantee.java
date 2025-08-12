package com.usafe.guarantee.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guarantee {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String guaranteeNumber;  // 보증서 고유번호 (UUID)

    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private String buyerName;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuaranteeStatus status;

    private OffsetDateTime issuedAt;
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (guaranteeNumber == null) {
            guaranteeNumber = "GTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        issuedAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
