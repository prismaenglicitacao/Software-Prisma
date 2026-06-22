package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.CatItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CatItemRepository extends JpaRepository<CatItem, Long> {

    @Query("""
            select i
            from CatItem i
            join fetch i.cat c
            join fetch c.engenheiro
            where i.id = :id
            """)
    Optional<CatItem> buscarDetalhadoPorId(Long id);
}
