package BlossomCraft.service;

import BlossomCraft.entity.Product;
import BlossomCraft.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Add a new product
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // Update product
    public Product updateProduct(Long id, Product updatedProduct) {

        Product product = productRepository.findById(id).orElse(null);

        if (product == null) {
            return null;
        }

        product.setName(updatedProduct.getName());
        product.setCategory(updatedProduct.getCategory());
        product.setPrice(updatedProduct.getPrice());
        product.setDescription(updatedProduct.getDescription());
        product.setImage(updatedProduct.getImage());
        product.setStock(updatedProduct.getStock());

        return productRepository.save(product);
    }

    // Delete product
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}