package it.unicam.cs.mpgc.rpg130131.model.animals;

// la rarità di un animale influenza la sua cattura

public enum RaritaEnum {
    COMUNE(1.0, 1),
    RARO(0.75, 3),
    EPICO(0.5, 7),
    LEGGENDARIO(0.25, 15);

    private final double fattoreCattura;
    private final int moltiplicatoreValore;

    RaritaEnum(double fattoreCattura, int moltiplicatoreValore) {
        this.fattoreCattura = fattoreCattura;
        this.moltiplicatoreValore = moltiplicatoreValore;
    }

    /** Coefficiente in (0, 1] da applicare alla probabilità di cattura. */
    public double getFattoreCattura() {
        return fattoreCattura;
    }

    public int getMoltiplicatoreValore() {
        return moltiplicatoreValore;
    }
}
