package com.project.service.impl;

import com.project.config.JwtProvider;
import com.project.domain.USER_ROLE;
import com.project.model.Cart;
import com.project.model.User;
import com.project.repository.CartRepository;
import com.project.repository.UserRepository;
import com.project.request.SignupRequest;
import com.project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public String createUser(SignupRequest request) {
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
}
