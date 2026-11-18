package it.unicam.cs.mpgc.rpg130131.model.shop;

import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;

import java.util.List;
import java.util.Objects;

/**
 * Il negozio: vende gli articoli del catalogo e l'ampliamento dei recinti, in cambio di monete.
 * Non conosce lo zoo né il controller: riceve il giocatore che paga.
 */
public class Negozio {

    /** Posti guadagnati a ogni ampliamento di un recinto. */
    public static final int POSTI_PER_AMPLIAMENTO = 2;
    /** L'ampliamento costa questo valore per ogni posto che il recinto ha già. */
    public static final int PREZZO_PER_POSTO = 10;

    private final List<VoceNegozio> articoli;

    public Negozio(List<VoceNegozio> articoli) {
        this.articoli = List.copyOf(Objects.requireNonNull(articoli, "articoli"));
    }

    /** @return gli articoli in vendita (lista non modificabile) */
    public List<VoceNegozio> getArticoli() {
        return articoli;
    }

    /** Vende una unità dell'articolo, se è in vendita e il giocatore ha abbastanza monete. */
    public EsitoAcquisto compra(Giocatore giocatore, VoceNegozio articolo) {
        Objects.requireNonNull(giocatore, "giocatore");
        Objects.requireNonNull(articolo, "articolo");
        if (!articoli.contains(articolo)) {
            return EsitoAcquisto.NON_IN_VENDITA;
        }
        if (!giocatore.spendi(articolo.prezzo())) {
            return EsitoAcquisto.MONETE_INSUFFICIENTI;
        }
        giocatore.getInventario().aggiungi(articolo.oggetto(), 1);
        return EsitoAcquisto.ACQUISTATO;
    }

    /** @return il costo del prossimo ampliamento del recinto */
    public int prezzoAmpliamento(Recinto recinto) {
        return Objects.requireNonNull(recinto, "recinto").getCapienza() * PREZZO_PER_POSTO;
    }

    /** Amplia il recinto di {@link #POSTI_PER_AMPLIAMENTO} posti, se il giocatore ha abbastanza monete. */
    public EsitoAcquisto ampliaRecinto(Giocatore giocatore, Recinto recinto) {
        Objects.requireNonNull(giocatore, "giocatore");
        if (!giocatore.spendi(prezzoAmpliamento(recinto))) {
            return EsitoAcquisto.MONETE_INSUFFICIENTI;
        }
        recinto.amplia(POSTI_PER_AMPLIAMENTO);
        return EsitoAcquisto.ACQUISTATO;
    }
}