package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.controller.dto.AnaliseResumoDTO;
import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnaliseRepository extends JpaRepository<Analise, Long> {

    @EntityGraph(attributePaths = "itens")
    @Query("""
            select a
            from Analise a
            where a.id = :id
            """)
    Optional<Analise> buscarDetalhadaPorId(Long id);

    @EntityGraph(attributePaths = "itens")
    @Query("""
            select a
            from Analise a
            where a.id = :id
              and a.empresa = :empresa
            """)
    Optional<Analise> buscarDetalhadaPorIdEEmpresa(@Param("id") Long id, @Param("empresa") Empresa empresa);

    @Query("select count(a) from Analise a")
    long countTotal();

    @Query("select count(a) from Analise a where a.empresa = :empresa")
    long countByEmpresa(@Param("empresa") Empresa empresa);

    @Query("select count(a) from Analise a where a.resultado = 'ATENDE'")
    long countAtenderam();

    @Query("select count(a) from Analise a where a.empresa = :empresa and a.resultado = 'ATENDE'")
    long countAtenderamByEmpresa(@Param("empresa") Empresa empresa);

    @Query("select count(a) from Analise a where a.resultado = 'NAO_ATENDE'")
    long countNaoAtenderam();

    @Query("select count(a) from Analise a where a.empresa = :empresa and a.resultado = 'NAO_ATENDE'")
    long countNaoAtenderamByEmpresa(@Param("empresa") Empresa empresa);

    @Query("select avg(a.cobertura) from Analise a where a.cobertura is not null")
    java.math.BigDecimal avgCobertura();

    @Query("select avg(a.cobertura) from Analise a where a.empresa = :empresa and a.cobertura is not null")
    java.math.BigDecimal avgCoberturaByEmpresa(@Param("empresa") Empresa empresa);

    @Query("""
            select new br.com.softwareprisma.licitacao.controller.dto.AnaliseResumoDTO(
                a.id, a.area, a.dataCriacao, a.resultado, a.cobertura
            )
            from Analise a
            order by a.dataCriacao desc
            """)
    Page<AnaliseResumoDTO> findRecentes(Pageable pageable);

    @Query("""
            select new br.com.softwareprisma.licitacao.controller.dto.AnaliseResumoDTO(
                a.id, a.area, a.dataCriacao, a.resultado, a.cobertura
            )
            from Analise a
            where a.empresa = :empresa
            order by a.dataCriacao desc
            """)
    Page<AnaliseResumoDTO> findRecentesByEmpresa(@Param("empresa") Empresa empresa, Pageable pageable);

    @Query("""
            select a
            from Analise a
            where (:area is null or a.area = :area)
            and (:resultado is null or a.resultado = :resultado)
            order by a.dataCriacao desc
            """)
    Page<Analise> buscarComFiltros(Area area, ResultadoAnalise resultado, Pageable pageable);

    @Query("""
            select a
            from Analise a
            where a.empresa = :empresa
              and (:area is null or a.area = :area)
              and (:resultado is null or a.resultado = :resultado)
            order by a.dataCriacao desc
            """)
    Page<Analise> buscarComFiltrosEEmpresa(@Param("empresa") Empresa empresa, Area area, ResultadoAnalise resultado, Pageable pageable);

    @Query("""
            select distinct a
            from Analise a
            left join fetch a.itens
            where a.id in :ids
            """)
    List<Analise> buscarComItensPorIds(List<Long> ids);

    @Query("""
            select distinct a
            from Analise a
            left join fetch a.itens
            where a.id in :ids
              and a.empresa = :empresa
            """)
    List<Analise> buscarComItensPorIdsEEmpresa(@Param("ids") List<Long> ids, @Param("empresa") Empresa empresa);
}
