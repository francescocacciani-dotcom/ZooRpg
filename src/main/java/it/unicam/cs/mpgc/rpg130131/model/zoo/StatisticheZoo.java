package it.unicam.cs.mpgc.rpg130131.model.zoo;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Istantanea immutabile dei numeri dello zoo.
 *
 * @param totaleAnimali   animali ospitati
 * @param capienzaTotale  somma delle capienze dei recinti
 * @param animaliPerSpecie numero di animali per nome di specie (ordinato alfabeticamente)
 * @param animaliPerRarita numero di animali per rarità (solo le rarità presenti)
 * @param punteggio       somma dei moltiplicatori di valore delle rarità
 */
public record StatisticheZoo(
        int totaleAnimali,
        int capienzaTotale,
        Map<String, Long> animaliPerSpecie,
        Map<RaritaEnum, Long> animaliPerRarita,
        int punteggio) {

    public StatisticheZoo {
        animaliPerSpecie = Collections.unmodifiableMap(new TreeMap<>(animaliPerSpecie));
        EnumMap<RaritaEnum, Long> perRarita = new EnumMap<>(RaritaEnum.class);
        perRarita.putAll(animaliPerRarita);
        animaliPerRarita = Collections.unmodifiableMap(perRarita);
    }

    /** Calcola le statistiche a partire da un insieme di recinti. */
    public static StatisticheZoo calcola(Collection<Recinto> recinti) {
        var animali = recinti.stream().flatMap(r -> r.getAnimali().stream()).toList();
        return new StatisticheZoo(
                animali.size(),
                recinti.stream().mapToInt(Recinto::getCapienza).sum(),
                animali.stream().collect(Collectors.groupingBy(Animale::getSpecie, Collectors.counting())),
                animali.stream().collect(Collectors.groupingBy(Animale::getRarita, Collectors.counting())),
                animali.stream().mapToInt(a -> a.getRarita().getMoltiplicatoreValore()).sum());
    }
}