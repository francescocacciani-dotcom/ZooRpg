package it.unicam.cs.mpgc.rpg130131.model.inventory;

import java.util.Objects;

/**
 * Cibo usato come esca per attirare un animale.
 *
 * @param nome      nome visualizzato
 * @param bonusEsca quanto aumenta la probabilità di cattura, in [0, 1]
 */
public record Cibo(String nome, double bonusEsca) implements Oggetto {

    /** Valore da usare quando non si impiega alcuna esca. */
    public static final Cibo NESSUNA_ESCA = new Cibo("Nessuna esca", 0.0);

    public Cibo {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        if (bonusEsca < 0 || bonusEsca > 1) {
            throw new IllegalArgumentException("Il bonus deve essere in [0, 1]: " + bonusEsca);
        }
    }
}