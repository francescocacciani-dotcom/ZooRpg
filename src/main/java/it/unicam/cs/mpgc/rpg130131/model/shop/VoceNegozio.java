package it.unicam.cs.mpgc.rpg130131.model.shop;

import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;

import java.util.Objects;

/**
 * Un articolo in vendita.
 *
 * @param oggetto l'oggetto venduto (una unità per acquisto)
 * @param prezzo  prezzo in monete
 */
public record VoceNegozio(Oggetto oggetto, int prezzo) {

    public VoceNegozio {
        Objects.requireNonNull(oggetto, "oggetto");
        if (prezzo <= 0) {
            throw new IllegalArgumentException("Il prezzo deve essere positivo: " + prezzo);
        }
    }
}