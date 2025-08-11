package com.usafe.guarantee.repo;

import com.usafe.guarantee.domain.Guarantee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> { }
