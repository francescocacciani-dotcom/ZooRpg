package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo;
import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.shop.EsitoAcquisto;
import it.unicam.cs.mpgc.rpg130131.model.shop.VoceNegozio;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.RisultatoCura;
import it.unicam.cs.mpgc.rpg130131.model.zoo.StatisticheZoo;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Tutti i testi mostrati al giocatore, in un unico posto. Non dipende da JavaFX. */
public final class Messaggi {

    private Messaggi() {
    }

    // ---------- animali e cattura ----------

    public static String animale(Animale animale) {
        String stato = animale.getStato() == StatoAnimale.IN_PERICOLO ? ", in pericolo" : "";
        return "%s «%s» (%s%s), vita %d/%d".formatted(
                animale.getSpecie(),
                animale.getNome(),
                animale.getRarita().name().toLowerCase(Locale.ROOT),
                stato,
                animale.getVita(),
                Animale.VITA_MASSIMA);
    }

    public static String esito(EsitoTentativo esito) {
        return switch (esito.tipo()) {
            case NESSUN_ANIMALE -> "Qui non c'è nessun animale da catturare.";
            case STRUMENTO_NON_DISPONIBILE -> "Non hai più questo strumento.";
            case ESCA_NON_DISPONIBILE -> "Non hai più questa esca.";
            case ZOO_SENZA_POSTO -> ("Lo zoo non ha un recinto libero adatto per %s. "
                    + "Rilascia un animale o amplia un recinto dal Negozio.").formatted(animale(esito.animale()));
            case FALLITO -> "Tentativo fallito (%d%%): %s non si è lasciato catturare. Puoi riprovare."
                    .formatted(percentuale(esito.probabilita()), animale(esito.animale()));
            case CATTURATO -> "Catturato! %s è nel tuo zoo (%d%%). «%s»%s"
                    .formatted(animale(esito.animale()), percentuale(esito.probabilita()),
                            esito.animale().getVerso(), ricompensa(esito.ricompensa()));
        };
    }

    public static String anteprima(double probabilita) {
        return "Probabilità di cattura: %d%%".formatted(percentuale(probabilita));
    }

    // ---------- oggetti ----------

    /** Descrizione di un oggetto, senza quantità. */
    public static String descrizione(Oggetto oggetto) {
        return switch (oggetto) {
            case StrumentoCattura strumento ->
                    "%s (+%d%%)".formatted(strumento.nome(), percentuale(strumento.bonusCattura()));
            case Cibo cibo -> {
                if (cibo.equals(Cibo.NESSUNA_ESCA)) {
                    yield cibo.nome();
                }
                String cura = cibo.puntiVita() > 0 ? ", +%d vita".formatted(cibo.puntiVita()) : "";
                yield "%s (+%d%%%s)".formatted(cibo.nome(), percentuale(cibo.bonusEsca()), cura);
            }
            case Medicina medicina -> "%s (+%d vita)".formatted(medicina.nome(), medicina.puntiVita());
            default -> oggetto.nome();
        };
    }

    /** Descrizione di un oggetto con la quantità posseduta. */
    public static String oggetto(Oggetto oggetto, int quantita) {
        if (oggetto.equals(Cibo.NESSUNA_ESCA)) {
            return descrizione(oggetto);
        }
        return "%s ×%d".formatted(descrizione(oggetto), quantita);
    }

    public static String strumento(StrumentoCattura strumento, int quantita) {
        return oggetto(strumento, quantita);
    }

    public static String cibo(Cibo cibo, int quantita) {
        return oggetto(cibo, quantita);
    }

    // ---------- zoo, rilascio e cure ----------

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

    public static String rilascio(Animale animale, int monete) {
        return "Hai rilasciato %s. Ricevi %d monete.".formatted(animale(animale), monete);
    }

    public static String cura(Animale animale, Oggetto curativo, RisultatoCura risultato) {
        return switch (risultato.esito()) {
            case CURATO -> "%s ha recuperato %d punti vita con %s."
                    .formatted(animale.getNome(), risultato.puntiRecuperati(), curativo.nome());
            case GIA_IN_SALUTE -> "%s è già in perfetta salute.".formatted(animale.getNome());
            case CIBO_NON_ADATTO -> "Il cibo cura solo se la vita è sopra %d: per %s serve una medicina."
                    .formatted(Zoo.VITA_MINIMA_PER_CIBO, animale.getNome());
            case NON_CURATIVO -> "%s non serve a curare.".formatted(curativo.nome());
            case NON_POSSEDUTO -> "Non hai più %s.".formatted(curativo.nome());
        };
    }

    // ---------- monete e negozio ----------

    public static String monete(int monete) {
        return "Monete: " + monete;
    }

    public static String articolo(VoceNegozio articolo) {
        return "%s — %d monete".formatted(descrizione(articolo.oggetto()), articolo.prezzo());
    }

    public static String ampliamento(Recinto recinto, int posti, int prezzo) {
        return "%s — +%d posti per %d monete".formatted(recinto(recinto), posti, prezzo);
    }

    public static String acquisto(EsitoAcquisto esito, String cosa, int prezzo) {
        return switch (esito) {
            case ACQUISTATO -> "Acquistato: %s (-%d monete).".formatted(cosa, prezzo);
            case MONETE_INSUFFICIENTI -> "Monete insufficienti per %s (servono %d).".formatted(cosa, prezzo);
            case NON_IN_VENDITA -> "%s non è in vendita.".formatted(cosa);
        };
    }

    // ---------- supporto ----------

    private static String ricompensa(Map<Oggetto, Integer> premio) {
        if (premio.isEmpty()) {
            return "";
        }
        return " Ricompensa: " + premio.entrySet().stream()
                .map(e -> oggetto(e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ")) + ".";
    }

    private static int percentuale(double valore) {
        return (int) Math.round(valore * 100);
    }
}