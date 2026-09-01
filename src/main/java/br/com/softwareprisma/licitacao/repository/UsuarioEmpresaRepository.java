package br.com.softwareprisma.licitacao.repository;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Usuario;
import br.com.softwareprisma.licitacao.domain.UsuarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioEmpresaRepository extends JpaRepository<UsuarioEmpresa, Long> {

    List<UsuarioEmpresa> findByUsuarioAndAtivoTrueOrderByEmpresaNomeAsc(Usuario usuario);

    @Query("""
            select ue
            from UsuarioEmpresa ue
            join fetch ue.empresa e
            where ue.usuario = :usuario
              and ue.ativo = true
            order by e.nome asc
            """)
    List<UsuarioEmpresa> findByUsuarioAtivoTrueComEmpresa(Usuario usuario);

    Optional<UsuarioEmpresa> findByUsuarioAndEmpresaAndAtivoTrue(Usuario usuario, Empresa empresa);

    Optional<UsuarioEmpresa> findByUsuarioAndEmpresa(Usuario usuario, Empresa empresa);

    boolean existsByUsuarioAndEmpresaAndAtivoTrue(Usuario usuario, Empresa empresa);

    boolean existsByUsuarioAndEmpresaIdAndAtivoTrue(Usuario usuario, Long empresaId);

    List<UsuarioEmpresa> findByEmpresaAndAtivoTrueOrderByUsuarioNomeAsc(Empresa empresa);

    @Query("""
            select ue
            from UsuarioEmpresa ue
            join fetch ue.usuario u
            join fetch ue.empresa e
            where ue.empresa = :empresa
              and ue.ativo = true
            order by u.nome asc
            """)
    List<UsuarioEmpresa> findByEmpresaAtivoTrueComUsuario(Empresa empresa);

    void deleteByUsuarioAndEmpresa(Usuario usuario, Empresa empresa);
}
