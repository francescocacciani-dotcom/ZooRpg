package it.unicam.cs.mpgc.rpg130131.model.inventory;

import java.util.Objects;

/** Una quantità di un oggetto (per esempio una voce del kit iniziale). */
public record Scorta(Oggetto oggetto, int quantita) {

    public Scorta {
        Objects.requireNonNull(oggetto, "oggetto");
        if (quantita <= 0) {
            throw new IllegalArgumentException("La quantità deve essere positiva: " + quantita);
        }
    }
}