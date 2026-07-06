package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.AnaliseResumoDTO;
import br.com.softwareprisma.licitacao.controller.dto.EstatisticasGeraisDTO;
import br.com.softwareprisma.licitacao.domain.enums.Area;
import br.com.softwareprisma.licitacao.repository.AnaliseRepository;
import br.com.softwareprisma.licitacao.repository.CatItemRepository;
import br.com.softwareprisma.licitacao.repository.CatRepository;
import br.com.softwareprisma.licitacao.repository.EngenheiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EngenheiroRepository engenheiroRepository;
    private final CatRepository catRepository;
    private final CatItemRepository catItemRepository;
    private final AnaliseRepository analiseRepository;

    public EstatisticasGeraisDTO getEstatisticasGerais() {
        long quantidadeEngenheiros = engenheiroRepository.countTotal();
        long quantidadeCats = catRepository.countTotal();
        long quantidadeItens = catItemRepository.countTotal();
        long quantidadeAnalises = analiseRepository.countTotal();
        long quantidadeAnalisesAtenderam = analiseRepository.countAtenderam();
        long quantidadeAnalisesNaoAtenderam = analiseRepository.countNaoAtenderam();
        
        BigDecimal coberturaMedia = analiseRepository.avgCobertura();
        if (coberturaMedia == null) {
            coberturaMedia = BigDecimal.ZERO;
        } else {
            coberturaMedia = coberturaMedia.multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new EstatisticasGeraisDTO(
                quantidadeEngenheiros,
                quantidadeCats,
                quantidadeItens,
                quantidadeAnalises,
                quantidadeAnalisesAtenderam,
                quantidadeAnalisesNaoAtenderam,
                coberturaMedia
        );
    }

    public Page<AnaliseResumoDTO> getUltimasAnalises(int page) {
        PageRequest pageRequest = PageRequest.of(page, 5, Sort.by("dataCriacao").descending());
        return analiseRepository.findRecentes(pageRequest);
    }

    public Map<String, Long> getCatsPorArea() {
        return Map.of(
            "ILUMINACAO", catRepository.countByArea(Area.ILUMINACAO),
            "PAVIMENTACAO", catRepository.countByArea(Area.PAVIMENTACAO)
        );
    }

    public Map<String, Long> getItensPorArea() {
        return Map.of(
            "ILUMINACAO", catItemRepository.countByArea(Area.ILUMINACAO),
            "PAVIMENTACAO", catItemRepository.countByArea(Area.PAVIMENTACAO)
        );
    }
}
