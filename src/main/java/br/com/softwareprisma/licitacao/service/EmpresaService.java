package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Empresa> listarAtivas() {
        return empresaRepository.findByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Empresa não encontrada"));
    }

    @Transactional(readOnly = true)
    public Empresa buscarAtivaPorId(Long id) {
        return empresaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Empresa não encontrada ou inativa"));
    }

    @Transactional
    public Empresa salvar(Empresa empresa) {
        if (empresa.getCnpj() != null && empresaRepository.existsByCnpj(empresa.getCnpj())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "CNPJ já cadastrado");
        }
        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa atualizar(Long id, Empresa formulario) {
        Empresa empresa = buscarPorId(id);
        empresa.setNome(formulario.getNome());

        String cnpjAtual = empresa.getCnpj();
        String cnpjNovo = formulario.getCnpj();

        // Caso: null -> null (não fazer nada)
        if (cnpjAtual == null && cnpjNovo == null) {
            // CNPJ continua null
        }
        // Caso: null -> valor (verificar duplicidade e salvar)
        else if (cnpjAtual == null && cnpjNovo != null) {
            if (empresaRepository.existsByCnpj(cnpjNovo)) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "CNPJ já cadastrado");
            }
            empresa.setCnpj(cnpjNovo);
        }
        // Caso: valor -> valor (mesmo valor, não fazer nada)
        else if (cnpjAtual != null && cnpjAtual.equals(cnpjNovo)) {
            // CNPJ permanece o mesmo
        }
        // Caso: valor -> outro (verificar duplicidade e atualizar)
        else if (cnpjAtual != null && cnpjNovo != null && !cnpjAtual.equals(cnpjNovo)) {
            if (empresaRepository.existsByCnpj(cnpjNovo)) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "CNPJ já cadastrado");
            }
            empresa.setCnpj(cnpjNovo);
        }
        // Caso: valor -> null (permitir remoção)
        else if (cnpjAtual != null && cnpjNovo == null) {
            empresa.setCnpj(null);
        }

        empresa.setAtivo(formulario.getAtivo());
        return empresaRepository.save(empresa);
    }

    @Transactional
    public void desativar(Long id) {
        Empresa empresa = buscarPorId(id);
        empresa.setAtivo(false);
        empresaRepository.save(empresa);
    }

    @Transactional
    public void ativar(Long id) {
        Empresa empresa = buscarPorId(id);
        empresa.setAtivo(true);
        empresaRepository.save(empresa);
    }
}
