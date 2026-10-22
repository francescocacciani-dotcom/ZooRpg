package it.unicam.cs.mpgc.rpg130131.model.map;

// tipi di ambiente disponibili nel gioco

public enum BiomaEnum {
    SAVANA("Savana"),
    FORESTA("Foresta"),
    PALUDE("Palude"),
    DESERTO("Deserto");

    private final String nomeVisualizzato;

    BiomaEnum(String nomeVisualizzato) {
        this.nomeVisualizzato = nomeVisualizzato;
    }

    public String getNomeVisualizzato() {
        return nomeVisualizzato;
    }
}
