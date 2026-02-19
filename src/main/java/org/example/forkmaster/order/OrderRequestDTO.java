package org.example.forkmaster.order;

import java.math.BigDecimal;
import java.util.List;

public class OrderRequestDTO {

    private Long customerId;
    private Long shippingAddressId;
    private List<OrderLineRequest> orderLines;
    private String orderStatus;
    private String orderType;
    private BigDecimal shippingCharge;

    public OrderRequestDTO() {
    }

    public OrderRequestDTO(Long customerId, Long shippingAddressId, List<OrderLineRequest> orderLines,
                           String orderStatus, String orderType, BigDecimal shippingCharge) {
        this.customerId = customerId;
        this.shippingAddressId = shippingAddressId;
        this.orderLines = orderLines;
        this.orderStatus = orderStatus;
        this.orderType = orderType;
        this.shippingCharge = shippingCharge;
    }

    public static class OrderLineRequest {
        private Long productId;
        private int quantity;

        public OrderLineRequest() {
        }

        public OrderLineRequest(Long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(Long shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public List<OrderLineRequest> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<OrderLineRequest> orderLines) {
        this.orderLines = orderLines;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public BigDecimal getShippingCharge() {
        return shippingCharge;
    }

    public void setShippingCharge(BigDecimal shippingCharge) {
        this.shippingCharge = shippingCharge;
    }
}
