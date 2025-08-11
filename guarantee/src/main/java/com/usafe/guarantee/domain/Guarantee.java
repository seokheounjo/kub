package com.usafe.guarantee.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guarantee {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String buyerName;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private GuaranteeStatus status;

    private OffsetDateTime issuedAt;
}
