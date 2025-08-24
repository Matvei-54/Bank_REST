package com.example.bankcards.service;

import com.example.bankcards.exception.ErrorValueIdempotencyKeyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class IdempotencyService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper;

    public boolean idempotencyKeyCheck(String idempotencyKey) {

        if (idempotencyKey.isBlank()) {
            throw new ErrorValueIdempotencyKeyException();
        }
        return redisTemplate.hasKey(idempotencyKey);

    }

    @Transactional(readOnly = true)
    public <T> T getResultByIdempotencyKey(String idempotencyKey, Class<T> clazz) {

        return mapper.convertValue(redisTemplate.opsForValue().get(idempotencyKey), clazz);
    }

    @Transactional
    public void saveIdempotencyKey(String idempotencyKey, Object resultMethod, long ttlSecond) {
        redisTemplate.opsForValue().set(idempotencyKey, resultMethod, ttlSecond, TimeUnit.SECONDS);
    }
}
