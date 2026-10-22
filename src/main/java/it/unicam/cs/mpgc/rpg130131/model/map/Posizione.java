package it.unicam.cs.mpgc.rpg130131.model.map;

import java.util.Objects;

/** Coordinate di una cella. Non valida i limiti: spetta alla mappa dire se una posizione le appartiene. */
public record Posizione(int x, int y) {

    /** Restituisce la posizione adiacente nella direzione indicata. */
    public Posizione verso(Direzione direzione) {
        Objects.requireNonNull(direzione, "direzione");
        return new Posizione(x + direzione.getDx(), y + direzione.getDy());
    }
}
