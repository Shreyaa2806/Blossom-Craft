
package BlossomCraft.controller;

import BlossomCraft.entity.Order;
import BlossomCraft.service.OrderService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminController(OrderService orderService,
                           ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    // CHECK ADMIN LOGIN
    private boolean isAdminLoggedIn(HttpSession session) {
        return Boolean.TRUE.equals(
                session.getAttribute("adminLoggedIn"));
    }

    // ADMIN LOGIN PAGE
    @GetMapping("/admin/login")
    public String adminLoginPage(HttpSession session) {

        if (isAdminLoggedIn(session)) {
            return "redirect:/admin/dashboard";
        }

        return "admin-login";
    }

    // ADMIN LOGIN
    @PostMapping("/admin/login")
    public String adminLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (adminEmail.equals(email)
                && adminPassword.equals(password)) {

            session.setAttribute("adminLoggedIn", true);
            session.setAttribute("adminUsername", email);

            return "redirect:/admin/dashboard";
        }

        model.addAttribute("error",
                "Invalid admin email or password!");

        return "admin-login";
    }

    // ADMIN DASHBOARD
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        return "admin-dashboard";
    }

    // MANAGE ORDERS
    @GetMapping("/admin/orders")
    public String adminOrders(
            HttpSession session,
            Model model) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        List<Order> orders = orderService.getAllOrders();
        List<Map<String, Object>> displayOrders = new ArrayList<>();

        for (Order order : orders) {

            Map<String, Object> orderData = new HashMap<>();

            orderData.put("id", order.getId());
            orderData.put("customerName", order.getCustomerName());
            orderData.put("mobile", order.getMobile());
            orderData.put("address", order.getAddress());
            orderData.put("total", order.getTotal());
            orderData.put("paymentMethod", order.getPaymentMethod());
            orderData.put("orderDate", order.getOrderDate());
            orderData.put("status", order.getStatus());

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

        return "admin-orders";
    }

    // UPDATE ORDER STATUS
    @PostMapping("/admin/orders/update-status")
    public String updateOrderStatus(
            @RequestParam Long orderId,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        try {
            orderService.updateOrderStatus(orderId, status);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Order status updated successfully!");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    // ADMIN LOGOUT
    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {

        session.removeAttribute("adminLoggedIn");
        session.removeAttribute("adminUsername");

        return "redirect:/admin/login?logout";
    }
}
