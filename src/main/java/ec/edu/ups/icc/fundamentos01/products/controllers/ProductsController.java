package ec.edu.ups.icc.fundamentos01.products.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import ec.edu.ups.icc.fundamentos01.core.dto.PaginationDto;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
 * Controlador REST encargado de exponer endpoints HTTP
 * para la gestión de productos.
 *
 * Todos los endpoints de este controlador requieren JWT,
 * porque el proyecto usa .anyRequest().authenticated().
 */
@Tag(name = "Productos", description = "Gestión de productos con paginación, roles y ownership")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductService service;

    public ProductsController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todos los productos", description = """
            Devuelve todos los productos activos sin paginación.

            Este endpoint es administrativo y requiere ROLE_ADMIN.
            Para consultas normales se recomienda usar /products/page o /products/slice.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado completo de productos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene ROLE_ADMIN")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponseDto> findAll() {
        return service.findAll();
    }

    @Operation(summary = "Obtener producto por id", description = "Devuelve un producto específico según su id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ProductResponseDto findOne(@PathVariable Long id) {
        return service.findOne(id);
    }

    /*
     * GET /products/user/{userId}
     */
    @Operation(summary = "Listar productos por usuario", description = "Devuelve los productos pertenecientes a un usuario específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de productos del usuario"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/user/{userId}")
    public List<ProductResponseDto> findByUserId(@PathVariable Long userId) {
        return service.findByUserId(userId);
    }

    /*
     * GET /products/category/{categoryId}
     */
    @Operation(summary = "Listar productos por categoría", description = "Devuelve los productos pertenecientes a una categoría específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de productos de la categoría"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/category/{categoryId}")
    public List<ProductResponseDto> findByCategoryId(@PathVariable Long categoryId) {
        return service.findByCategoryId(categoryId);
    }

    @Operation(summary = "Crear producto", description = """
            Crea un producto asociado al usuario autenticado.

            El cliente no debe enviar userId.
            El owner se obtiene desde el JWT mediante @AuthenticationPrincipal.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "409", description = "Nombre de producto ya registrado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto create(
            @Valid @RequestBody CreateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return service.create(dto, currentUser);
    }

    @Operation(summary = "Actualizar producto", description = """
            Actualiza completamente un producto.

            Reglas:
            - ROLE_USER solo puede actualizar productos propios.
            - ROLE_ADMIN puede actualizar cualquier producto.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "El usuario no es propietario del producto"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}")
    public ProductResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return service.update(id, dto, currentUser);
    }

    @Operation(summary = "Actualizar producto parcialmente", description = """
            Actualiza uno o varios campos de un producto (PATCH).

            Reglas:
            - ROLE_USER solo puede actualizar productos propios.
            - ROLE_ADMIN puede actualizar cualquier producto.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "El usuario no es propietario del producto"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}")
    public ProductResponseDto partialUpdate(
            @PathVariable Long id,
            @Valid @RequestBody PartialUpdateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return service.partialUpdate(id, dto, currentUser);
    }

    @Operation(summary = "Eliminar producto", description = """
            Elimina lógicamente un producto.

            Reglas:
            - ROLE_USER solo puede eliminar productos propios.
            - ROLE_ADMIN puede eliminar cualquier producto.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "El usuario no es propietario del producto"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        service.delete(id, currentUser);
    }

    /*
     * GET /products/page
     * GET /products/page?page=0&size=5&sortBy=price&direction=desc
     */
    @Operation(summary = "Listar productos con Page", description = """
            Devuelve productos activos usando Page.

            Incluye metadatos como:
            - totalElements
            - totalPages
            - number
            - size
            - first
            - last
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de productos obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/page")
    public Page<ProductResponseDto> findAllPage(@Valid @ModelAttribute PaginationDto pagination) {
        return service.findAllPage(pagination);
    }

    /*
     * GET /products/slice
     * GET /products/slice?page=0&size=5&sortBy=createdAt&direction=desc
     */
    @Operation(summary = "Listar productos con Slice", description = """
            Devuelve productos activos usando Slice.

            No calcula totalElements ni totalPages.
            Es útil para navegación simple o scroll infinito.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slice de productos obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/slice")
    public Slice<ProductResponseDto> findAllSlice(
            @Valid @ModelAttribute PaginationDto pagination,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return service.findAllSlice(pagination, currentUser);
    }

}