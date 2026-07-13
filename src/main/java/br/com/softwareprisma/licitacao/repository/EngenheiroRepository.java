package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Engenheiro;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EngenheiroRepository extends JpaRepository<Engenheiro, Long> {

    @EntityGraph(attributePaths = "cats")
    List<Engenheiro> findAllByOrderByNomeAsc();

    @Query("select count(e) from Engenheiro e")
    long countTotal();

    @Query("""
        SELECT new br.com.softwareprisma.licitacao.repository.projection.EngenheiroSearchProjection(
            e.id, e.nome, e.area, SIZE(e.cats)
        )
        FROM Engenheiro e
        WHERE LOWER(unaccent(e.nome)) LIKE LOWER(unaccent(CONCAT('%', :termo, '%')))
        ORDER BY e.nome ASC
        LIMIT 5
        """)
    List<br.com.softwareprisma.licitacao.repository.projection.EngenheiroSearchProjection> pesquisarPorNome(@Param("termo") String termo);
}
