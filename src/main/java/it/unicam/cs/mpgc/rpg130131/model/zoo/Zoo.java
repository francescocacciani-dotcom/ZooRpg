package it.unicam.cs.mpgc.rpg130131.model.zoo;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Lo zoo del giocatore: recinti che accolgono, ospitano, curano e rilasciano gli animali. */
public class Zoo {

    /** Il cibo cura solo animali con una vita strettamente superiore a questo valore. */
    public static final int VITA_MINIMA_PER_CIBO = 80;
    private final String nome;
    private final List<Recinto> recinti = new ArrayList<>();

    public Zoo(String nome) {
        Objects.requireNonNull(nome, "nome");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void aggiungiRecinto(Recinto recinto) {
        recinti.add(Objects.requireNonNull(recinto, "recinto"));
    }

    /** @return vista non modificabile dei recinti */
    public List<Recinto> getRecinti() {
        return Collections.unmodifiableList(recinti);
    }

    /** @return tutti gli animali ospitati nello zoo */
    public List<Animale> getAnimali() {
        return recinti.stream().flatMap(r -> r.getAnimali().stream()).toList();
    }

    /** @return true se l'animale non è già nello zoo e esiste un recinto adatto con posto libero */
    public boolean puoAccogliere(Animale animale) {
        return trovaRecinto(animale).isPresent();
    }

    /**
     * Inserisce l'animale nel primo recinto adatto.
     *
     * @return il recinto scelto, oppure vuoto se l'animale non può essere accolto
     */
    public Optional<Recinto> accogli(Animale animale) {
        Optional<Recinto> recinto = trovaRecinto(animale);
        recinto.ifPresent(r -> r.aggiungi(animale));
        return recinto;
    }

    /**
     * Toglie l'animale dallo zoo.
     *
     * @return true se l'animale era nello zoo ed è stato rimosso
     */
    public boolean rilascia(Animale animale) {
        Objects.requireNonNull(animale, "animale");
        for (Recinto recinto : recinti) {
            if (recinto.rimuovi(animale)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cura un animale con una medicina o con del cibo. Le medicine funzionano sempre; il cibo solo
     * se la vita dell'animale è sopra {@link #VITA_MINIMA_PER_CIBO}. Non consuma l'oggetto:
     * lo fa chi coordina il gioco.
     *
     * @throws IllegalArgumentException se l'animale non è nello zoo
     */
    public RisultatoCura cura(Animale animale, Oggetto curativo) {
        Objects.requireNonNull(animale, "animale");
        Objects.requireNonNull(curativo, "curativo");
        if (!getAnimali().contains(animale)) {
            throw new IllegalArgumentException("L'animale non è nello zoo: " + animale);
        }
        int punti = puntiCura(curativo);
        if (punti == 0) {
            return new RisultatoCura(EsitoCura.NON_CURATIVO, 0);
        }
        if (animale.getVita() >= Animale.VITA_MASSIMA) {
            return new RisultatoCura(EsitoCura.GIA_IN_SALUTE, 0);
        }
        if (curativo instanceof Cibo && animale.getVita() <= VITA_MINIMA_PER_CIBO) {
            return new RisultatoCura(EsitoCura.CIBO_NON_ADATTO, 0);
        }
        return new RisultatoCura(EsitoCura.CURATO, animale.cura(punti));
    }

    public StatisticheZoo getStatistiche() {
        return StatisticheZoo.calcola(recinti);
    }

    private static int puntiCura(Oggetto oggetto) {
        //TODO controllare se viene passato o una medicina o un cibo altrimenti un throws
        return switch (oggetto) {
            case Medicina medicina -> medicina.puntiVita();
            case Cibo cibo -> cibo.puntiVita();
            default -> 0;
        };
    }

    private Optional<Recinto> trovaRecinto(Animale animale) {
        Objects.requireNonNull(animale, "animale");
        if (getAnimali().contains(animale)) {
            return Optional.empty();
        }
        return recinti.stream().filter(r -> r.puoOspitare(animale)).findFirst();
    }
}