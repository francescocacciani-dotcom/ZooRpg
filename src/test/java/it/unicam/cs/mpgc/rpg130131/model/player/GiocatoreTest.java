package it.unicam.cs.mpgc.rpg130131.model.player;

import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GiocatoreTest {

    private Giocatore giocatore(int monete) {
        return new Giocatore("Tester", new Posizione(0, 0), new Inventario(), monete);
    }

    @Test
    void senzaMonetePartiDaZero() {
        assertEquals(0, new Giocatore("T", new Posizione(0, 0), new Inventario()).getMonete());
    }

    @Test
    void aggiungiESpendi() {
        Giocatore g = giocatore(10);
        g.aggiungiMonete(5);
        assertEquals(15, g.getMonete());

        assertTrue(g.spendi(15));
        assertEquals(0, g.getMonete());
    }

    @Test
    void spendereTroppoNonCambiaNulla() {
        Giocatore g = giocatore(10);

        assertFalse(g.spendi(11));

        assertEquals(10, g.getMonete());
    }

    @Test
    void importiNonValidiRifiutati() {
        Giocatore g = giocatore(10);
        assertThrows(IllegalArgumentException.class, () -> g.aggiungiMonete(0));
        assertThrows(IllegalArgumentException.class, () -> g.spendi(-1));
        assertThrows(IllegalArgumentException.class, () -> giocatore(-5));
    }
}