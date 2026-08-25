package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.domain.Analise;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final AnaliseRepository analiseRepository;
    private final AnaliseResultadoPersistidoRepository analiseResultadoPersistidoRepository;

    @Transactional(readOnly = true)
    public Page<Analise> buscarAnalises(Integer pagina, String ordenarPor, String area, String resultado) {
        int page = pagina != null ? pagina : 0;
        int size = 10;

        String ordenarPorSafe = (ordenarPor == null || ordenarPor.isBlank())
                ? "data"
                : ordenarPor;

        Sort sort = switch (ordenarPorSafe) {
            case "data" -> Sort.by("dataCriacao").descending();
            case "resultado" -> Sort.by("resultado").ascending();
            case "area" -> Sort.by("area").ascending();
            default -> Sort.by("dataCriacao").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        Area areaEnum = area != null && !area.isEmpty() ? Area.valueOf(area) : null;
        ResultadoAnalise resultadoEnum = resultado != null && !resultado.isEmpty() ? ResultadoAnalise.valueOf(resultado) : null;

        // Primeiro buscar análises paginadas (sem itens para evitar problemas de paginação)
        Page<Analise> analisesPaginadas = analiseRepository.buscarComFiltros(areaEnum, resultadoEnum, pageable);

        // Extrair IDs das análises da página atual
        List<Long> ids = analisesPaginadas.getContent().stream()
                .map(Analise::getId)
                .toList();

        // Carregar análises com itens em segunda consulta
        if (!ids.isEmpty()) {
            List<Analise> analisesComItens = analiseRepository.buscarComItensPorIds(ids);
            
            // Criar mapa de ID -> Analise com itens
            Map<Long, Analise> analisesComItensMap = analisesComItens.stream()
                    .collect(Collectors.toMap(Analise::getId, a -> a));

            // Substituir análises no Page pelas versões com itens carregados
            List<Analise> analisesComItensOrdenadas = analisesPaginadas.getContent().stream()
                    .map(a -> analisesComItensMap.get(a.getId()))
                    .toList();

            return new PageImpl<>(analisesComItensOrdenadas, pageable, analisesPaginadas.getTotalElements());
        }

        return analisesPaginadas;
    }

    @Transactional
    public void excluirAnalise(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        // Excluir resultado persistido relacionado (cascade cuida dos itens e origens)
        analiseResultadoPersistidoRepository.deleteByAnaliseId(id);
        
        // Excluir análise (cascade cuida dos AnaliseItem)
        analiseRepository.deleteById(id);
    }
}
