package AM.PM.Homepage.common.controller;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_ops")
@RequiredArgsConstructor
public class OpsController {

    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/redis/ping")
    public String ping() {
        stringRedisTemplate.opsForValue().set("ops:ping", "ok", Duration.ofMinutes(1));
        return String.valueOf(stringRedisTemplate.opsForValue().get("ops:ping"));
    }
}
