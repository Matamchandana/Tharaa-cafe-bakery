package com.bakery.store;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// ==========================================
// 1. DATA MODELS / ENTITY LAYER (Maps to MySQL Tables)
// ==========================================

@Entity
@Table(name = "products")
class Product {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private String description;
    private double price;
    
    @Column(name = "image_url") // Links Java directly to our new MySQL column
    private String imageUrl;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; } // Added Getter
}

@Entity
@Table(name = "orders")
class Order {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private String customerEmail;
    private double totalAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}

@Entity
@Table(name = "order_items")
class OrderItem {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne 
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne 
    @JoinColumn(name = "product_id")
    private Product product;
    
    private int quantity;

    public OrderItem() {}
    public OrderItem(Order order, Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
    }
}

// ==========================================
// 2. DATA PAYLOADS / DTO LAYER (Models incoming checkout data)
// ==========================================

class OrderRequest {
    private String name;
    private String email;
    private List<CartItem> items;

    public String getName() { return name; }
    public String getEmail() { return email; }
    public List<CartItem> getItems() { return items; }

    public static class CartItem {
        private Long productId;
        private int quantity;
        public Long getProductId() { return productId; }
        public int getQuantity() { return quantity; }
    }
}

// ==========================================
// 3. REPOSITORY LAYER (Handles data reading/writing to MySQL)
// ==========================================

@Repository interface ProductRepository extends JpaRepository<Product, Long> {}
@Repository interface OrderRepository extends JpaRepository<Order, Long> {}
@Repository interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}

// ==========================================
// 4. REST CONTROLLER / API LAYER (Exposes Endpoints)
// ==========================================

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Prevents CORS blockages when local browser requests hit port 8080
class BakeryController {

    @Autowired private ProductRepository productRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderItemRepository orderItemRepo;

    // Route accessible at http://localhost:8080/api/products
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    // Route accessible at http://localhost:8080/api/checkout
    @PostMapping("/checkout")
    public String checkout(@RequestBody OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return "Cart is empty!";
        }

        // 1. Compute pricing totals via absolute records stored in the database
        double checkoutTotal = 0;
        for (OrderRequest.CartItem item : request.getItems()) {
            Product p = productRepo.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
            checkoutTotal += p.getPrice() * item.getQuantity();
        }

        // 2. Persist parent Order instance
        Order mainOrder = new Order();
        mainOrder.setCustomerName(request.getName());
        mainOrder.setCustomerEmail(request.getEmail());
        mainOrder.setTotalAmount(checkoutTotal);
        Order savedOrder = orderRepo.save(mainOrder);

        // 3. Connect line item rows back to parent Order via foreign keys
        for (OrderRequest.CartItem item : request.getItems()) {
            Product p = productRepo.findById(item.getProductId()).orElseThrow();
            OrderItem dynamicItem = new OrderItem(savedOrder, p, item.getQuantity());
            orderItemRepo.save(dynamicItem);
        }

        return "Success! Order #" + savedOrder.getId() + " confirmed. Total billed: $" + String.format("%.2f", checkoutTotal);
    }
}