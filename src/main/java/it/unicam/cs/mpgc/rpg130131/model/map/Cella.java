package it.unicam.cs.mpgc.rpg130131.model.map;

import java.util.Objects;

/** Una singola cella del terreno: dove si trova e di che bioma è. */
public record Cella(Posizione posizione, BiomaEnum bioma) {

    public Cella {
        Objects.requireNonNull(posizione, "posizione");
        Objects.requireNonNull(bioma, "bioma");
    }
}