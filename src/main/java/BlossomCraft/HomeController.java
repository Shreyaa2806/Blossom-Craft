
package BlossomCraft;

import BlossomCraft.entity.Order;
import BlossomCraft.service.OrderService;
import BlossomCraft.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final OrderService orderService;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public HomeController(OrderService orderService,
                          ProductService productService,
                          ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    // HOME PAGE
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // SIGNUP PAGE
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    // SHOP
    @GetMapping("/shop")
    public String shop(HttpSession session, Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("products", productService.getAllProducts());
        return "shop";
    }

    // CART
    @GetMapping("/cart")
    public String cart(HttpSession session) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        return "cart";
    }

    // CHECKOUT
    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        return "checkout";
    }

    // SUCCESS PAGE
    @GetMapping("/success")
    public String success(HttpSession session) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        return "success";
    }

    // CUSTOMER ORDERS
    @GetMapping("/orders")
    public String orders(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String cancelled) {

        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("loggedInUserId");

        List<Order> customerOrders =
                orderService.getCustomerOrders(userId);

        List<Map<String, Object>> displayOrders = new ArrayList<>();

        for (Order order : customerOrders) {

            Map<String, Object> orderData = new HashMap<>();

            orderData.put("id", order.getId());
            orderData.put("customerName", order.getCustomerName());
            orderData.put("mobile", order.getMobile());
            orderData.put("address", order.getAddress());
            orderData.put("orderDate", order.getOrderDate());
            orderData.put("total", order.getTotal());

            String paymentMethod = order.getPaymentMethod();

            if (paymentMethod == null || paymentMethod.isBlank()) {
                paymentMethod = "COD";
            }

            orderData.put("paymentMethod", paymentMethod);

            String status = order.getStatus();

            if (status == null || status.isBlank()) {
                status = "Pending";
            }

            orderData.put("status", status);

            boolean cancellable =
                    "Pending".equalsIgnoreCase(status)
                    || "Confirmed".equalsIgnoreCase(status);

            orderData.put("cancellable", cancellable);

            try {
                List<Map<String, Object>> items =
                        objectMapper.readValue(
                                order.getCartData(),
                                new TypeReference<List<Map<String, Object>>>() {}
                        );

                orderData.put("items", items);

            } catch (Exception e) {
                orderData.put("items", new ArrayList<>());
            }

            displayOrders.add(orderData);
        }

        model.addAttribute("orders", displayOrders);

        if ("success".equalsIgnoreCase(cancelled)) {
            model.addAttribute("cancelMessage",
                    "Your order has been cancelled successfully.");
        } else if ("failed".equalsIgnoreCase(cancelled)) {
            model.addAttribute("cancelMessage",
                    "Unable to cancel this order. Please check its status.");
        }

        return "orders";
    }

    // WISHLIST
    @GetMapping("/wishlist")
    public String wishlist(HttpSession session) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        return "wishlist";
    }

    // PRODUCT DETAILS
    @GetMapping("/product-details")
    public String productDetails(HttpSession session) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        return "product-details";
    }

    // CUSTOMER LOGIN CHECK
    private boolean isCustomerLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInUserId") != null;
    }
}
