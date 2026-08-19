package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.UsuarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioEmpresaRepository extends JpaRepository<UsuarioEmpresa, Long> {

    List<UsuarioEmpresa> findByUsuarioAndAtivoTrueOrderByEmpresaNomeAsc(Usuario usuario);

    Optional<UsuarioEmpresa> findByUsuarioAndEmpresaAndAtivoTrue(Usuario usuario, Empresa empresa);

    Optional<UsuarioEmpresa> findByUsuarioAndEmpresa(Usuario usuario, Empresa empresa);

    boolean existsByUsuarioAndEmpresaAndAtivoTrue(Usuario usuario, Empresa empresa);

    List<UsuarioEmpresa> findByEmpresaAndAtivoTrueOrderByUsuarioNomeAsc(Empresa empresa);

    void deleteByUsuarioAndEmpresa(Usuario usuario, Empresa empresa);
}
