package it.unicam.cs.mpgc.rpg130131.controller;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Risultato di un tentativo di cattura, da mostrare al giocatore.
 *
 * @param tipo        che cosa è successo
 * @param animale     l'animale coinvolto; è null solo per {@link Tipo#NESSUN_ANIMALE}
 * @param probabilita probabilità del tentativo; 0 se il tentativo non è stato effettuato
 * @param ricompensa  oggetti ricevuti (vuota se non c'è stata una cattura)
 */
public record EsitoTentativo(Tipo tipo, Animale animale, double probabilita, Map<Oggetto, Integer> ricompensa) {

    public enum Tipo {
        NESSUN_ANIMALE,
        STRUMENTO_NON_DISPONIBILE,
        ESCA_NON_DISPONIBILE,
        ZOO_SENZA_POSTO,
        FALLITO,
        CATTURATO
    }

    public EsitoTentativo {
        Objects.requireNonNull(tipo, "tipo");
        ricompensa = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(ricompensa, "ricompensa")));
    }

    /** Esito senza ricompensa. */
    public EsitoTentativo(Tipo tipo, Animale animale, double probabilita) {
        this(tipo, animale, probabilita, Map.of());
    }

    public boolean catturato() {
        return tipo == Tipo.CATTURATO;
    }
}