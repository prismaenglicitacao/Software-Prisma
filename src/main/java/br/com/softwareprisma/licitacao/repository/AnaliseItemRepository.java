package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
            select i.descricao, i.unidade, sum(i.quantidade)
            from AnaliseItem i
            join i.analise a
            where (:area is null or a.area = :area)
            group by i.descricao, i.unidade
            order by max(a.dataCriacao) desc
            """)
    List<Object[]> buscarDescricoesRecentesUtilizadas(br.com.softwareprisma.licitacao.domain.enums.Area area);
}
