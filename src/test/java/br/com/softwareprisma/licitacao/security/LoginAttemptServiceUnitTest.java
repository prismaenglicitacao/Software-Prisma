package br.com.softwareprisma.licitacao.security;

import br.com.softwareprisma.licitacao.domain.LoginAttempt;
import br.com.softwareprisma.licitacao.repository.LoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginAttemptServiceUnitTest {

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        reset(loginAttemptRepository);
    }

    @Test
    void testCenario1_umaTentativaFalha_naoBloqueado() {
        when(loginAttemptRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        
        loginAttemptService.loginFailed(TEST_USERNAME);
        
        verify(loginAttemptRepository).save(any(LoginAttempt.class));
        when(loginAttemptRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(new LoginAttempt(TEST_USERNAME)));
        
        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
    }

    @Test
    void testCenario2_quatroTentativas_naoBloqueado() {
        LoginAttempt attempt = new LoginAttempt(TEST_USERNAME);
        attempt.setAttempts(4);
        attempt.setLastAttempt(LocalDateTime.now());
        
        when(loginAttemptRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(attempt));
        
        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
    }

    @Test
    void testCenario3_cincoTentativas_bloqueado() {
        LoginAttempt attempt = new LoginAttempt(TEST_USERNAME);
        attempt.setAttempts(5);
        attempt.setLastAttempt(LocalDateTime.now());
        
        when(loginAttemptRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(attempt));
        
        assertTrue(loginAttemptService.isLocked(TEST_USERNAME));
        assertTrue(loginAttemptService.getRemainingLockTimeMinutes(TEST_USERNAME) > 0);
    }

    @Test
    void testCenario4_apos15Minutos_naoBloqueado() {
        LoginAttempt attempt = new LoginAttempt(TEST_USERNAME);
        attempt.setAttempts(5);
        attempt.setLastAttempt(LocalDateTime.now().minusMinutes(16));
        
        when(loginAttemptRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(attempt));
        
        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
        verify(loginAttemptRepository).deleteByUsername(TEST_USERNAME);
    }

    @Test
    void testCenario5_loginBemSucedido_registroRemovido() {
        loginAttemptService.loginSucceeded(TEST_USERNAME);
        
        verify(loginAttemptRepository).deleteByUsername(TEST_USERNAME);
    }

    @Test
    void testCenario6_persistencia_bloqueioPersisteAposRestart() {
        LoginAttempt attempt = new LoginAttempt(TEST_USERNAME);
        attempt.setAttempts(5);
        attempt.setLastAttempt(LocalDateTime.now());
        
        when(loginAttemptRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(attempt));
        
        assertTrue(loginAttemptService.isLocked(TEST_USERNAME));
    }

    @Test
    void testCenario7_usuariosDiferentes_bloqueioIndependente() {
        String usuarioA = "usuarioA";
        String usuarioB = "usuarioB";
        
        LoginAttempt attemptA = new LoginAttempt(usuarioA);
        attemptA.setAttempts(5);
        attemptA.setLastAttempt(LocalDateTime.now());
        
        when(loginAttemptRepository.findByUsername(usuarioA)).thenReturn(Optional.of(attemptA));
        when(loginAttemptRepository.findByUsername(usuarioB)).thenReturn(Optional.empty());
        
        assertTrue(loginAttemptService.isLocked(usuarioA));
        assertFalse(loginAttemptService.isLocked(usuarioB));
    }

    @Test
    void testCenario8_senhaCorretaDuranteBloqueio_naoLimpaBloqueio() {
        LoginAttempt attempt = new LoginAttempt(TEST_USERNAME);
        attempt.setAttempts(5);
        attempt.setLastAttempt(LocalDateTime.now());
        
        when(loginAttemptRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(attempt));
        
        assertTrue(loginAttemptService.isLocked(TEST_USERNAME));
        
        loginAttemptService.loginSucceeded(TEST_USERNAME);
        
        verify(loginAttemptRepository).deleteByUsername(TEST_USERNAME);
    }
}
