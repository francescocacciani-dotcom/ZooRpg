package it.unicam.cs.mpgc.rpg130131.model.zoo;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Lo zoo del giocatore: un insieme di recinti in cui vengono accolti gli animali catturati. */
public class Zoo {

    private final String nome;
    private final List<Recinto> recinti = new ArrayList<>();

    public Zoo(String nome) {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void aggiungiRecinto(Recinto recinto) {
        recinti.add(Objects.requireNonNull(recinto, "recinto"));
    }

    /** @return vista non modificabile dei recinti */
    public List<Recinto> getRecinti() {
        return Collections.unmodifiableList(recinti);
    }

    /** @return tutti gli animali ospitati nello zoo */
    public List<Animale> getAnimali() {
        return recinti.stream().flatMap(r -> r.getAnimali().stream()).toList();
    }

    /** @return true se l'animale non è già nello zoo e esiste un recinto adatto con posto libero */
    public boolean puoAccogliere(Animale animale) {
        return trovaRecinto(animale).isPresent();
    }

    /**
     * Inserisce l'animale nel primo recinto adatto.
     *
     * @return il recinto scelto, oppure vuoto se l'animale non può essere accolto
     */
    public Optional<Recinto> accogli(Animale animale) {
        Optional<Recinto> recinto = trovaRecinto(animale);
        recinto.ifPresent(r -> r.aggiungi(animale));
        return recinto;
    }

    public StatisticheZoo getStatistiche() {
        return StatisticheZoo.calcola(recinti);
    }

    private Optional<Recinto> trovaRecinto(Animale animale) {
        Objects.requireNonNull(animale, "animale");
        if (getAnimali().contains(animale)) {
            return Optional.empty();
        }
        return recinti.stream().filter(r -> r.puoOspitare(animale)).findFirst();
    }
}