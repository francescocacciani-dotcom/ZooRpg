package it.unicam.cs.mpgc.rpg130131.model.inventory;

import java.util.Objects;

/**
 * Medicina per curare gli animali dello zoo.
 *
 * @param nome      nome visualizzato
 * @param puntiVita punti vita recuperati
 */
public record Medicina(String nome, int puntiVita) implements Oggetto {

    public Medicina {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        if (puntiVita <= 0) {
            throw new IllegalArgumentException("I punti vita devono essere positivi: " + puntiVita);
        }
    }
}