package it.unicam.cs.mpgc.rpg130131.model.catalog;

import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;

import java.util.List;

/** Tutto ciò che il gioco legge dai dati: animali che possono comparire, strumenti e cibi. */
public record Catalogo(List<VoceAnimale> animali, List<StrumentoCattura> strumenti, List<Cibo> cibi) {

    public Catalogo {
        animali = List.copyOf(animali);
        strumenti = List.copyOf(strumenti);
        cibi = List.copyOf(cibi);
    }
}