package com.example.warehouseflowmanager.product.repository;

import com.example.warehouseflowmanager.product.entity.Product;
import com.example.warehouseflowmanager.product.entity.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByStorageLocationId(Long storageLocationId);

    @EntityGraph(attributePaths = "storageLocation")
    @Query(
            value = """
                    select p
                    from Product p
                    left join p.storageLocation sl
                    where (:status is null or p.status = :status)
                      and (:storageLocationId is null or sl.id = :storageLocationId)
                      and (
                          :search = ''
                          or lower(p.name) like lower(concat('%', :search, '%'))
                          or lower(p.sku) like lower(concat('%', :search, '%'))
                      )
                    """,
            countQuery = """
                    select count(p)
                    from Product p
                    left join p.storageLocation sl
                    where (:status is null or p.status = :status)
                      and (:storageLocationId is null or sl.id = :storageLocationId)
                      and (
                          :search = ''
                          or lower(p.name) like lower(concat('%', :search, '%'))
                          or lower(p.sku) like lower(concat('%', :search, '%'))
                      )
                    """
    )
    Page<Product> findAllWithFilters(
            @Param("status") ProductStatus status,
            @Param("storageLocationId") Long storageLocationId,
            @Param("search") String search,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "storageLocation")
    @Query("""
           select p
           from Product p
           left join p.storageLocation sl
           where sl.id = :storageLocationId
           order by p.name asc, p.id asc
           """)
    List<Product> findAllByStorageLocationIdWithStorageLocation(@Param("storageLocationId") Long storageLocationId);

    @EntityGraph(attributePaths = "storageLocation")
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findWithStorageLocationById(@Param("id") Long id);

    @EntityGraph(attributePaths = "storageLocation")
    @Query("""
           select p
           from Product p
           where p.status = :status
             and p.quantity <= coalesce(p.minimumQuantity, 0)
           order by p.quantity asc, p.name asc
           """)
    List<Product> findLowStockProducts(@Param("status") ProductStatus status);
}