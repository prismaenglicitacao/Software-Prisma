package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Cat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
            where c.id = :id
            """)
    Optional<Cat> buscarDetalhadaPorId(Long id);

    @Query("select count(c) from Cat c")
    long countTotal();

    @Query("select count(c) from Cat c where c.engenheiro.area = :area")
    long countByArea(br.com.softwareprisma.licitacao.domain.enums.Area area);
}
