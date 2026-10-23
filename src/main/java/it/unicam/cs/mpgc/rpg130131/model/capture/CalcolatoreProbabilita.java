package it.unicam.cs.mpgc.rpg130131.model.capture;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;

/** Strategia per stabilire la probabilità di riuscita di un tentativo di cattura. */
@FunctionalInterface
public interface CalcolatoreProbabilita {

    /**
     * @param esca usare {@link Cibo#NESSUNA_ESCA} se non c'è esca
     * @return probabilità di cattura, in [0, 1]
     */
    double calcola(Animale animale, StrumentoCattura strumento, Cibo esca);
}