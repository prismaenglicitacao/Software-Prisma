package br.com.softwareprisma.licitacao.service.matcher;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SimilaridadeCalculator {

    public double calcular(Set<String> tokens1, Set<String> tokens2) {
        if (tokens1.isEmpty() && tokens2.isEmpty()) {
            return 1.0;
        }
        
        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new java.util.HashSet<>(tokens1);
        intersection.retainAll(tokens2);
        
        Set<String> union = new java.util.HashSet<>(tokens1);
        union.addAll(tokens2);
        
        return (double) intersection.size() / union.size();
    }
}
