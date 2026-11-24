
package BlossomCraft.service;

import BlossomCraft.entity.Order;
import BlossomCraft.entity.Product;
import BlossomCraft.entity.User;
import BlossomCraft.repository.OrderRepository;
import BlossomCraft.repository.ProductRepository;
import BlossomCraft.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    // Existing method: keeps compatibility with older calls
    @Transactional
    public Order placeOrder(Long userId,
                            String customerName,
                            String mobile,
                            String address,
                            String cartData,
                            double total) {

        return placeOrder(
                userId, customerName, mobile,
                address, cartData, total, "COD"
        );
    }

    // Place order with selected payment method
    @Transactional
    public Order placeOrder(Long userId,
                            String customerName,
                            String mobile,
                            String address,
                            String cartData,
                            double total,
                            String paymentMethod) {

        try {
            if (cartData == null || cartData.isBlank()) {
                throw new IllegalArgumentException("Your cart is empty!");
            }

            List<String> allowedPaymentMethods = List.of(
                    "COD", "UPI_DEMO", "CARD_DEMO"
            );

            if (paymentMethod == null
                    || !allowedPaymentMethods.contains(paymentMethod)) {
                throw new IllegalArgumentException("Invalid payment method!");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Customer not found!"));

            JsonNode cart = objectMapper.readTree(cartData);

            if (!cart.isArray() || cart.isEmpty()) {
                throw new IllegalArgumentException("Your cart is empty!");
            }

            Map<Long, Integer> quantities = new LinkedHashMap<>();

            for (JsonNode item : cart) {

                long productId = readPositiveLong(item.path("id"));
                int quantity = readPositiveInt(item.path("quantity"));

                int newQuantity =
                        quantities.getOrDefault(productId, 0) + quantity;

                if (newQuantity > 100) {
                    throw new IllegalArgumentException("Invalid cart quantity!");
                }

                quantities.put(productId, newQuantity);
            }

            BigDecimal calculatedTotal = BigDecimal.ZERO;
            ArrayNode verifiedCart = objectMapper.createArrayNode();

            for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {

                Long productId = entry.getKey();
                int quantity = entry.getValue();

                Product product = productRepository.findByIdForUpdate(productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "A product in your cart no longer exists!"
                                ));

                if (product.getStock() < quantity) {
                    throw new IllegalArgumentException(
                            "Insufficient stock for " + product.getName()
                    );
                }

                if (product.getPrice() <= 0) {
                    throw new IllegalArgumentException("Invalid product price!");
                }

                BigDecimal price = BigDecimal.valueOf(product.getPrice());

                calculatedTotal = calculatedTotal.add(
                        price.multiply(BigDecimal.valueOf(quantity))
                );

                product.setStock(product.getStock() - quantity);
                productRepository.save(product);

                ObjectNode verifiedItem = objectMapper.createObjectNode();

                verifiedItem.put("id", product.getId());
                verifiedItem.put("name", product.getName());
                verifiedItem.put("price", product.getPrice());
                verifiedItem.put("image",
                        product.getImage() == null ? "" : product.getImage());
                verifiedItem.put("description",
                        product.getDescription() == null
                                ? "" : product.getDescription());
                verifiedItem.put("quantity", quantity);

                verifiedCart.add(verifiedItem);
            }

            if (calculatedTotal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Invalid order total!");
            }

            Order order = new Order();

            order.setCustomerName(customerName);
            order.setMobile(mobile);
            order.setAddress(address);
            order.setCartData(objectMapper.writeValueAsString(verifiedCart));
            order.setTotal(calculatedTotal.doubleValue());
            order.setPaymentMethod(paymentMethod);
            order.setUser(user);

            return orderRepository.save(order);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to process order!", e);
        }
    }

    // Read a valid positive product ID
    private long readPositiveLong(JsonNode node) {

        try {
            String value = node.asText();

            long number = Long.parseLong(value);

            if (number <= 0) {
                throw new IllegalArgumentException();
            }

            return number;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Order product details are invalid!"
            );
        }
    }

    // Read a valid positive quantity
    private int readPositiveInt(JsonNode node) {

        try {
            String value = node.asText();

            int number = Integer.parseInt(value);

            if (number <= 0) {
                throw new IllegalArgumentException();
            }

            return number;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Order product details are invalid!"
            );
        }
    }

    // Get customer orders
    public List<Order> getCustomerOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    // Get all customer orders for admin
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Cancel an order and restore product stock
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException("Please log in first!");
        }

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found!"));

        // Make sure this order belongs to the logged-in customer
        if (order.getUser() == null
                || !userId.equals(order.getUser().getId())) {
            throw new IllegalArgumentException(
                    "You are not allowed to cancel this order!"
            );
        }

        // Check whether this order can be cancelled
        if (!order.isCancellable()) {
            throw new IllegalArgumentException(
                    "This order can no longer be cancelled!"
            );
        }

        try {
            JsonNode cart = objectMapper.readTree(order.getCartData());

            if (cart == null || !cart.isArray() || cart.isEmpty()) {
                throw new IllegalArgumentException(
                        "Order product details are invalid!"
                );
            }

            // Collect quantities before changing stock
            Map<Long, Integer> quantities = new LinkedHashMap<>();

            for (JsonNode item : cart) {

                long productId = readPositiveLong(item.path("id"));
                int quantity = readPositiveInt(item.path("quantity"));

                quantities.merge(productId, quantity, Integer::sum);
            }

            // Restore stock for products that still exist
            for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {

                Long productId = entry.getKey();
                int quantity = entry.getValue();

                productRepository.findByIdForUpdate(productId)
                        .ifPresent(product -> {
                            product.setStock(product.getStock() + quantity);
                            productRepository.save(product);
                        });
            }

            // Mark order as cancelled
            order.setStatus("Cancelled");
            orderRepository.save(order);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to cancel order!", e);
        }
    }

    // Update order status for admin
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {

        List<String> allowedStatuses = List.of(
                "Pending",
                "Confirmed",
                "Shipped",
                "Delivered"
        );

        if (!allowedStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid order status!");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found!"));

        order.setStatus(status);

        orderRepository.save(order);
    }
}
