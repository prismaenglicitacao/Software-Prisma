package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.repository.projection.EngenheiroSearchProjection;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
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
   WHERE LOWER(e.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
    ORDER BY e.nome ASC
    """)
List<EngenheiroSearchProjection> pesquisarPorNome(
        @Param("termo") String termo,
        Pageable pageable);

    
   
}
