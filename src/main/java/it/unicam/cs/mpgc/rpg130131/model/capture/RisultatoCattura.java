package it.unicam.cs.mpgc.rpg130131.model.capture;

/**
 * Esito di un tentativo di cattura.
 *
 * @param riuscita    true se l'animale è stato catturato
 * @param probabilita probabilità con cui il tentativo è stato effettuato
 */
public record RisultatoCattura(boolean riuscita, double probabilita) {
}