package br.com.softwareprisma.licitacao.security;

import br.com.softwareprisma.licitacao.repository.LoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class LoginAttemptServiceTest {

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        loginAttemptRepository.deleteByUsername(TEST_USERNAME);
    }

    @Test
    void testCenario1_umaTentativaFalha_naoBloqueado() {
        loginAttemptService.loginFailed(TEST_USERNAME);
        
        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
        assertEquals(0, loginAttemptService.getRemainingLockTimeMinutes(TEST_USERNAME));
    }

    @Test
    void testCenario2_quatroTentativas_naoBloqueado() {
        for (int i = 0; i < 4; i++) {
            loginAttemptService.loginFailed(TEST_USERNAME);
        }
        
        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
        assertEquals(0, loginAttemptService.getRemainingLockTimeMinutes(TEST_USERNAME));
    }

    @Test
    void testCenario3_cincoTentativas_bloqueado() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(TEST_USERNAME);
        }
        
        assertTrue(loginAttemptService.isLocked(TEST_USERNAME));
        assertTrue(loginAttemptService.getRemainingLockTimeMinutes(TEST_USERNAME) > 0);
    }

    @Test
    void testCenario4_apos15Minutos_naoBloqueado() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(TEST_USERNAME);
        }
        
        assertTrue(loginAttemptService.isLocked(TEST_USERNAME));
        
        loginAttemptRepository.deleteByUsername(TEST_USERNAME);
        
        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
    }

    @Test
    void testCenario5_loginBemSucedido_registroRemovido() {
        for (int i = 0; i < 3; i++) {
            loginAttemptService.loginFailed(TEST_USERNAME);
        }
        
        assertTrue(loginAttemptRepository.findByUsername(TEST_USERNAME).isPresent());
        
        loginAttemptService.loginSucceeded(TEST_USERNAME);
        
        assertFalse(loginAttemptRepository.findByUsername(TEST_USERNAME).isPresent());
        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
    }

    @Test
    void testCenario6_persistencia_bloqueioPersisteAposRestart() {
        String username = "persistencetest";
        
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(username);
        }
        
        assertTrue(loginAttemptService.isLocked(username));
        
        loginAttemptRepository.deleteByUsername(username);
    }

    @Test
    void testCenario7_usuariosDiferentes_bloqueioIndependente() {
        String usuarioA = "usuarioA";
        String usuarioB = "usuarioB";
        
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(usuarioA);
        }
        
        assertTrue(loginAttemptService.isLocked(usuarioA));
        assertFalse(loginAttemptService.isLocked(usuarioB));
        
        loginAttemptRepository.deleteByUsername(usuarioA);
    }
}
