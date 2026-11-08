package it.unicam.cs.mpgc.rpg130131.model.capture;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;

import java.util.Map;

/** Strategia che decide cosa riceve il giocatore quando cattura un animale. */
@FunctionalInterface
public interface PoliticaRicompensa {

    /** @return gli oggetti da consegnare, con le quantità (tutte positive); vuoto se non c'è premio */
    Map<Oggetto, Integer> perCattura(Animale animale);
}