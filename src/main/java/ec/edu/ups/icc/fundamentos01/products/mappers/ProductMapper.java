package ec.edu.ups.icc.fundamentos01.products.mappers;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.entities.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.models.ProductModel;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductModel toModelFromEntity(ProductEntity entity) {
        if (entity == null)
            return null;

        ProductModel model = new ProductModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setPrice(entity.getPrice());
        model.setStock(entity.getStock());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        UserEntity owner = entity.getOwner();
        if (owner != null) {
            model.setOwnerId(owner.getId());
            model.setOwnerName(owner.getName());
            model.setOwnerEmail(owner.getEmail());
            model.setOwnerCreatedAt(owner.getCreatedAt());
        }

        CategoryEntity category = entity.getCategory();
        if (category != null) {
            model.setCategoryId(category.getId());
            model.setCategoryName(category.getName());
            model.setCategoryDescription(category.getDescription());
        }

        return model;
    }

    public ProductResponseDto toResponse(ProductModel model) {
        if (model == null)
            return null;

        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setDescription(model.getDescription());
        dto.setPrice(model.getPrice());
        dto.setStock(model.getStock());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setUpdatedAt(model.getUpdatedAt());

        UserResponseDto ownerDto = new UserResponseDto();
        ownerDto.setId(model.getOwnerId());
        ownerDto.setName(model.getOwnerName());
        ownerDto.setEmail(model.getOwnerEmail());
        ownerDto.setCreatedAt(model.getOwnerCreatedAt());
        dto.setOwner(ownerDto);

        CategoryResponseDto categoryDto = new CategoryResponseDto();
        categoryDto.setId(model.getCategoryId());
        categoryDto.setName(model.getCategoryName());
        categoryDto.setDescription(model.getCategoryDescription());
        dto.setCategory(categoryDto);

        return dto;
    }
}