package com.mmd.marcobrico.repository;

import com.mmd.marcobrico.domain.InventoryEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryEntry, Long> {

    List<InventoryEntry> findByProductIdOrderByCreatedAtDesc(Long productId);
    Page<InventoryEntry> findAll(Specification<InventoryEntry> spec, Pageable pageable);

}