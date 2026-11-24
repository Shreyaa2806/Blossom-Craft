
package BlossomCraft.controller;

import BlossomCraft.entity.Product;
import BlossomCraft.service.ProductService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // SAVE NEW PRODUCT - ADMIN ONLY
    @PostMapping("/save-product")
    public String saveProduct(
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam double price,
            @RequestParam String description,
            @RequestParam("image") MultipartFile imageFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // CHECK ADMIN LOGIN
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        try {
            if (imageFile == null || imageFile.isEmpty()) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage", "Please select an image!");
                return "redirect:/add-product";
            }

            if (imageFile.getContentType() == null ||
                    !imageFile.getContentType().startsWith("image/")) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage", "Please select a valid image!");
                return "redirect:/add-product";
            }

            if (price <= 0) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage", "Please enter a valid price!");
                return "redirect:/add-product";
            }

            String image = "data:" + imageFile.getContentType()
                    + ";base64,"
                    + Base64.getEncoder().encodeToString(imageFile.getBytes());

            Product product = new Product();

            product.setName(name);
            product.setCategory(category);
            product.setPrice(price);
            product.setDescription(description);
            product.setImage(image);
            product.setStock(0);

            productService.addProduct(product);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Product added successfully!");

            return "redirect:/manage-products";

        } catch (Exception e) {
            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Unable to save product!");

            return "redirect:/add-product";
        }
    }
}
