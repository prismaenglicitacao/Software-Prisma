package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Cat;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.Engenheiro;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CatService {

    private final CatRepository catRepository;
    private final EngenheiroService engenheiroService;

    @Transactional(readOnly = true)
    public List<Cat> listarTodas() {
        return catRepository.listarTodasComEngenheiroEItens();
    }


    @Transactional(readOnly = true)
    public Cat buscarPorId(Long id) {
        return catRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "CAT nao encontrada"));
    }

    @Transactional(readOnly = true)
    public Cat buscarDetalhadaPorId(Long id) {
        return catRepository.buscarDetalhadaPorId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "CAT nao encontrada"));
    }

    @Transactional(readOnly = true)
    public Map<Engenheiro, CatGroupInfo> listarAgrupadasPorEngenheiroComInfoFiltradas(String filtro, Empresa empresa) {
        List<Cat> cats = catRepository.listarPorEmpresa(empresa);
        Map<Engenheiro, List<Cat>> agrupadas = cats.stream()
                .filter(cat -> cat.getEngenheiro() != null)
                .collect(Collectors.groupingBy(Cat::getEngenheiro));

        if (filtro != null && !filtro.trim().isEmpty()) {
            String filtroLower = filtro.toLowerCase();
            agrupadas = agrupadas.entrySet().stream()
                    .filter(entry -> {
                        String nome = entry.getKey() != null && entry.getKey().getNome() != null
                                ? entry.getKey().getNome() : "";
                        return nome.toLowerCase().contains(filtroLower);
                    })
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new
                    ));
        }

        return agrupadas.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(
                        Engenheiro::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new CatGroupInfo(entry.getValue()),
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
    }

    @Transactional(readOnly = true)
    public Cat buscarDetalhadaPorIdEEmpresa(Long id, Empresa empresa) {
        return catRepository.buscarDetalhadaPorIdEEmpresa(id, empresa)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "CAT nao encontrada"));
    }

    @Transactional
    public Cat salvarComEmpresa(Cat cat, Long engenheiroId, Empresa empresa) {
        Engenheiro engenheiro = engenheiroService.buscarPorIdEEmpresa(engenheiroId, empresa);
        cat.setEngenheiro(engenheiro);
        return catRepository.save(cat);
    }

    @Transactional
    public Cat atualizarComEmpresa(Long id, Cat formulario, Long engenheiroId, Empresa empresa) {
        Cat cat = buscarDetalhadaPorIdEEmpresa(id, empresa);
        Engenheiro engenheiro = engenheiroService.buscarPorIdEEmpresa(engenheiroId, empresa);
        cat.setEngenheiro(engenheiro);
        cat.setNome(formulario.getNome());
        cat.setNumeroCat(formulario.getNumeroCat());
        cat.setMunicipio(formulario.getMunicipio());
        cat.setObservacoes(formulario.getObservacoes());
        return catRepository.save(cat);
    }

    @Transactional
    public void excluirPorIdEEmpresa(Long id, Empresa empresa) {
        Cat cat = buscarDetalhadaPorIdEEmpresa(id, empresa);
        catRepository.delete(cat);
    }


    @Transactional
    public Cat salvar(Cat cat, Long engenheiroId) {
        Engenheiro engenheiro = engenheiroService.buscarPorId(engenheiroId);
        cat.setEngenheiro(engenheiro);
        return catRepository.save(cat);
    }


    @Transactional
    public Cat atualizar(Long id, Cat formulario, Long engenheiroId) {
        Cat cat = buscarPorId(id);
        Engenheiro engenheiro = engenheiroService.buscarPorId(engenheiroId);
        cat.setEngenheiro(engenheiro);
        cat.setNome(formulario.getNome());
        cat.setNumeroCat(formulario.getNumeroCat());
        cat.setMunicipio(formulario.getMunicipio());
        cat.setObservacoes(formulario.getObservacoes());
        return catRepository.save(cat);
    }


    @Transactional
    public void excluir(Long id) {
        Cat cat = buscarPorId(id);
        catRepository.delete(cat);
    }


    public static class CatGroupInfo {
        private final List<Cat> cats;
        private final int totalItens;

        public CatGroupInfo(List<Cat> cats) {
            this.cats = cats;
            this.totalItens = cats.stream()
                    .mapToInt(cat -> cat.getItens() != null ? cat.getItens().size() : 0)
                    .sum();
        }

        public List<Cat> getCats() {
            return cats;
        }

        public int getTotalItens() {
            return totalItens;
        }

        public int getTotalCats() {
            return cats.size();
        }
    }
}
