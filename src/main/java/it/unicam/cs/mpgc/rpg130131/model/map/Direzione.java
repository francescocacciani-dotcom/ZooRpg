package it.unicam.cs.mpgc.rpg130131.model.map;

public enum Direzione {
    NORD(0, -1),
    SUD(0, 1),
    EST(1, 0),
    OVEST(-1, 0);

    private final int dx;
    private final int dy;

    Direzione(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
