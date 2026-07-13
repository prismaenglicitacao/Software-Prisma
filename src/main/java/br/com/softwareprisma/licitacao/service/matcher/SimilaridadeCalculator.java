package br.com.softwareprisma.licitacao.service.matcher;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SimilaridadeCalculator {

    public double calcular(Set<String> a, Set<String> b) {

        Set<String> intersecao = new HashSet<>(a);

        intersecao.retainAll(b);

        Set<String> uniao = new HashSet<>(a);

        uniao.addAll(b);

        if (uniao.isEmpty()) {
            return 0;
        }

        return (double) intersecao.size() / uniao.size();

    }

}