package it.unicam.cs.mpgc.rpg130131.controller;

/**
 * Parametri di una nuova partita.
 *
 * @param larghezza       colonne della mappa
 * @param altezza         righe della mappa
 * @param popolazione     animali che il gioco cerca di tenere sulla mappa
 * @param capienzaRecinti capienza di ciascun recinto iniziale
 * @param moneteIniziali  monete con cui parte il giocatore (può essere 0)
 */
public record ConfigurazioneGioco(int larghezza,
                                  int altezza,
                                  int popolazione,
                                  int capienzaRecinti,
                                  int moneteIniziali) {

    public ConfigurazioneGioco {
        if (larghezza <= 0 || altezza <= 0 || popolazione <= 0 || capienzaRecinti <= 0) {
            throw new IllegalArgumentException("Dimensioni, popolazione e capienza devono essere positive");
        }
        if (moneteIniziali < 0) {
            throw new IllegalArgumentException("Le monete iniziali non possono essere negative");
        }
    }

    public static ConfigurazioneGioco predefinita() {
        return new ConfigurazioneGioco(12, 8, 8, 4, 20);
    }
}