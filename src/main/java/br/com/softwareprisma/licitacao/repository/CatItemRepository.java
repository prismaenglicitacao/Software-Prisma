package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.CatItem;
import br.com.softwareprisma.licitacao.repository.projection.ItemSearchProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("""
            select distinct i.descricao, i.unidade
            from CatItem i
            where (:area is null or i.cat.engenheiro.area = :area)
              and lower(i.descricao) like lower(concat('%', :termo, '%'))
            order by i.descricao
            """)
    List<Object[]> buscarDescricoesPorTermo(String termo,
                                            br.com.softwareprisma.licitacao.domain.enums.Area area);

    @Query("""
            select i.descricao, i.unidade
            from CatItem i
            where (:area is null or i.cat.engenheiro.area = :area)
            group by i.descricao, i.unidade
            order by max(i.id) desc
            """)
    List<Object[]> buscarDescricoesRecentes(br.com.softwareprisma.licitacao.domain.enums.Area area);

    @Query("""
            select distinct i.descricao, i.unidade
            from CatItem i
            where lower(i.descricao) like lower(concat('%', :termo, '%'))
            order by i.descricao
            """)
    List<Object[]> buscarDescricoesPorTermo(String termo);

    @Query("""
            select i.descricao, i.unidade, sum(i.quantidade)
            from CatItem i
            where (:area is null or i.cat.engenheiro.area = :area)
              and lower(i.descricao) like lower(concat('%', :termo, '%'))
            group by i.descricao, i.unidade
            order by 
                case when lower(i.descricao) like lower(concat(:termo, '%')) then 0 else 1 end,
                i.descricao
            """)
    List<Object[]> buscarDescricoesPorTermoComQuantidade(String termo,
                                                         br.com.softwareprisma.licitacao.domain.enums.Area area);

    @Query("""
            select i.descricao, i.unidade, sum(i.quantidade)
            from CatItem i
            where lower(i.descricao) like lower(concat('%', :termo, '%'))
            group by i.descricao, i.unidade
            order by 
                case when lower(i.descricao) like lower(concat(:termo, '%')) then 0 else 1 end,
                i.descricao
            """)
    List<Object[]> buscarDescricoesPorTermoComQuantidade(String termo);

    @Query("""
            select i.descricao, i.unidade, sum(i.quantidade)
            from CatItem i
            where (:area is null or i.cat.engenheiro.area = :area)
            group by i.descricao, i.unidade
            order by max(i.id) desc
            """)
    List<Object[]> buscarDescricoesRecentesComQuantidade(br.com.softwareprisma.licitacao.domain.enums.Area area);

    @Query("select count(i) from CatItem i")
    long countTotal();

    @Query("select count(i) from CatItem i where i.cat.engenheiro.area = :area")
    long countByArea(br.com.softwareprisma.licitacao.domain.enums.Area area);

   @Query("""
    SELECT new br.com.softwareprisma.licitacao.repository.projection.ItemSearchProjection(
        i.id, c.id, i.descricao, i.unidade, c.nome, e.nome, e.area
    )
    FROM CatItem i
    JOIN i.cat c
    JOIN c.engenheiro e
    WHERE LOWER(i.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))
    ORDER BY i.descricao
    """)
List<ItemSearchProjection> pesquisarPorDescricao(
        @Param("termo") String termo,
        Pageable pageable);

    @Query("SELECT ci.cat.id, COUNT(ci) FROM CatItem ci WHERE ci.cat.id IN :catIds GROUP BY ci.cat.id")
    List<Object[]> contarItensPorCatIds(@Param("catIds") List<Long> catIds);

    @Query("SELECT i FROM CatItem i WHERE i.cat.id = :catId ORDER BY i.id ASC")
    Page<CatItem> findByCatId(@Param("catId") Long catId, Pageable pageable);

    @Query("SELECT i FROM CatItem i WHERE i.cat.id = :catId AND LOWER(i.descricao) LIKE LOWER(CONCAT('%', :descricao, '%')) ORDER BY i.id ASC")
    Page<CatItem> findByCatIdAndDescricaoContainingIgnoreCase(@Param("catId") Long catId, @Param("descricao") String descricao, Pageable pageable);

    @Query("""
            select i
            from CatItem i
            join fetch i.cat c
            join fetch c.engenheiro e
            where (:area is null or e.area = :area)
              and lower(i.descricao) like lower(concat('%', :termo, '%'))
            order by i.descricao
            """)
    List<CatItem> buscarItensPorTermoParaAutocomplete(String termo, br.com.softwareprisma.licitacao.domain.enums.Area area);

    @Query("""
            select i
            from CatItem i
            join fetch i.cat c
            join fetch c.engenheiro e
            where lower(i.descricao) like lower(concat('%', :termo, '%'))
            order by i.descricao
            """)
    List<CatItem> buscarItensPorTermoParaAutocomplete(String termo);

    @Query("""
            select i
            from CatItem i
            join fetch i.cat c
            join fetch c.engenheiro e
            where (:area is null or e.area = :area)
            """)
    List<CatItem> buscarTodosPorArea(br.com.softwareprisma.licitacao.domain.enums.Area area);

    @Query("""
            select i
            from CatItem i
            join fetch i.cat c
            join fetch c.engenheiro e
            """)
    List<CatItem> buscarTodos();
}