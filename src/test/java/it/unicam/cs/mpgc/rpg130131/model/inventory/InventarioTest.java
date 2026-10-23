package it.unicam.cs.mpgc.rpg130131.model.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventarioTest {

    private final StrumentoCattura rete = new StrumentoCattura("Rete", 0.1);
    private final Cibo carne = new Cibo("Carne", 0.15);
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        inventario = new Inventario();
    }

    @Test
    void aggiungiImpilaLeQuantita() {
        inventario.aggiungi(rete, 2);
        inventario.aggiungi(new StrumentoCattura("Rete", 0.1), 3);
        assertEquals(5, inventario.quantita(rete));
    }

    @Test
    void usaConsumaUnEsemplareAllaVolta() {
        inventario.aggiungi(rete, 2);
        assertTrue(inventario.usa(rete));
        assertEquals(1, inventario.quantita(rete));
        assertTrue(inventario.usa(rete));
        assertFalse(inventario.contiene(rete));
        assertFalse(inventario.usa(rete));
    }

    @Test
    void quantitaNonValida() {
        assertThrows(IllegalArgumentException.class, () -> inventario.aggiungi(rete, 0));
        assertThrows(IllegalArgumentException.class, () -> inventario.aggiungi(rete, -1));
    }

    @Test
    void oggettoMancanteHaQuantitaZero() {
        assertEquals(0, inventario.quantita(carne));
    }

    @Test
    void contenutoDiFiltraPerTipo() {
        inventario.aggiungi(rete, 2);
        inventario.aggiungi(carne, 4);
        assertEquals(1, inventario.getContenutoDi(StrumentoCattura.class).size());
        assertEquals(4, inventario.getContenutoDi(Cibo.class).get(carne));
        assertEquals(2, inventario.getContenuto().size());
    }

    @Test
    void contenutoNonModificabileDall_Esterno() {
        assertThrows(UnsupportedOperationException.class,
                () -> inventario.getContenuto().put(rete, 1));
    }

    @Test
    void oggettiNonValidi() {
        assertThrows(IllegalArgumentException.class, () -> new StrumentoCattura(" ", 0.1));
        assertThrows(IllegalArgumentException.class, () -> new StrumentoCattura("X", 1.5));
        assertThrows(IllegalArgumentException.class, () -> new Cibo("X", -0.1));
    }
}