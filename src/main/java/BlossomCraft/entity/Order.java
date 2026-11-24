
package BlossomCraft.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(nullable = false, length = 10)
    private String mobile;

    @Column(nullable = false, length = 1000)
    private String address;

    @Column(name = "cart_data", nullable = false, columnDefinition = "TEXT")
    private String cartData;

    @Column(nullable = false)
    private double total;

    @Column(name = "payment_method", nullable = false,
            length = 30, columnDefinition = "varchar(30) default 'COD'")
    private String paymentMethod = "COD";

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private String status = "Pending";

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Order() {
    }

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();

        if (status == null || status.isBlank()) {
            status = "Pending";
        }

        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "COD";
        }
    }

    // Check whether customer can cancel this order
    public boolean isCancellable() {
        return "Pending".equalsIgnoreCase(status)
                || "Confirmed".equalsIgnoreCase(status);
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCartData() {
        return cartData;
    }

    public void setCartData(String cartData) {
        this.cartData = cartData;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
