package com.mmd.marcobrico.repository;

import com.mmd.marcobrico.domain.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PartnerRepository extends JpaRepository<Partner, Long>, JpaSpecificationExecutor<Partner> {

    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
}
