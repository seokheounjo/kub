package com.usafe.guarantee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Lombok 없이도 동작하도록 명시적인 게터/세터 작성
 * (환경에 따라 annotation processing 미설정일 수 있어서)
 */
public class GuaranteeForm {

    @NotBlank(message = "주문번호를 입력하세요.")
    private String orderId;

    @NotBlank(message = "구매자를 입력하세요.")
    private String buyerName;

    @NotNull(message = "금액을 입력하세요.")
    @DecimalMin(value = "0.0", inclusive = false, message = "금액은 0보다 커야 합니다.")
    private BigDecimal amount;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
