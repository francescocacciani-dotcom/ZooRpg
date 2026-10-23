package it.unicam.cs.mpgc.rpg130131.model.inventory;

import java.util.Objects;

/**
 * Strumento usato per tentare una cattura (rete, trappola, tranquillante).
 *
 * @param nome         nome visualizzato
 * @param bonusCattura quanto aumenta la probabilità di cattura, in [0, 1]
 */
public record StrumentoCattura(String nome, double bonusCattura) implements Oggetto {

    public StrumentoCattura {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        if (bonusCattura < 0 || bonusCattura > 1) {
            throw new IllegalArgumentException("Il bonus deve essere in [0, 1]: " + bonusCattura);
        }
    }
}