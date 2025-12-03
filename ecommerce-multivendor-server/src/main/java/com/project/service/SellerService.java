package com.project.service;

import com.project.domain.AccountStatus;
import com.project.exception.SellerException;
import com.project.model.Seller;

import java.util.List;

public interface SellerService {

    Seller getSellerProfile(String jwt) throws Exception;

    Seller createSeller(Seller seller) throws Exception;

    Seller getSellerById(Long id) throws SellerException;

    Seller getSellerByEmail(String email) throws Exception;

    List<Seller> getAllSellers(AccountStatus status);

    Seller updateSeller(Long id, Seller seller) throws Exception;

    void deleteSeller(Long id) throws Exception;

    Seller verifyEmail(String email, String otp) throws Exception;

    Seller updateSellerAccountStatus(Long id, AccountStatus status) throws Exception;
}
