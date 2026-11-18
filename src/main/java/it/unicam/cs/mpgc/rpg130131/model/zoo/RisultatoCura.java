package it.unicam.cs.mpgc.rpg130131.model.zoo;

import java.util.Objects;

/**
 * @param esito           che cosa è successo
 * @param puntiRecuperati punti vita guadagnati (0 se l'animale non è stato curato)
 */
public record RisultatoCura(EsitoCura esito, int puntiRecuperati) {

    public RisultatoCura {
        Objects.requireNonNull(esito, "esito");
    }
}