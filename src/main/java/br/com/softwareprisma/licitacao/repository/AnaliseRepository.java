package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Analise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AnaliseRepository extends JpaRepository<Analise, Long> {

    @EntityGraph(attributePaths = "itens")
    @Query("""
            select a
            from Analise a
            where a.id = :id
            """)
    Optional<Analise> buscarDetalhadaPorId(Long id);
}
