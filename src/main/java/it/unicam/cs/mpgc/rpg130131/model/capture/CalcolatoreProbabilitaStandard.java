package it.unicam.cs.mpgc.rpg130131.model.capture;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;

import java.util.Objects;

/**
 * Formula standard:
 * <pre>
 * base = (1 - difficoltaBase) * fattoreRarita * fattoreStato
 * p    = clamp(base + bonusStrumento + bonusEsca, MINIMA, MASSIMA)
 * </pre>
 * Un animale in pericolo è più facile da avvicinare (fattoreStato 1.25 contro 1.0).
 * Il clamp garantisce che nessuna cattura sia mai certa né impossibile.
 */
public class CalcolatoreProbabilitaStandard implements CalcolatoreProbabilita {

    public static final double PROBABILITA_MINIMA = 0.10;
    public static final double PROBABILITA_MASSIMA = 0.95;

    @Override
    public double calcola(Animale animale, StrumentoCattura strumento, Cibo esca) {
        Objects.requireNonNull(animale, "animale");
        Objects.requireNonNull(strumento, "strumento");
        Objects.requireNonNull(esca, "esca");

        double base = (1.0 - animale.getDifficoltaBase())
                * animale.getRarita().getFattoreCattura()
                * fattoreStato(animale.getStato());
        double probabilita = base + strumento.bonusCattura() + esca.bonusEsca();
        return Math.clamp(probabilita, PROBABILITA_MINIMA, PROBABILITA_MASSIMA);
    }

    private static double fattoreStato(StatoAnimale stato) {
        return switch (stato) {
            case SELVATICO -> 1.0;
            case IN_PERICOLO -> 1.25;
        };
    }
}