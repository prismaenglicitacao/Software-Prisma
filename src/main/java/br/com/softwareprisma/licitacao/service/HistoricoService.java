package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import br.com.softwareprisma.licitacao.repository.AnaliseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final AnaliseRepository analiseRepository;

    @Transactional(readOnly = true)
    public Page<Analise> buscarAnalises(Integer pagina, String ordenarPor, String area, String resultado) {
        int page = pagina != null ? pagina : 0;
        int size = 10;

        Sort sort = switch (ordenarPor) {
            case "data" -> Sort.by("dataCriacao").descending();
            case "resultado" -> Sort.by("resultado").ascending();
            case "area" -> Sort.by("area").ascending();
            default -> Sort.by("dataCriacao").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        Area areaEnum = area != null && !area.isEmpty() ? Area.valueOf(area) : null;
        ResultadoAnalise resultadoEnum = resultado != null && !resultado.isEmpty() ? ResultadoAnalise.valueOf(resultado) : null;

        return analiseRepository.buscarComFiltros(areaEnum, resultadoEnum, pageable);
    }

    @Transactional
    public void excluirAnalise(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        analiseRepository.deleteById(id);
    }
}
