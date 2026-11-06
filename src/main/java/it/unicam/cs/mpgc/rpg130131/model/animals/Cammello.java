package it.unicam.cs.mpgc.rpg130131.model.animals;

import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import java.util.UUID;

public final class Cammello extends Animale {

    public Cammello(String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(nome, rarita, stato);
    }

    public Cammello(UUID id, String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(id, nome, rarita, stato);
    }

    @Override public String getSpecie() { return "Cammello"; }
    @Override public BiomaEnum getBiomaNativo() { return BiomaEnum.DESERTO; }
    @Override public double getDifficoltaBase() { return 0.3; }
    @Override public String getVerso() { return "Groooo!"; }
}