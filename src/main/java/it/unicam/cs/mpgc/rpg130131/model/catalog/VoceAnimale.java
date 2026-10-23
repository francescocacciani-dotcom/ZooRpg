package it.unicam.cs.mpgc.rpg130131.model.catalog;

import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;

import java.util.List;
import java.util.Objects;

/**
 * Una possibile comparsa di animale: quale specie, con che rarità, con che peso
 * (frequenza relativa) e con quali nomi per gli esemplari.
 */
public record VoceAnimale(String specie, RaritaEnum rarita, int peso, List<String> nomi) {

    public VoceAnimale {
        Objects.requireNonNull(specie, "specie");
        Objects.requireNonNull(rarita, "rarita");
        Objects.requireNonNull(nomi, "nomi");
        if (specie.isBlank()) {
            throw new IllegalArgumentException("La specie non può essere vuota");
        }
        if (peso <= 0) {
            throw new IllegalArgumentException("Il peso deve essere positivo: " + peso);
        }
        nomi = List.copyOf(nomi);
        if (nomi.isEmpty() || nomi.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("Servono nomi non vuoti per " + specie);
        }
    }
}