package it.unicam.cs.mpgc.rpg130131.view;

import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Fornisce le icone degli animali, lette da {@code /img/animali/<specie>.png} (nome della specie
 * in minuscolo). Ogni icona è caricata una sola volta; se il file manca il risultato è vuoto
 * e il chiamante usa un'alternativa.
 */
public class ImmaginiAnimali {

    private static final String CARTELLA = "/img/animali/";
    /** Le icone sono caricate al doppio dei pixel richiesti per restare nitide sugli schermi HiDPI. */
    private static final int RISOLUZIONE = 2;

    private record Chiave(String specie, int lato) {
    }

    private final Map<Chiave, Optional<Image>> cache = new HashMap<>();

    /**
     * @param lato lato dell'icona in pixel logici
     * @return l'icona della specie, o vuoto se non disponibile
     */
    public Optional<Image> perSpecie(String specie, int lato) {
        Objects.requireNonNull(specie, "specie");
        if (lato <= 0) {
            throw new IllegalArgumentException("Il lato deve essere positivo: " + lato);
        }
        return cache.computeIfAbsent(new Chiave(specie, lato), this::carica);
    }

    private Optional<Image> carica(Chiave chiave) {
        String percorso = CARTELLA + chiave.specie().toLowerCase(Locale.ROOT) + ".png";
        try (InputStream flusso = ImmaginiAnimali.class.getResourceAsStream(percorso)) {
            if (flusso == null) {
                return Optional.empty();
            }
            int pixel = chiave.lato() * RISOLUZIONE;
            Image immagine = new Image(flusso, pixel, pixel, true, true);
            return immagine.isError() ? Optional.empty() : Optional.of(immagine);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}