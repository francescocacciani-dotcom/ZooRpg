package it.unicam.cs.mpgc.rpg130131.model.animals;

import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import java.util.UUID;

public final class Orso extends Animale {

    public Orso(String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(nome, rarita, stato);
    }

    public Orso(UUID id, String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(id, nome, rarita, stato);
    }

    @Override public String getSpecie() { return "Orso"; }
    @Override public BiomaEnum getBiomaNativo() { return BiomaEnum.FORESTA; }
    @Override public double getDifficoltaBase() { return 0.6; }
    @Override public String getVerso() { return "Grrrr!"; }
}