package com.stockmanagement.authentication_service.service;

import com.stockmanagement.authentication_service.cache.AuthCacheHelper;
import com.stockmanagement.authentication_service.dto.*;
import com.stockmanagement.authentication_service.entity.RefreshToken;
import com.stockmanagement.authentication_service.entity.Role;
import com.stockmanagement.authentication_service.entity.RoleName;
import com.stockmanagement.authentication_service.entity.User;
import com.stockmanagement.authentication_service.exception.TokenRefreshException;
import com.stockmanagement.authentication_service.exception.UserAlreadyExistsException;
import com.stockmanagement.authentication_service.repository.RefreshTokenRepository;
import com.stockmanagement.authentication_service.repository.RoleRepository;
import com.stockmanagement.authentication_service.repository.UserRepository;
import com.stockmanagement.authentication_service.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthCacheHelper cacheHelper;
    
    @Value("${jwt.refresh-expiration}")
    private long refreshTokenDurationMs;
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            roles.add(userRole);
        } else {
            request.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(RoleName.valueOf(roleName))
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            });
        }
        
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        
        UserResponse userResponse = mapToUserResponse(savedUser);
        cacheHelper.cacheUser(userResponse);
        
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = createRefreshToken(savedUser);
        
        cacheHelper.cacheRefreshToken(refreshToken.getToken(), savedUser.getUsername(), refreshTokenDurationMs);
        
        return buildAuthResponse(savedUser, accessToken, refreshToken.getToken());
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (!cacheHelper.checkRateLimit(request.getUsername(), MAX_LOGIN_ATTEMPTS)) {
            throw new RuntimeException("Too many login attempts. Please try again later.");
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = tokenProvider.generateToken(authentication);
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = createRefreshToken(user);
        
        cacheHelper.cacheRefreshToken(refreshToken.getToken(), user.getUsername(), refreshTokenDurationMs);
        cacheHelper.resetRateLimit(request.getUsername());
        
        UserResponse userResponse = mapToUserResponse(user);
        cacheHelper.cacheUser(userResponse);
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }
    
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String cachedUsername = cacheHelper.getUsernameByRefreshToken(request.getRefreshToken());
        
        if (cachedUsername != null) {
            User user = userRepository.findByUsername(cachedUsername)
                    .orElseThrow(() -> new TokenRefreshException("User not found"));
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), null, user.getRoles().stream()
                            .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName().name()))
                            .collect(Collectors.toList())
            );
            
            String accessToken = tokenProvider.generateToken(authentication);
            
            log.info("Token refreshed for user: {}", user.getUsername());
            
            return buildAuthResponse(user, accessToken, request.getRefreshToken());
        }
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
        
        if (refreshToken.getRevoked()) {
            throw new TokenRefreshException("Refresh token is revoked");
        }
        
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token is expired");
        }
        
        User user = refreshToken.getUser();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName().name()))
                        .collect(Collectors.toList())
        );
        
        String accessToken = tokenProvider.generateToken(authentication);
        
        cacheHelper.cacheRefreshToken(refreshToken.getToken(), user.getUsername(), refreshTokenDurationMs);
        
        log.info("Token refreshed for user: {}", user.getUsername());
        
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }
    
    @Transactional
    public void logout(String username, String token) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        refreshTokenRepository.deleteByUser(user);
        
        long tokenExpiration = tokenProvider.getExpirationFromToken(token);
        cacheHelper.blacklistToken(token, tokenExpiration);
        cacheHelper.evictUser(username, user.getEmail(), user.getId());
        
        SecurityContextHolder.clearContext();
        
        log.info("User logged out: {}", username);
    }
    
    public UserResponse getCurrentUser(String username) {
        UserResponse cached = cacheHelper.getUserByUsername(username);
        if (cached != null) {
            log.debug("User found in cache: {}", username);
            return cached;
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        UserResponse response = mapToUserResponse(user);
        cacheHelper.cacheUser(response);
        
        return response;
    }
    
    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000));
        refreshToken.setRevoked(false);
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }
    
    private UserResponse mapToUserResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getActive(),
                roles
        );
    }
}