package it.unicam.cs.mpgc.rpg130131.controller;

/**
 * Parametri di una nuova partita.
 *
 * @param larghezza         colonne della mappa
 * @param altezza           righe della mappa
 * @param popolazione       animali che il gioco cerca di tenere sulla mappa
 * @param quantitaStrumenti esemplari di ogni strumento nel kit iniziale
 * @param quantitaCibi      esemplari di ogni cibo nel kit iniziale
 * @param capienzaRecinti   capienza di ciascun recinto iniziale
 */
public record ConfigurazioneGioco(int larghezza,
                                  int altezza,
                                  int popolazione,
                                  int quantitaStrumenti,
                                  int quantitaCibi,
                                  int capienzaRecinti) {

    public ConfigurazioneGioco {
        if (larghezza <= 0 || altezza <= 0 || popolazione <= 0
                || quantitaStrumenti <= 0 || quantitaCibi <= 0 || capienzaRecinti <= 0) {
            throw new IllegalArgumentException("Tutti i parametri di configurazione devono essere positivi");
        }
    }

    public static ConfigurazioneGioco predefinita() {
        return new ConfigurazioneGioco(12, 8, 8, 3, 2, 4);
    }
}