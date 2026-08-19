package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    List<Empresa> findByAtivoTrueOrderByNomeAsc();

    Optional<Empresa> findByIdAndAtivoTrue(Long id);

    Optional<Empresa> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);
}
