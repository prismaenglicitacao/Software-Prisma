package br.com.softwareprisma.licitacao.service;

import br.com.softwareprisma.licitacao.controller.dto.AnaliseResumoDTO;
import br.com.softwareprisma.licitacao.controller.dto.EstatisticasGeraisDTO;
import br.com.softwareprisma.licitacao.domain.Empresa;
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

    public EstatisticasGeraisDTO getEstatisticasGerais(Empresa empresa) {
        long quantidadeEngenheiros = engenheiroRepository.countByEmpresa(empresa);
        long quantidadeCats = catRepository.countByEmpresa(empresa);
        long quantidadeItens = catItemRepository.countByEmpresa(empresa);
        long quantidadeAnalises = analiseRepository.countByEmpresa(empresa);
        long quantidadeAnalisesAtenderam = analiseRepository.countAtenderamByEmpresa(empresa);
        long quantidadeAnalisesNaoAtenderam = analiseRepository.countNaoAtenderamByEmpresa(empresa);

        BigDecimal coberturaMedia = analiseRepository.avgCoberturaByEmpresa(empresa);
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

    public Page<AnaliseResumoDTO> getUltimasAnalises(int page, Empresa empresa) {
        PageRequest pageRequest = PageRequest.of(page, 5, Sort.by("dataCriacao").descending());
        return analiseRepository.findRecentesByEmpresa(empresa, pageRequest);
    }

    public Map<String, Long> getCatsPorArea(Empresa empresa) {
        return Map.of(
            "ELETRICA", catRepository.countByEmpresaEEmpresa(empresa, Area.ELETRICA),
            "CIVIL", catRepository.countByEmpresaEEmpresa(empresa, Area.CIVIL)
        );
    }

    public Map<String, Long> getItensPorArea(Empresa empresa) {
        return Map.of(
            "ELETRICA", catItemRepository.countByEmpresaAndArea(empresa, Area.ELETRICA),
            "CIVIL", catItemRepository.countByEmpresaAndArea(empresa, Area.CIVIL)
        );
    }
}
