package it.unicam.cs.mpgc.rpg130131.model.animals;

import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;

import java.util.Objects;
import java.util.UUID;

/**
 * Un esemplare di animale. Contiene i dati comuni a tutte le specie; ogni sottoclasse
 * definisce solo le caratteristiche proprie della specie.
 * L'identità è data dall'id univoco; la vita è l'unica parte modificabile.
 * La condizione ({@link StatoAnimale}) è derivata dalla vita.
 */
public abstract class Animale {

    public static final int VITA_MASSIMA = 100;
    /** Sotto questa soglia di vita l'animale è in pericolo. */
    public static final int SOGLIA_PERICOLO = 50;

    private static final int VITA_INIZIALE_IN_PERICOLO = 30;

    private final UUID id;
    private final String nome;
    private final RaritaEnum rarita;
    private int vita;

    protected Animale(String nome, RaritaEnum rarita, StatoAnimale stato) {
        this(UUID.randomUUID(), nome, rarita, stato);
    }

    /** Costruttore usato anche per ricostruire un animale salvato. */
    protected Animale(UUID id, String nome, RaritaEnum rarita, StatoAnimale stato) {
        this.id = Objects.requireNonNull(id, "id");
        this.nome = requireNonBlank(nome);
        this.rarita = Objects.requireNonNull(rarita, "rarita");
        this.vita = Objects.requireNonNull(stato, "stato") == StatoAnimale.IN_PERICOLO
                ? VITA_INIZIALE_IN_PERICOLO
                : VITA_MASSIMA;
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

    public int getVita() {
        return vita;
    }

    /** @return IN_PERICOLO se la vita è sotto {@link #SOGLIA_PERICOLO}, altrimenti SELVATICO */
    public StatoAnimale getStato() {
        return vita < SOGLIA_PERICOLO ? StatoAnimale.IN_PERICOLO : StatoAnimale.SELVATICO;
    }

    /**
     * @throws IllegalArgumentException se la vita non è in [0, {@link #VITA_MASSIMA}]
     */
    public void impostaVita(int vita) {
        if (vita < 0 || vita > VITA_MASSIMA) {
            throw new IllegalArgumentException("La vita deve essere in [0, %d]: %d".formatted(VITA_MASSIMA, vita));
        }
        this.vita = vita;
    }

    /**
     * Aggiunge punti di vita, senza superare il massimo.
     *
     * @return i punti effettivamente recuperati
     * @throws IllegalArgumentException se i punti non sono positivi
     */
    public int cura(int punti) {
        if (punti <= 0) {
            throw new IllegalArgumentException("I punti di cura devono essere positivi: " + punti);
        }
        int prima = vita;
        vita = Math.min(VITA_MASSIMA, vita + punti);
        return vita - prima;
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
        return "%s \"%s\" [%s, vita %d]".formatted(getSpecie(), nome, rarita, vita);
    }
}
