package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AnaliseItemRepository extends JpaRepository<AnaliseItem, Long> {

    @Query("""
            select i
            from AnaliseItem i
            join fetch i.analise
            where i.id = :id
            """)
    Optional<AnaliseItem> buscarDetalhadoPorId(Long id);
}
