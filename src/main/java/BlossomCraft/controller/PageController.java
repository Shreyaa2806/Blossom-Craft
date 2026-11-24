
package BlossomCraft.controller;

import BlossomCraft.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final ProductService productService;

    public PageController(ProductService productService) {
        this.productService = productService;
    }

    // ADD PRODUCT - ADMIN ONLY
    @GetMapping("/add-product")
    public String addProduct(HttpSession session) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        return "add-product";
    }

    // MANAGE PRODUCTS - ADMIN ONLY
    @GetMapping("/manage-products")
    public String manageProducts(HttpSession session, Model model) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        model.addAttribute("products", productService.getAllProducts());

        return "manage-products";
    }
}
