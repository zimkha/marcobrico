package com.mmd.marcobrico.repository;

import com.mmd.marcobrico.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByReference(String reference);

    boolean existsByReference(String reference);

}