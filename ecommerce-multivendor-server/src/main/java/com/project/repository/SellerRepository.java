package com.project.repository;

import com.project.domain.AccountStatus;
import com.project.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Seller findByEmail(String actualUsername);

    List<Seller> findByAccountStatus(AccountStatus status);
}
