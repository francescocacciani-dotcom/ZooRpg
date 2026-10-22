package it.unicam.cs.mpgc.rpg130131.model.animals;

import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import java.util.UUID;

public final class Leone extends Animale {

    public Leone(String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(nome, rarita, stato);
    }

    public Leone(UUID id, String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(id, nome, rarita, stato);
    }

    @Override public String getSpecie() { return "Leone"; }
    @Override public BiomaEnum getBiomaNativo() { return BiomaEnum.SAVANA; }
    @Override public double getDifficoltaBase() { return 0.7; }
    @Override public String getVerso() { return "Roarrr!"; }
}