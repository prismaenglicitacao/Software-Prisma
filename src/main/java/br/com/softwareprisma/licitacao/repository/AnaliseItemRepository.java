package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import br.com.softwareprisma.licitacao.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnaliseItemRepository extends JpaRepository<AnaliseItem, Long> {

    @Query("""
            select i
            from AnaliseItem i
            join fetch i.analise
            where i.id = :id
            """)
    Optional<AnaliseItem> buscarDetalhadoPorId(Long id);

    @Query("""
            select i
            from AnaliseItem i
            join fetch i.analise a
            where i.id = :id
              and a.empresa = :empresa
            """)
    Optional<AnaliseItem> buscarDetalhadoPorIdEEmpresa(@Param("id") Long id, @Param("empresa") Empresa empresa);

    @Query("""
            select ai
            from AnaliseItem ai
            join fetch ai.analise a
            where (:area is null or a.area = :area)
            order by a.dataCriacao desc
            """)
    List<AnaliseItem> buscarRecentes(br.com.softwareprisma.licitacao.domain.enums.Area area);

    @Query("""
            select ai
            from AnaliseItem ai
            join fetch ai.analise a
            where a.empresa = :empresa
              and (:area is null or a.area = :area)
            order by a.dataCriacao desc
            """)
    List<AnaliseItem> buscarRecentesPorEmpresa(@Param("area") br.com.softwareprisma.licitacao.domain.enums.Area area,
                                               @Param("empresa") Empresa empresa);

    List<AnaliseItem> findByAnalise(Analise analise);

    @Deprecated
    @Query("""
            select ai.descricao, ai.unidade, coalesce(sum(ci.quantidade), 0)
            from AnaliseItem ai
            join ai.analise a
            left join CatItem ci on ai.descricao = ci.descricao and ai.unidade = ci.unidade
            where (:area is null or a.area = :area)
            group by ai.descricao, ai.unidade
            order by max(a.dataCriacao) desc
            """)
    List<Object[]> buscarDescricoesRecentesUtilizadas(br.com.softwareprisma.licitacao.domain.enums.Area area);
}
