package it.unicam.cs.mpgc.rpg130131.model.animals;

import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;

import java.util.Objects;
import java.util.UUID;

public abstract class Animale {
    private final UUID id;
    private final String nome;
    private final RaritaEnum rarita;
    private final StatoAnimale stato;

    protected Animale(String nome, RaritaEnum rarita, StatoAnimale stato) {
        this(UUID.randomUUID(), nome, rarita, stato);
    }

    /** Costruttore usato anche per ricostruire un animale salvato. */
    protected Animale(UUID id, String nome, RaritaEnum rarita, StatoAnimale stato) {
        this.id = Objects.requireNonNull(id, "id");
        this.nome = requireNonBlank(nome);
        this.rarita = Objects.requireNonNull(rarita, "rarita");
        this.stato = Objects.requireNonNull(stato, "stato");
    }

    private static String requireNonBlank(String nome) {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        return nome;
    }

    /** Nome della specie (es. "Leone"). */
    public abstract String getSpecie();

    /** Bioma in cui la specie compare più naturalmente. */
    public abstract BiomaEnum getBiomaNativo();

    /** Difficoltà base di cattura della specie, in [0, 1]: più alta = più difficile. */
    public abstract double getDifficoltaBase();

    /** Verso caratteristico, usato dalla vista e dai messaggi di gioco. */
    public abstract String getVerso();

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public RaritaEnum getRarita() {
        return rarita;
    }

    public StatoAnimale getStato() {
        return stato;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Animale altro && id.equals(altro.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "%s \"%s\" [%s, %s]".formatted(getSpecie(), nome, rarita, stato);
    }
}

