package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

    Optional<LoginAttempt> findByUsername(String username);

    void deleteByUsername(String username);
}
