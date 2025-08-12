package com.usafe.guarantee.repo;

import com.usafe.guarantee.domain.Guarantee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> {
    
    // 주문번호로 조회
    List<Guarantee> findByOrderIdContainingIgnoreCase(String orderId);
    
    // 보증서번호로 조회  
    Optional<Guarantee> findByGuaranteeNumber(String guaranteeNumber);
    
    // 구매자명으로 조회
    List<Guarantee> findByBuyerNameContainingIgnoreCase(String buyerName);
    
    // 복합 검색
    @Query("SELECT g FROM Guarantee g WHERE " +
           "(:orderId IS NULL OR LOWER(g.orderId) LIKE LOWER(CONCAT('%', :orderId, '%'))) AND " +
           "(:buyerName IS NULL OR LOWER(g.buyerName) LIKE LOWER(CONCAT('%', :buyerName, '%')))")
    List<Guarantee> findByOrderIdAndBuyerName(@Param("orderId") String orderId, 
                                            @Param("buyerName") String buyerName);
}
