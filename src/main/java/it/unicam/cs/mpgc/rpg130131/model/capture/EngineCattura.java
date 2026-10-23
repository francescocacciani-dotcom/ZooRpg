package it.unicam.cs.mpgc.rpg130131.model.capture;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;

import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Esegue i tentativi di cattura. Non modifica inventario né zoo: calcola la probabilità
 * con il {@link CalcolatoreProbabilita} e tira il dado.
 */
public class EngineCattura {

    private final CalcolatoreProbabilita calcolatore;
    private final RandomGenerator random;

    public EngineCattura(CalcolatoreProbabilita calcolatore) {
        this(calcolatore, new Random());
    }

    public EngineCattura(CalcolatoreProbabilita calcolatore, RandomGenerator random) {
        this.calcolatore = Objects.requireNonNull(calcolatore, "calcolatore");
        this.random = Objects.requireNonNull(random, "random");
    }

    public RisultatoCattura tenta(Animale animale, StrumentoCattura strumento) {
        return tenta(animale, strumento, Cibo.NESSUNA_ESCA);
    }

    /**
     * @throws IllegalStateException se il calcolatore restituisce una probabilità fuori da [0, 1]
     */
    public RisultatoCattura tenta(Animale animale, StrumentoCattura strumento, Cibo esca) {
        double probabilita = calcolatore.calcola(animale, strumento, esca);
        if (probabilita < 0 || probabilita > 1) {
            throw new IllegalStateException("Probabilità non valida: " + probabilita);
        }
        return new RisultatoCattura(random.nextDouble() < probabilita, probabilita);
    }
}