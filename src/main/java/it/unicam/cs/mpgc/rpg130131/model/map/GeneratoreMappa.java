package it.unicam.cs.mpgc.rpg130131.model.map;

/** Strategia per costruire il terreno di una mappa. */
public interface GeneratoreMappa {

    /**
     * @throws IllegalArgumentException se larghezza o altezza sono <= 0
     */
    Mappa2D genera(int larghezza, int altezza);
}