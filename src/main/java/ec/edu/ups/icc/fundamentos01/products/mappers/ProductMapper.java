package ec.edu.ups.icc.fundamentos01.products.mappers;

import java.util.List;
import org.springframework.stereotype.Component;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.models.ProductModel;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;

@Component
public class ProductMapper {

    public ProductModel toModelFromEntity(ProductEntity entity) {
        if (entity == null) return null;

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

        // SECCIÓN 17 DE LA GUÍA: Convertimos el Set<CategoryEntity> de la entidad a List<CategoryEntity> para el modelo
        if (entity.getCategories() != null) {
            model.setCategories(entity.getCategories().stream().toList());
        }

        return model;
    }

    public ProductResponseDto toResponse(ProductModel model) {
        if (model == null) return null;

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

        // SECCIÓN 17 DE LA GUÍA: Mapeamos de CategoryEntity (desde el modelo) a CategoryResponseDto
        if (model.getCategories() != null) {
            List<CategoryResponseDto> categories = model.getCategories()
                    .stream()
                    .map(c -> new CategoryResponseDto(c.getId(), c.getName(), c.getDescription()))
                    .toList();
            dto.setCategories(categories);
        }

        return dto;
    }
}