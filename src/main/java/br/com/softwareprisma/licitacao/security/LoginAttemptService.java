package br.com.softwareprisma.licitacao.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_MINUTES = 15;

    private final Map<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String login) {
        attemptsCache.remove(login);
    }

    public void loginFailed(String login) {
        LoginAttempt attempt = attemptsCache.computeIfAbsent(login, k -> new LoginAttempt());
        attempt.incrementAttempts();
        attempt.setLastAttempt(LocalDateTime.now());
    }

    public boolean isLocked(String login) {
        LoginAttempt attempt = attemptsCache.get(login);
        if (attempt == null) {
            return false;
        }

        if (attempt.getAttempts() >= MAX_ATTEMPTS) {
            LocalDateTime lockTime = attempt.getLastAttempt();
            LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_TIME_MINUTES);
            
            if (LocalDateTime.now().isBefore(unlockTime)) {
                return true;
            } else {
                attemptsCache.remove(login);
                return false;
            }
        }
        return false;
    }

    public long getRemainingLockTimeMinutes(String login) {
        LoginAttempt attempt = attemptsCache.get(login);
        if (attempt == null || attempt.getAttempts() < MAX_ATTEMPTS) {
            return 0;
        }

        LocalDateTime lockTime = attempt.getLastAttempt();
        LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_TIME_MINUTES);
        long remainingMinutes = java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
        return Math.max(0, remainingMinutes);
    }

    private static class LoginAttempt {
        private int attempts = 0;
        private LocalDateTime lastAttempt;

        public void incrementAttempts() {
            this.attempts++;
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getLastAttempt() {
            return lastAttempt;
        }

        public void setLastAttempt(LocalDateTime lastAttempt) {
            this.lastAttempt = lastAttempt;
        }
    }
}
