package com.roadwarnings.narino.aspect;

import com.roadwarnings.narino.annotation.RateLimited;
import com.roadwarnings.narino.config.RateLimitingConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Aspecto para aplicar rate limiting específico basado en la anotación @RateLimited
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RateLimitingConfig rateLimitingConfig;

    @Around("@annotation(rateLimited)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String username = getAuthenticatedUsername();
        Bucket bucket = getBucketForType(username, rateLimited.value());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            log.debug("Rate limit check passed for user: {} (remaining: {})", username, probe.getRemainingTokens());
            return joinPoint.proceed();
        } else {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("Rate limit exceeded for user: {} on {}", username, rateLimited.value());

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    String.format("Has excedido el límite de %s. Intenta de nuevo en %d segundos.",
                            getActionName(rateLimited.value()), waitSeconds)
            );
        }
    }

    private Bucket getBucketForType(String username, RateLimited.Type type) {
        return switch (type) {
            case ALERT_CREATION -> rateLimitingConfig.resolveAlertCreationBucket(username);
            case COMMENT_CREATION -> rateLimitingConfig.resolveCommentCreationBucket(username);
            default -> rateLimitingConfig.resolveBucket(username);
        };
    }

    private String getActionName(RateLimited.Type type) {
        return switch (type) {
            case ALERT_CREATION -> "creación de alertas";
            case COMMENT_CREATION -> "creación de comentarios";
            default -> "solicitudes";
        };
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
