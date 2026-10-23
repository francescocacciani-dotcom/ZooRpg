package it.unicam.cs.mpgc.rpg130131.model.animals;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Associa il nome di una specie al modo di costruirne gli esemplari.
 * Aggiungere una specie significa registrarla qui: nessun altro codice va modificato.
 */
public class RegistroSpecie {

    /** Costruisce un esemplare di una specie con un id dato. */
    @FunctionalInterface
    public interface CreatoreAnimale {
        Animale crea(UUID id, String nome, RaritaEnum rarita, StatoAnimale stato);
    }

    private final Map<String, CreatoreAnimale> creatori = new LinkedHashMap<>();

    /** @return un registro con le specie incluse nel gioco */
    public static RegistroSpecie predefinito() {
        return new RegistroSpecie()
                .registra("Capibara", Capibara::new)
                .registra("Giraffa", Giraffa::new)
                .registra("Leone", Leone::new);
    }

    /**
     * La chiave deve coincidere con {@link Animale#getSpecie()} degli esemplari creati.
     *
     * @return this, per concatenare le registrazioni
     * @throws IllegalArgumentException se il nome è vuoto o la specie è già registrata
     */
    public RegistroSpecie registra(String specie, CreatoreAnimale creatore) {
        Objects.requireNonNull(specie, "specie");
        Objects.requireNonNull(creatore, "creatore");
        if (specie.isBlank()) {
            throw new IllegalArgumentException("Il nome della specie non può essere vuoto");
        }
        if (creatori.putIfAbsent(specie, creatore) != null) {
            throw new IllegalArgumentException("Specie già registrata: " + specie);
        }
        return this;
    }

    public boolean contiene(String specie) {
        return creatori.containsKey(specie);
    }

    /** @return i nomi delle specie registrate (vista non modificabile) */
    public Set<String> specie() {
        return Collections.unmodifiableSet(creatori.keySet());
    }

    /**
     * Crea un nuovo esemplare con id casuale.
     *
     * @throws IllegalArgumentException se la specie non è registrata
     */
    public Animale crea(String specie, String nome, RaritaEnum rarita, StatoAnimale stato) {
        return ricostruisci(specie, UUID.randomUUID(), nome, rarita, stato);
    }

    /**
     * Ricostruisce un esemplare già esistente (per esempio da un salvataggio).
     *
     * @throws IllegalArgumentException se la specie non è registrata
     * @throws IllegalStateException    se il creatore produce una specie diversa da quella registrata
     */
    public Animale ricostruisci(String specie, UUID id, String nome, RaritaEnum rarita, StatoAnimale stato) {
        CreatoreAnimale creatore = creatori.get(specie);
        if (creatore == null) {
            throw new IllegalArgumentException("Specie sconosciuta: " + specie);
        }
        Animale animale = creatore.crea(id, nome, rarita, stato);
        if (!specie.equals(animale.getSpecie())) {
            throw new IllegalStateException(
                    "Registrata come \"%s\" ma crea \"%s\"".formatted(specie, animale.getSpecie()));
        }
        return animale;
    }
}