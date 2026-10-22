package it.unicam.cs.mpgc.rpg130131.model.animals;

import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import java.util.UUID;

public final class Capibara extends Animale {

    public Capibara(String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(nome, rarita, stato);
    }

    public Capibara(UUID id, String nome, RaritaEnum rarita, StatoAnimale stato) {
        super(id, nome, rarita, stato);
    }

    @Override public String getSpecie() { return "Capibara"; }
    @Override public BiomaEnum getBiomaNativo() { return BiomaEnum.PALUDE; }
    @Override public double getDifficoltaBase() { return 0.2; }
    @Override public String getVerso() { return "Fiiii!"; }
}