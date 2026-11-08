package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo;
import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.StatisticheZoo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Map;

/** Tutti i testi mostrati al giocatore, in un unico posto. Non dipende da JavaFX. */
public final class Messaggi {

    private Messaggi() {
    }

    public static String animale(Animale animale) {
        String stato = animale.getStato() == StatoAnimale.IN_PERICOLO ? ", in pericolo" : "";
        return "%s «%s» (%s%s)".formatted(
                animale.getSpecie(),
                animale.getNome(),
                animale.getRarita().name().toLowerCase(Locale.ROOT),
                stato);
    }

    public static String esito(EsitoTentativo esito) {
        return switch (esito.tipo()) {
            case NESSUN_ANIMALE -> "Qui non c'è nessun animale da catturare.";
            case STRUMENTO_NON_DISPONIBILE -> "Non hai più questo strumento.";
            case ESCA_NON_DISPONIBILE -> "Non hai più questa esca.";
            case ZOO_SENZA_POSTO ->
                    "Lo zoo non ha un recinto libero adatto per %s.".formatted(animale(esito.animale()));
            case FALLITO -> "Tentativo fallito (%d%%): %s non si è lasciato catturare. Puoi riprovare."
                    .formatted(percentuale(esito.probabilita()), animale(esito.animale()));
            case CATTURATO -> "Catturato! %s è nel tuo zoo (%d%%). «%s»%s"
                    .formatted(animale(esito.animale()), percentuale(esito.probabilita()),
                            esito.animale().getVerso(), ricompensa(esito.ricompensa()));
        };
    }

    public static String strumento(StrumentoCattura strumento, int quantita) {
        return "%s (+%d%%) ×%d".formatted(strumento.nome(), percentuale(strumento.bonusCattura()), quantita);
    }

    public static String cibo(Cibo cibo, int quantita) {
        if (cibo.equals(Cibo.NESSUNA_ESCA)) {
            return cibo.nome();
        }
        return "%s (+%d%%) ×%d".formatted(cibo.nome(), percentuale(cibo.bonusEsca()), quantita);
    }

    public static String recinto(Recinto recinto) {
        return "%s (%d/%d)".formatted(recinto.getNome(), recinto.getAnimali().size(), recinto.getCapienza());
    }

    public static String statistiche(StatisticheZoo statistiche) {
        String specie = statistiche.animaliPerSpecie().isEmpty()
                ? "Nessun animale ancora."
                : statistiche.animaliPerSpecie().entrySet().stream()
                .map(e -> e.getKey() + " ×" + e.getValue())
                .collect(Collectors.joining(", "));
        return "Animali: %d/%d · Punteggio: %d\n%s".formatted(
                statistiche.totaleAnimali(), statistiche.capienzaTotale(), statistiche.punteggio(), specie);
    }

    public static String anteprima(double probabilita) {
        return "Probabilità di cattura: %d%%".formatted(percentuale(probabilita));
    }

    private static String ricompensa(Map<Oggetto, Integer> premio) {
        if (premio.isEmpty()) {
            return "";
        }
        return " Ricompensa: " + premio.entrySet().stream()
                .map(e -> oggetto(e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ")) + ".";
    }

    private static String oggetto(Oggetto oggetto, int quantita) {
        if (oggetto instanceof StrumentoCattura strumento) {
            return strumento(strumento, quantita);
        }
        if (oggetto instanceof Cibo cibo) {
            return cibo(cibo, quantita);
        }
        return "%s ×%d".formatted(oggetto.nome(), quantita);
    }

    private static int percentuale(double valore) {
        return (int) Math.round(valore * 100);
    }
}