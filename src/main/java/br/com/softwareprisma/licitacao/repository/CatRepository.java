package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.Empresa;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CatRepository extends JpaRepository<Cat, Long> {

    @EntityGraph(attributePaths = {"engenheiro", "itens"})
    @Query("""
            select c
            from Cat c
            order by c.nome
            """)
    List<Cat> listarTodasComEngenheiroEItens();

    @EntityGraph(attributePaths = {"engenheiro", "itens"})
    @Query("""
            select c
            from Cat c
            where c.engenheiro.empresa = :empresa
            order by c.nome
            """)
    List<Cat> listarPorEmpresa(@Param("empresa") Empresa empresa);

    @EntityGraph(attributePaths = {"engenheiro", "itens"})
    @Query("""
            select c
            from Cat c
            where c.id = :id
            """)
    Optional<Cat> buscarDetalhadaPorId(Long id);

    @EntityGraph(attributePaths = {"engenheiro", "itens"})
    @Query("""
            select c
            from Cat c
            where c.id = :id
              and c.engenheiro.empresa = :empresa
            """)
    Optional<Cat> buscarDetalhadaPorIdEEmpresa(@Param("id") Long id, @Param("empresa") Empresa empresa);

    @Query("select count(c) from Cat c")
    long countTotal();

    @Query("select count(c) from Cat c where c.engenheiro.empresa = :empresa")
    long countByEmpresa(@Param("empresa") Empresa empresa);

    @Query("select count(c) from Cat c where c.engenheiro.area = :area")
    long countByArea(br.com.softwareprisma.licitacao.domain.enums.Area area);

    @Query("select count(c) from Cat c where c.engenheiro.empresa = :empresa and c.engenheiro.area = :area")
    long countByEmpresaEEmpresa(@Param("empresa") Empresa empresa, br.com.softwareprisma.licitacao.domain.enums.Area area);

    @Query("""
        SELECT new br.com.softwareprisma.licitacao.repository.projection.CatSearchProjection(
            c.id, c.nome, e.nome, 0
        )
        FROM Cat c
        JOIN c.engenheiro e
       WHERE LOWER(c.nome)LIKE LOWER(CONCAT('%', :termo, '%'))
        ORDER BY c.nome ASC
        """)
    List<br.com.softwareprisma.licitacao.repository.projection.CatSearchProjection> pesquisarPorNomeOuNumero(@Param("termo") String termo, Pageable pageable);

    @Query("""
        SELECT new br.com.softwareprisma.licitacao.repository.projection.CatSearchProjection(
            c.id, c.nome, e.nome, 0
        )
        FROM Cat c
        JOIN c.engenheiro e
       WHERE e.empresa = :empresa
         AND LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
        ORDER BY c.nome ASC
        """)
    List<br.com.softwareprisma.licitacao.repository.projection.CatSearchProjection> pesquisarPorNomeOuNumeroEEmpresa(@Param("termo") String termo, @Param("empresa") Empresa empresa, Pageable pageable);
}
