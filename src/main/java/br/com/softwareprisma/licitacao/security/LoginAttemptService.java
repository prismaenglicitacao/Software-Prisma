package br.com.softwareprisma.licitacao.security;

import br.com.softwareprisma.licitacao.domain.LoginAttempt;
import br.com.softwareprisma.licitacao.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_MINUTES = 15;

    private final LoginAttemptRepository loginAttemptRepository;

    @Transactional
    public void loginSucceeded(String login) {
        loginAttemptRepository.deleteByUsername(login);
    }

    @Transactional
    public void loginFailed(String login) {
        LoginAttempt attempt = loginAttemptRepository.findByUsername(login)
                .orElse(new LoginAttempt(login));
        
        attempt.setAttempts(attempt.getAttempts() + 1);
        attempt.setLastAttempt(LocalDateTime.now());
        
        loginAttemptRepository.save(attempt);
    }

    @Transactional(readOnly = true)
    public boolean isLocked(String login) {
        return loginAttemptRepository.findByUsername(login)
                .map(attempt -> {
                    if (attempt.getAttempts() >= MAX_ATTEMPTS) {
                        LocalDateTime lockTime = attempt.getLastAttempt();
                        LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_TIME_MINUTES);
                        
                        if (LocalDateTime.now().isBefore(unlockTime)) {
                            return true;
                        } else {
                            loginAttemptRepository.deleteByUsername(login);
                            return false;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public long getRemainingLockTimeMinutes(String login) {
        return loginAttemptRepository.findByUsername(login)
                .map(attempt -> {
                    if (attempt.getAttempts() < MAX_ATTEMPTS) {
                        return 0L;
                    }
                    
                    LocalDateTime lockTime = attempt.getLastAttempt();
                    LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_TIME_MINUTES);
                    long remainingMinutes = java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
                    return Math.max(0, remainingMinutes);
                })
                .orElse(0L);
    }
}
