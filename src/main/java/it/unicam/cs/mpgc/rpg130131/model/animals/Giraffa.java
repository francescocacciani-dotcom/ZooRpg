package it.unicam.cs.mpgc.rpg130131.model.animals;

import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import java.util.UUID;

public final class Giraffa extends Animale {

    public Giraffa(String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(nome, rarita, stato);
    }

    public Giraffa(UUID id, String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(id, nome, rarita, stato);
    }

    @Override public String getSpecie() { return "Giraffa"; }
    @Override public BiomaEnum getBiomaNativo() { return BiomaEnum.SAVANA; }
    @Override public double getDifficoltaBase() { return 0.5; }
    @Override public String getVerso() { return "Mmmh..."; }
}