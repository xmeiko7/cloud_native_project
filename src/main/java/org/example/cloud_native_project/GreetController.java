package org.example.cloud_native_project;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class GreetController {


    @Resource
    private RedisTemplate<String,String> redisTemplate;
    private final Counter requestCounter;

    public GreetController(MeterRegistry meterRegistry) {
        this.requestCounter = Counter.builder("api_requests_total")
                .description("Total number of requests to your API")
                .register(meterRegistry);
    }
    @GetMapping("/greet")
    public ResponseEntity<Map<String, String>>  limitedEndpoint() {

        requestCounter.increment();

        Long increment = redisTemplate.opsForValue().increment("global");
        redisTemplate.expire("global",1, TimeUnit.SECONDS);

        if (increment <= 100) {
            return ResponseEntity.ok(Collections.singletonMap("msg", "hello"));
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}
