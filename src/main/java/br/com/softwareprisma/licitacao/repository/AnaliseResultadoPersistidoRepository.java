package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.AnaliseResultadoPersistido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AnaliseResultadoPersistidoRepository extends JpaRepository<AnaliseResultadoPersistido, Long> {
    
    Optional<AnaliseResultadoPersistido> findByAnaliseId(Long analiseId);
    
    boolean existsByAnaliseId(Long analiseId);
    
    @Modifying
    @Transactional
    void deleteByAnaliseId(Long analiseId);
}
