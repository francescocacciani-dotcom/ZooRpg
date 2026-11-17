package it.unicam.cs.mpgc.rpg130131.model.animals;

/** Rarità di un animale: influenza la difficoltà di cattura, il valore per lo zoo e le monete del rilascio. */
public enum RaritaEnum {
    COMUNE(1.0, 1),
    RARO(0.75, 3),
    EPICO(0.5, 7),
    LEGGENDARIO(0.25, 15);

    private static final int MONETE_PER_PUNTO = 10;

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

    /** Monete ricevute rilasciando un animale di questa rarità. */
    public int getMoneteRilascio() {
        return moltiplicatoreValore * MONETE_PER_PUNTO;
    }
}