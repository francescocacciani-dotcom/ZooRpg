package it.unicam.cs.mpgc.rpg130131.model.catalog;

import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Scorta;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.shop.VoceNegozio;

import java.util.List;

/**
 * Tutto ciò che il gioco legge dai dati: animali che possono comparire, oggetti, articoli
 * del negozio e dotazione iniziale del giocatore.
 */
public record Catalogo(List<VoceAnimale> animali,
                       List<StrumentoCattura> strumenti,
                       List<Cibo> cibi,
                       List<Medicina> medicine,
                       List<VoceNegozio> negozio,
                       List<Scorta> kit) {

    public Catalogo {
        animali = List.copyOf(animali);
        strumenti = List.copyOf(strumenti);
        cibi = List.copyOf(cibi);
        medicine = List.copyOf(medicine);
        negozio = List.copyOf(negozio);
        kit = List.copyOf(kit);
    }

    /** Catalogo senza medicine, negozio né kit iniziale. */
    public Catalogo(List<VoceAnimale> animali, List<StrumentoCattura> strumenti, List<Cibo> cibi) {
        this(animali, strumenti, cibi, List.of(), List.of(), List.of());
    }
}