package com.project.service.impl;

import com.project.config.JwtProvider;
import com.project.domain.USER_ROLE;
import com.project.model.Cart;
import com.project.model.Seller;
import com.project.model.User;
import com.project.model.VerificationCode;
import com.project.repository.CartRepository;
import com.project.repository.SellerRepository;
import com.project.repository.UserRepository;
import com.project.repository.VerificationCodeRepository;
import com.project.request.LoginRequest;
import com.project.request.SignupRequest;
import com.project.response.AuthResponse;
import com.project.service.AuthService;
import com.project.service.EmailService;
import com.project.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final SellerRepository sellerRepository;
    private final VerificationCodeRepository verificationCodeRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;
    private final CustomUserServiceImpl customUserService;

    @Override
    public String createUser(SignupRequest request) throws Exception {
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(request.getEmail());
        if (verificationCode == null || !verificationCode.getOtp().equals(request.getOtp())) {
            throw new Exception("Wrong OTP...");
        }

        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setFullName(request.getFullName());
            newUser.setRole(USER_ROLE.ROLE_CUSTOMER);
            newUser.setMobile("1234567789");
            newUser.setPassword(passwordEncoder.encode(request.getOtp()));
            user = userRepository.save(newUser);

            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(USER_ROLE.ROLE_CUSTOMER.toString()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(request.getEmail(), null,
                authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtProvider.generateToken(authentication);
    }

    @Override
    public void sentOtp(String email, USER_ROLE role) throws Exception {
        String SIGNING_PREFIX = "signin_";

        if (email.startsWith(SIGNING_PREFIX)) {
            email = email.substring(SIGNING_PREFIX.length());

            if (role.equals(USER_ROLE.ROLE_SELLER)) {
                Seller seller = sellerRepository.findByEmail(email);
                if (seller == null) {
                    throw new Exception("Seller not exists with provided email");
                }
            } else {
                User user = userRepository.findByEmail(email);
                if (user == null) {
                    throw new Exception("User not exists with provided email");
                }
            }
        }

        VerificationCode isExists = verificationCodeRepository.findByEmail(email);
        if (isExists != null) {
            verificationCodeRepository.delete(isExists);
        }
        String otp = OtpUtil.generateOtp();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);
        verificationCodeRepository.save(verificationCode);

        String subject = "Mega Bazar login/signup otp";
        String text = "Your login/signup OTP is - " + otp;
        emailService.sendVerificationOtpEmail(email, otp, subject, text);
    }

    @Override
    public AuthResponse signingIn(LoginRequest request) throws Exception {
        String username = request.getEmail();
        String otp = request.getOtp();

        Authentication authentication = authenticate(username, otp);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);
        AuthResponse response = new AuthResponse();
        response.setJwt(token);
        response.setMessage("Login successfully");

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleName = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();
        response.setRole(USER_ROLE.valueOf(roleName));
        return response;
    }

    private Authentication authenticate(String username, String otp) throws Exception {
        UserDetails userDetails = customUserService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new Exception("Invalid Credentials");
        }

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(userDetails.getUsername());
        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new BadCredentialsException("Invalid OTP");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
