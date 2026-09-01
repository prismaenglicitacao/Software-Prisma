package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Analise;
import br.com.softwareprisma.licitacao.domain.Empresa;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import br.com.softwareprisma.licitacao.repository.AnaliseRepository;
import br.com.softwareprisma.licitacao.repository.AnaliseResultadoPersistidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final AnaliseRepository analiseRepository;
    private final AnaliseResultadoPersistidoRepository analiseResultadoPersistidoRepository;

    @Transactional(readOnly = true)
    public Page<Analise> buscarAnalises(Integer pagina, String ordenarPor, String area, String resultado, Empresa empresa) {
        int page = pagina != null ? pagina : 0;
        int size = 10;

        String ordenarPorSafe = (ordenarPor == null || ordenarPor.isBlank()) ? "data" : ordenarPor;

        Sort sort = switch (ordenarPorSafe) {
            case "data" -> Sort.by("dataCriacao").descending();
            case "resultado" -> Sort.by("resultado").ascending();
            case "area" -> Sort.by("area").ascending();
            default -> Sort.by("dataCriacao").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        Area areaEnum = area != null && !area.isEmpty() ? Area.valueOf(area) : null;
        ResultadoAnalise resultadoEnum = resultado != null && !resultado.isEmpty() ? ResultadoAnalise.valueOf(resultado) : null;

        Page<Analise> analisesPaginadas = analiseRepository.buscarComFiltrosEEmpresa(empresa, areaEnum, resultadoEnum, pageable);

        List<Long> ids = analisesPaginadas.getContent().stream().map(Analise::getId).toList();

        if (!ids.isEmpty()) {
            List<Analise> analisesComItens = analiseRepository.buscarComItensPorIdsEEmpresa(ids, empresa);
            Map<Long, Analise> map = analisesComItens.stream()
                    .collect(Collectors.toMap(Analise::getId, a -> a));
            List<Analise> ordenadas = analisesPaginadas.getContent().stream()
                    .map(a -> map.getOrDefault(a.getId(), a))
                    .toList();
            return new PageImpl<>(ordenadas, pageable, analisesPaginadas.getTotalElements());
        }

        return analisesPaginadas;
    }

    @Transactional
    public void excluirAnalise(Long id, Empresa empresa) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }

        Analise analise = analiseRepository.buscarDetalhadaPorIdEEmpresa(id, empresa)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Análise não encontrada ou acesso negado."));

        analiseResultadoPersistidoRepository.deleteByAnaliseId(analise.getId());
        analiseRepository.delete(analise);
    }

    // Keep old signature for backward compatibility (called without empresa context)
    @Transactional
    public void excluirAnalise(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        Analise analise = analiseRepository.buscarDetalhadaPorId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Análise não encontrada."));
        analiseResultadoPersistidoRepository.deleteByAnaliseId(analise.getId());
        analiseRepository.delete(analise);
    }
}
