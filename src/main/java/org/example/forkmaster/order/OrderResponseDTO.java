package org.example.forkmaster.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {

    private Long id;
    private Long customerId;
    private String customerName;
    private AddressResponse shippingAddress;
    private List<OrderLineResponse> orderLines;
    private BigDecimal shippingCharge;
    private BigDecimal totalPrice;
    private boolean shipped;
    private String orderStatus;
    private String orderType;
    private LocalDateTime orderDate;

    public OrderResponseDTO() {
    }

    public OrderResponseDTO(Long id, Long customerId, String customerName, AddressResponse shippingAddress,
                            List<OrderLineResponse> orderLines, BigDecimal shippingCharge, BigDecimal totalPrice,
                            boolean shipped, String orderStatus, String orderType, LocalDateTime orderDate) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.orderLines = orderLines;
        this.shippingCharge = shippingCharge;
        this.totalPrice = totalPrice;
        this.shipped = shipped;
        this.orderStatus = orderStatus;
        this.orderType = orderType;
        this.orderDate = orderDate;
    }

    public static class AddressResponse {
        private Long id;
        private String street;
        private String city;
        private String postalCode;
        private String country;

        public AddressResponse() {
        }

        public AddressResponse(Long id, String street, String city, String postalCode, String country) {
            this.id = id;
            this.street = street;
            this.city = city;
            this.postalCode = postalCode;
            this.country = country;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    public static class OrderLineResponse {
        private Long id;
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal priceAtPurchase;
        private BigDecimal lineTotal;

        public OrderLineResponse() {
        }

        public OrderLineResponse(Long id, Long productId, String productName, int quantity,
                                 BigDecimal priceAtPurchase, BigDecimal lineTotal) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.priceAtPurchase = priceAtPurchase;
            this.lineTotal = lineTotal;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPriceAtPurchase() {
            return priceAtPurchase;
        }

        public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
            this.priceAtPurchase = priceAtPurchase;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public AddressResponse getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressResponse shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderLineResponse> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<OrderLineResponse> orderLines) {
        this.orderLines = orderLines;
    }

    public BigDecimal getShippingCharge() {
        return shippingCharge;
    }

    public void setShippingCharge(BigDecimal shippingCharge) {
        this.shippingCharge = shippingCharge;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isShipped() {
        return shipped;
    }

    public void setShipped(boolean shipped) {
        this.shipped = shipped;
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

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}
