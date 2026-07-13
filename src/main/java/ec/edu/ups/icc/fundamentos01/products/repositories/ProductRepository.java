package ec.edu.ups.icc.fundamentos01.products.repositories;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

  Optional<ProductEntity> findByNameIgnoreCaseAndDeletedFalse(String name);

  List<ProductEntity> findByDeletedFalse();

  Optional<ProductEntity> findByIdAndDeletedFalse(Long id);

  // Busca productos activos de un usuario (propietario)
  List<ProductEntity> findByOwner_IdAndDeletedFalse(Long ownerId);

  boolean existsByIdAndOwner_IdAndDeletedFalse(Long id, Long ownerId);

  // Busca productos activos que contengan una categoría específica (relación
  // ManyToMany)
  List<ProductEntity> findByCategories_IdAndDeletedFalse(Long categoryId);

  // NOTA: Los métodos findByOwner_Id... y findByCategory_Id... ya no se usarán
  // porque ahora usamos las consultas dinámicas con filtros.

  /*
   * Busca productos activos de un usuario aplicando filtros opcionales.
   * Según la Sección 23, se elimina el filtro de categoryId para limpiar la
   * semántica.
   */
  @Query("""
      SELECT DISTINCT p
      FROM ProductEntity p
      WHERE p.deleted = false
        AND p.owner.id = :userId
        AND p.owner.deleted = false
        AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
      """)
  List<ProductEntity> findByOwnerIdWithFilters(
      @Param("userId") Long userId,
      @Param("name") String name,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice);

  /*
   * Busca productos activos de una categoría aplicando filtros opcionales.
   * SECCIÓN 18 y 18.1: Se utiliza JOIN con p.categories y DISTINCT para evitar
   * duplicados.
   */
  @Query("""
      SELECT DISTINCT p
      FROM ProductEntity p
      JOIN p.categories c
      WHERE p.deleted = false
        AND c.id = :categoryId
        AND c.deleted = false
        AND p.owner.deleted = false
        AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:userId IS NULL OR p.owner.id = :userId)
      """)
  List<ProductEntity> findByCategoryIdWithFilters(
      @Param("categoryId") Long categoryId,
      @Param("name") String name,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("userId") Long userId);

  /*
   * Consulta productos activos usando Page.
   * Page ejecuta consulta de datos y consulta COUNT.
   */
  @Query(value = """
      SELECT p
      FROM ProductEntity p
      WHERE p.deleted = false
      """, countQuery = """
      SELECT COUNT(p)
      FROM ProductEntity p
      WHERE p.deleted = false
      """)
  Page<ProductEntity> findActivePage(Pageable pageable);

  /*
   * Consulta productos activos usando Slice.
   * Slice no necesita total de registros.
   */
  @Query("""
      SELECT p
      FROM ProductEntity p
      WHERE p.deleted = false
      """)
  Slice<ProductEntity> findActiveSlice(Pageable pageable);

  /*
   * Versión paginada (Page) de productos por categoría con filtros.
   */
  @Query(value = """
      SELECT DISTINCT p
      FROM ProductEntity p
      JOIN p.categories c
      WHERE p.deleted = false
        AND c.id = :categoryId
        AND c.deleted = false
        AND p.owner.deleted = false
        AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:userId IS NULL OR p.owner.id = :userId)
      """, countQuery = """
      SELECT COUNT(DISTINCT p)
      FROM ProductEntity p
      JOIN p.categories c
      WHERE p.deleted = false
        AND c.id = :categoryId
        AND c.deleted = false
        AND p.owner.deleted = false
        AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:userId IS NULL OR p.owner.id = :userId)
      """)
  Page<ProductEntity> findByCategoryIdWithFiltersPage(
      @Param("categoryId") Long categoryId,
      @Param("name") String name,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("userId") Long userId,
      Pageable pageable);

  /*
   * Versión paginada (Slice) de productos por categoría con filtros.
   */
  @Query("""
      SELECT DISTINCT p
      FROM ProductEntity p
      JOIN p.categories c
      WHERE p.deleted = false
        AND c.id = :categoryId
        AND c.deleted = false
        AND p.owner.deleted = false
        AND (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%')))
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:userId IS NULL OR p.owner.id = :userId)
      """)
  Slice<ProductEntity> findByCategoryIdWithFiltersSlice(
      @Param("categoryId") Long categoryId,
      @Param("name") String name,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("userId") Long userId,
      Pageable pageable);

  /*
   * Consulta productos activos del usuario autenticado usando Slice.
   */
  @Query("""
      SELECT p
      FROM ProductEntity p
      WHERE p.deleted = false
        AND p.owner.id = :ownerId
      """)
  Slice<ProductEntity> findActiveSliceByOwner(@Param("ownerId") Long ownerId, Pageable pageable);
}