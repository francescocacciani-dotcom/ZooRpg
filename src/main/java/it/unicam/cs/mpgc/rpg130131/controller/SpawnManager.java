package it.unicam.cs.mpgc.rpg130131.controller;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.catalog.VoceAnimale;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.Cella;
import it.unicam.cs.mpgc.rpg130131.model.map.Mappa2D;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Fa comparire gli animali sulla mappa e ricorda dove si trovano.
 * Ogni animale compare in una cella libera del proprio bioma nativo; la specie è
 * scelta in modo pesato in base al catalogo.
 */
public class SpawnManager {

    /** Probabilità che un animale compaia in pericolo anziché selvatico. */
    public static final double PROBABILITA_IN_PERICOLO = 0.25;

    private static final int TENTATIVI_PER_ANIMALE = 10;

    private final Catalogo catalogo;
    private final RegistroSpecie registro;
    private final RandomGenerator random;
    private final int pesoTotale;
    private final Map<Posizione, Animale> avvistamenti = new LinkedHashMap<>();

    /**
     * @throws IllegalArgumentException se il catalogo non ha animali o contiene specie non registrate
     */
    public SpawnManager(Catalogo catalogo, RegistroSpecie registro, RandomGenerator random) {
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
        this.registro = Objects.requireNonNull(registro, "registro");
        this.random = Objects.requireNonNull(random, "random");
        if (catalogo.animali().isEmpty()) {
            throw new IllegalArgumentException("Il catalogo non contiene animali");
        }
        for (VoceAnimale voce : catalogo.animali()) {
            if (!registro.contiene(voce.specie())) {
                throw new IllegalArgumentException("Specie non registrata: " + voce.specie());
            }
        }
        this.pesoTotale = catalogo.animali().stream().mapToInt(VoceAnimale::peso).sum();
    }

    /**
     * Prova a far comparire {@code quantita} animali. Se non c'è spazio nei biomi adatti
     * ne compaiono meno.
     *
     * @return quanti animali sono davvero comparsi
     * @throws IllegalArgumentException se la quantità è negativa
     */
    public int popola(Mappa2D mappa, int quantita) {
        Objects.requireNonNull(mappa, "mappa");
        if (quantita < 0) {
            throw new IllegalArgumentException("La quantità non può essere negativa: " + quantita);
        }
        int comparsi = 0;
        int tentativiMassimi = quantita * TENTATIVI_PER_ANIMALE;
        for (int tentativo = 0; tentativo < tentativiMassimi && comparsi < quantita; tentativo++) {
            Animale animale = creaAnimale(scegliVoce());
            Optional<Posizione> libera = scegliCellaLibera(mappa, animale.getBiomaNativo());
            if (libera.isPresent()) {
                avvistamenti.put(libera.get(), animale);
                comparsi++;
            }
        }
        return comparsi;
    }

    /**
     * Fa comparire animali finché sulla mappa ce ne sono {@code obiettivo}.
     *
     * @return quanti animali sono comparsi
     */
    public int mantieniPopolazione(Mappa2D mappa, int obiettivo) {
        return popola(mappa, Math.max(0, obiettivo - avvistamenti.size()));
    }

    public Optional<Animale> animaleIn(Posizione posizione) {
        return Optional.ofNullable(avvistamenti.get(posizione));
    }

    /** @return l'animale che si trovava in quella posizione, se c'era */
    public Optional<Animale> rimuovi(Posizione posizione) {
        return Optional.ofNullable(avvistamenti.remove(posizione));
    }

    /** @return vista non modificabile degli animali presenti sulla mappa */
    public Map<Posizione, Animale> getAvvistamenti() {
        return Collections.unmodifiableMap(avvistamenti);
    }

    private VoceAnimale scegliVoce() {
        int estratto = random.nextInt(pesoTotale);
        for (VoceAnimale voce : catalogo.animali()) {
            estratto -= voce.peso();
            if (estratto < 0) {
                return voce;
            }
        }
        throw new IllegalStateException("Pesi del catalogo incoerenti");
    }

    private Animale creaAnimale(VoceAnimale voce) {
        String nome = voce.nomi().get(random.nextInt(voce.nomi().size()));
        StatoAnimale stato = random.nextDouble() < PROBABILITA_IN_PERICOLO
                ? StatoAnimale.IN_PERICOLO
                : StatoAnimale.SELVATICO;
        return registro.crea(voce.specie(), nome, voce.rarita(), stato);
    }

    private Optional<Posizione> scegliCellaLibera(Mappa2D mappa, BiomaEnum bioma) {
        List<Posizione> libere = mappa.celleDiBioma(bioma).stream()
                .map(Cella::posizione)
                .filter(posizione -> !avvistamenti.containsKey(posizione))
                .toList();
        if (libere.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(libere.get(random.nextInt(libere.size())));
    }
}