package com.stockmanagement.authentication_service.cache;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.stockmanagement.authentication_service.dto.UserResponse;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthCacheHelper {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String USER_CACHE_PREFIX = "user:";
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";
    private static final String RATE_LIMIT_PREFIX = "rate:limit:";
    private static final long USER_TTL_HOURS = 24;
    private static final long TOKEN_TTL_HOURS = 24;
    private static final long RATE_LIMIT_TTL_SECONDS = 60;
    
    public void cacheUser(UserResponse user) {
        try {
            String usernameKey = USER_CACHE_PREFIX + "username:" + user.getUsername();
            String idKey = USER_CACHE_PREFIX + "id:" + user.getId();
            String emailKey = USER_CACHE_PREFIX + "email:" + user.getEmail();
            
            redisTemplate.opsForValue().set(usernameKey, user, USER_TTL_HOURS, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(idKey, user, USER_TTL_HOURS, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(emailKey, user, USER_TTL_HOURS, TimeUnit.HOURS);
            
            log.debug("Cached user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error caching user: {}", user.getUsername(), e);
        }
    }
    
    public UserResponse getUserByUsername(String username) {
        try {
            String key = USER_CACHE_PREFIX + "username:" + username;
            return (UserResponse) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting user by username from cache: {}", username, e);
            return null;
        }
    }
    
    public UserResponse getUserById(Long id) {
        try {
            String key = USER_CACHE_PREFIX + "id:" + id;
            return (UserResponse) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting user by id from cache: {}", id, e);
            return null;
        }
    }
    
    public void evictUser(String username, String email, Long id) {
        try {
            redisTemplate.delete(USER_CACHE_PREFIX + "username:" + username);
            redisTemplate.delete(USER_CACHE_PREFIX + "email:" + email);
            redisTemplate.delete(USER_CACHE_PREFIX + "id:" + id);
            log.debug("Evicted user: {}", username);
        } catch (Exception e) {
            log.error("Error evicting user: {}", username, e);
        }
    }
    
    public void blacklistToken(String token, long expirationMs) {
        try {
            String key = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "BLACKLISTED", expirationMs, TimeUnit.MILLISECONDS);
            log.debug("Token blacklisted");
        } catch (Exception e) {
            log.error("Error blacklisting token", e);
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        try {
            String key = TOKEN_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking token blacklist", e);
            return false;
        }
    }
    
    public void cacheRefreshToken(String token, String username, long expirationMs) {
        try {
            String key = REFRESH_TOKEN_PREFIX + token;
            redisTemplate.opsForValue().set(key, username, expirationMs, TimeUnit.MILLISECONDS);
            log.debug("Cached refresh token for user: {}", username);
        } catch (Exception e) {
            log.error("Error caching refresh token", e);
        }
    }
    
    public String getUsernameByRefreshToken(String token) {
        try {
            String key = REFRESH_TOKEN_PREFIX + token;
            return (String) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting username by refresh token", e);
            return null;
        }
    }
    
    public void evictRefreshToken(String token) {
        try {
            String key = REFRESH_TOKEN_PREFIX + token;
            redisTemplate.delete(key);
            log.debug("Evicted refresh token");
        } catch (Exception e) {
            log.error("Error evicting refresh token", e);
        }
    }
    
    public boolean checkRateLimit(String username, int maxRequests) {
        try {
            String key = RATE_LIMIT_PREFIX + username;
            Long currentCount = redisTemplate.opsForValue().increment(key);
            
            if (currentCount == null) {
                return false;
            }
            
            if (currentCount == 1) {
                redisTemplate.expire(key, RATE_LIMIT_TTL_SECONDS, TimeUnit.SECONDS);
            }
            
            if (currentCount > maxRequests) {
                log.warn("Rate limit exceeded for user: {}", username);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error checking rate limit for user: {}", username, e);
            return true;
        }
    }
    
    public void resetRateLimit(String username) {
        try {
            String key = RATE_LIMIT_PREFIX + username;
            redisTemplate.delete(key);
            log.debug("Reset rate limit for user: {}", username);
        } catch (Exception e) {
            log.error("Error resetting rate limit for user: {}", username, e);
        }
    }
}