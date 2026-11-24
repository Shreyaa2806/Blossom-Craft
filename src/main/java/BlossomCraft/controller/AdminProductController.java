
package BlossomCraft.controller;

import BlossomCraft.entity.Product;
import BlossomCraft.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Controller
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    // CHECK ADMIN LOGIN
    private boolean isAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"));
    }

    // EDIT PRODUCT
    @PostMapping("/admin/products/update")
    public String updateProduct(
            @RequestParam Long id,
            @RequestParam double price,
            @RequestParam String description,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Product product = productService.getProductById(id);

        if (product == null || price <= 0) {
            return "redirect:/manage-products";
        }

        product.setPrice(price);
        product.setDescription(description);

        productService.updateProduct(id, product);

        return "redirect:/manage-products";
    }

    // CHANGE PRODUCT IMAGE
    @PostMapping("/admin/products/change-image")
    public String changeProductImage(
            @RequestParam Long id,
            @RequestParam MultipartFile image,
            HttpSession session) throws IOException {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Product product = productService.getProductById(id);

        if (product == null || image == null || image.isEmpty()) {
            return "redirect:/manage-products";
        }

        if (image.getContentType() == null ||
                !image.getContentType().startsWith("image/")) {
            return "redirect:/manage-products";
        }

        String contentType = image.getContentType();

        String base64 = Base64.getEncoder()
                .encodeToString(image.getBytes());

        product.setImage("data:" + contentType + ";base64," + base64);

        productService.updateProduct(id, product);

        return "redirect:/manage-products";
    }

    // UPDATE STOCK
    @PostMapping("/admin/products/update-stock")
    public String updateStock(
            @RequestParam Long id,
            @RequestParam int stock,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Product product = productService.getProductById(id);

        if (product == null || stock < 0) {
            return "redirect:/manage-products";
        }

        product.setStock(stock);

        productService.updateProduct(id, product);

        return "redirect:/manage-products";
    }

    // DELETE PRODUCT
    @PostMapping("/admin/products/delete")
    public String deleteProduct(
            @RequestParam Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Product product = productService.getProductById(id);

        if (product != null) {
            productService.deleteProduct(id);
        }

        return "redirect:/manage-products";
    }
}
