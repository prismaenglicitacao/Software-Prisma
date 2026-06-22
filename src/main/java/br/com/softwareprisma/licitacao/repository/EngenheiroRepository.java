package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Engenheiro;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EngenheiroRepository extends JpaRepository<Engenheiro, Long> {

    @EntityGraph(attributePaths = "cats")
    List<Engenheiro> findAllByOrderByNomeAsc();
}
