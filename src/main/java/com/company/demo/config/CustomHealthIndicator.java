package com.company.demo.config;

//import org.springframework.boot.actuate.health.Health;
//import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public CustomHealthIndicator(JdbcTemplate jdbcTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        boolean dbUp = checkDatabase();
        boolean redisUp = checkRedis();

        if (dbUp && redisUp) {
            return Health.up()
                    .withDetail("database", "connected")
                    .withDetail("redis", "connected")
                    .build();
        } else if (!dbUp && !redisUp) {
            return Health.down()
                    .withDetail("database", "disconnected")
                    .withDetail("redis", "disconnected")
                    .build();
        } else if (!dbUp) {
            return Health.down()
                    .withDetail("database", "disconnected")
                    .withDetail("redis", "connected")
                    .build();
        } else {
            return Health.down()
                    .withDetail("database", "connected")
                    .withDetail("redis", "disconnected")
                    .build();
        }
    }

    private boolean checkDatabase() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            return "PONG".equals(redisTemplate.getConnectionFactory().getConnection().ping());
        } catch (Exception e) {
            return false;
        }
    }
}