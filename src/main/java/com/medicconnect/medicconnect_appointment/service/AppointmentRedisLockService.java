package com.medicconnect.medicconnect_appointment.service;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppointmentRedisLockService {

    private final StringRedisTemplate redisTemplate;

    public AppointmentRedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String buildKey(Long doctorId, LocalDate date, Long slotNo) {
        return "appointment:create:" + doctorId + ":" + date + ":" + slotNo;
    }

    public boolean tryLock(String key) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue()
                        .setIfAbsent(key, "PENDING", Duration.ofSeconds(60))
        );
    }

    public void markBooked(String key) {
        redisTemplate.opsForValue()
                .set(key, "BOOKED", Duration.ofMinutes(10));
    }

    public void release(String key) {
        redisTemplate.delete(key);
    }

    public Set<Long> getLockedSlots(Long doctorId, LocalDate date) {

        String pattern = "lock:doctor:" + doctorId + ":" + date + ":slot:*";

        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null) return Set.of();

        return keys.stream()
                .map(key -> {
                    String[] parts = key.split(":");
                    return Long.valueOf(parts[parts.length - 1]);
                })
                .collect(Collectors.toSet());
    }

}

