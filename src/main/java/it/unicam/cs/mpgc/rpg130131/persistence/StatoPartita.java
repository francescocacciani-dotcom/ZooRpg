package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;

import java.util.Objects;

/**
 * Tutto ciò che viene salvato di una partita: lo zoo e il giocatore (posizione e inventario).
 * La mappa è fissa e gli animali sulla mappa non vengono salvati.
 */
public record StatoPartita(Zoo zoo, Giocatore giocatore) {

    public StatoPartita {
        Objects.requireNonNull(zoo, "zoo");
        Objects.requireNonNull(giocatore, "giocatore");
    }
}