
package BlossomCraft.controller;

import BlossomCraft.entity.Order;
import BlossomCraft.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderController(OrderService orderService,
                           ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @RequestParam String customerName,
            @RequestParam String mobile,
            @RequestParam String address,
            @RequestParam String cartData,
            @RequestParam(defaultValue = "COD") String paymentMethod,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("loggedInUserId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            if (customerName == null || customerName.isBlank()
                    || mobile == null || !mobile.matches("[0-9]{10}")
                    || address == null || address.isBlank()) {
                return "redirect:/checkout?error=details";
            }

            if (!paymentMethod.equals("COD")
                    && !paymentMethod.equals("UPI_DEMO")
                    && !paymentMethod.equals("CARD_DEMO")) {
                return "redirect:/checkout?error=payment";
            }

            JsonNode cart = objectMapper.readTree(cartData);

            if (!cart.isArray() || cart.isEmpty()) {
                return "redirect:/checkout?error=empty";
            }

            for (JsonNode item : cart) {

                if (!item.path("id").canConvertToLong()
                        || !item.path("quantity").canConvertToInt()) {
                    return "redirect:/checkout?error=invalid";
                }

                if (item.path("id").asLong() <= 0
                        || item.path("quantity").asInt() <= 0) {
                    return "redirect:/checkout?error=invalid";
                }
            }

            Order savedOrder = orderService.placeOrder(
                    userId,
                    customerName.trim(),
                    mobile,
                    address.trim(),
                    cartData,
                    1.0,
                    paymentMethod
            );

            return "redirect:/success?order=placed&id="
                    + savedOrder.getId();

        } catch (IllegalArgumentException e) {

            String message = e.getMessage() == null
                    ? "" : e.getMessage();

            if (message.contains("Insufficient stock")) {
                return "redirect:/checkout?error=stock";
            }

            if (message.contains("no longer exists")) {
                return "redirect:/checkout?error=unavailable";
            }

            return "redirect:/checkout?error=invalid";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=failed";
        }
    }

    // CANCEL CUSTOMER ORDER
    @PostMapping("/cancel-order")
    public String cancelOrder(
            @RequestParam Long orderId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("loggedInUserId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            orderService.cancelOrder(orderId, userId);

            return "redirect:/orders?cancelled=success";

        } catch (IllegalArgumentException e) {

            e.printStackTrace();
            return "redirect:/orders?cancelled=failed";

        } catch (Exception e) {

            e.printStackTrace();
            return "redirect:/orders?cancelled=failed";
        }
    }
}
