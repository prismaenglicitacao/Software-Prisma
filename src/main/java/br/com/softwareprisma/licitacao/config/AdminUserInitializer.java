package br.com.softwareprisma.licitacao.config;

import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            log.info("Nenhum usuário encontrado. Criando usuário administrador padrão...");
            
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setLogin("admin");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setAtivo(true);
            admin.setAdministrador(true);
            
            usuarioRepository.save(admin);
            log.info("Usuário administrador criado com sucesso. Login: admin, Senha: admin123");
        } else {
            log.info("Usuários já existem no banco de dados. Nenhum usuário padrão será criado.");
        }
    }
}
