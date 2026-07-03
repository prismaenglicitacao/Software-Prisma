package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.controller.dto.AnaliseResumoDTO;
import br.com.softwareprisma.licitacao.domain.Analise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("select count(a) from Analise a")
    long countTotal();

    @Query("select count(a) from Analise a where a.resultado = 'ATENDE'")
    long countAtenderam();

    @Query("select count(a) from Analise a where a.resultado = 'NAO_ATENDE'")
    long countNaoAtenderam();

    @Query("select avg(a.cobertura) from Analise a where a.cobertura is not null")
    java.math.BigDecimal avgCobertura();

    @Query("""
            select new br.com.softwareprisma.licitacao.controller.dto.AnaliseResumoDTO(
                a.id, a.area, a.dataCriacao, a.resultado, a.cobertura
            )
            from Analise a
            order by a.dataCriacao desc
            """)
    Page<AnaliseResumoDTO> findRecentes(Pageable pageable);
}
