package it.unicam.cs.mpgc.rpg130131.model.inventory;

import java.util.Objects;

/**
 * Cibo: serve come esca per attirare un animale e, in piccola parte, per curarlo.
 *
 * @param nome      nome visualizzato
 * @param bonusEsca quanto aumenta la probabilità di cattura, in [0, 1]
 * @param puntiVita punti vita recuperati se usato per curare (0 = non cura)
 */
public record Cibo(String nome, double bonusEsca, int puntiVita) implements Oggetto {

    /** Valore da usare quando non si impiega alcuna esca. */
    public static final Cibo NESSUNA_ESCA = new Cibo("Nessuna esca", 0.0, 0);

    public Cibo {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        if (bonusEsca < 0 || bonusEsca > 1) {
            throw new IllegalArgumentException("Il bonus deve essere in [0, 1]: " + bonusEsca);
        }
        if (puntiVita < 0) {
            throw new IllegalArgumentException("I punti vita non possono essere negativi: " + puntiVita);
        }
    }

    /** Cibo che non cura. Ci possono essere cibi che non danno punti vita*/
    public Cibo(String nome, double bonusEsca) {
        this(nome, bonusEsca, 0);
    }
}