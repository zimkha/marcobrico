package com.mmd.marcobrico.repository;

import com.mmd.marcobrico.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

}
