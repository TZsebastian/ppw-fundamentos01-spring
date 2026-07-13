package ec.edu.ups.icc.fundamentos01.products.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ec.edu.ups.icc.fundamentos01.categories.entities.CategoryEntity;

public class ProductModel {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime ownerCreatedAt;

    private List<CategoryEntity> categories;

    public ProductModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public LocalDateTime getOwnerCreatedAt() {
        return ownerCreatedAt;
    }

    public void setOwnerCreatedAt(LocalDateTime ownerCreatedAt) {
        this.ownerCreatedAt = ownerCreatedAt;
    }

    public List<CategoryEntity> getCategories() {
        return categories;
    }

    /*
     * Clase interna simple para representar los datos mínimos
     * de una categoría dentro del modelo de producto.
     */
    public static class CategorySummary {
        private Long id;
        private String name;
        private String description;

        public CategorySummary() {
        }

        public CategorySummary(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public void setCategories(List<CategoryEntity> categories) {
        this.categories = categories;
    }
}