package it.unicam.cs.mpgc.rpg130131.model.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class Mappa2DTest {

    private Mappa2D mappa;

    @BeforeEach
    void setUp() {
        // 4 colonne x 3 righe: colonne 0-1 savana, colonne 2-3 palude
        mappa = new Mappa2D(4, 3, p -> p.x() < 2 ? BiomaEnum.SAVANA : BiomaEnum.PALUDE);
    }

    @Test
    void dimensioni() {
        assertEquals(4, mappa.getLarghezza());
        assertEquals(3, mappa.getAltezza());
    }

    @Test
    void dimensioniNonValide() {
        assertThrows(IllegalArgumentException.class, () -> new Mappa2D(0, 3, p -> BiomaEnum.SAVANA));
        assertThrows(IllegalArgumentException.class, () -> new Mappa2D(3, -1, p -> BiomaEnum.SAVANA));
    }

    @Test
    void generatoreNulloNonAmmesso() {
        assertThrows(NullPointerException.class, () -> new Mappa2D(2, 2, null));
        assertThrows(NullPointerException.class, () -> new Mappa2D(2, 2, p -> null));
    }

    @Test
    void contieneRispettaIBordi() {
        assertTrue(mappa.contiene(new Posizione(0, 0)));
        assertTrue(mappa.contiene(new Posizione(3, 2)));
        assertFalse(mappa.contiene(new Posizione(4, 0)));
        assertFalse(mappa.contiene(new Posizione(0, 3)));
        assertFalse(mappa.contiene(new Posizione(-1, 1)));
    }

    @Test
    void cellaHaPosizioneEBiomaCorretti() {
        Cella c = mappa.getCella(new Posizione(3, 1));
        assertEquals(new Posizione(3, 1), c.posizione());
        assertEquals(BiomaEnum.PALUDE, c.bioma());
        assertEquals(BiomaEnum.SAVANA, mappa.getCella(new Posizione(1, 2)).bioma());
    }

    @Test
    void cellaFuoriMappaLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> mappa.getCella(new Posizione(4, 0)));
    }

    @Test
    void destinazioneDentroELimiti() {
        assertEquals(Optional.of(new Posizione(1, 0)),
                mappa.destinazione(new Posizione(0, 0), Direzione.EST));
        assertEquals(Optional.empty(), mappa.destinazione(new Posizione(0, 0), Direzione.NORD));
        assertEquals(Optional.empty(), mappa.destinazione(new Posizione(3, 2), Direzione.EST));
    }

    @Test
    void viciniAdAngoloEInCentro() {
        List<Posizione> angolo = mappa.vicini(new Posizione(0, 0));
        assertEquals(2, angolo.size());
        assertTrue(angolo.containsAll(List.of(new Posizione(1, 0), new Posizione(0, 1))));
        assertEquals(4, mappa.vicini(new Posizione(1, 1)).size());
    }

    @Test
    void celleInOrdineDiRiga() {
        List<Cella> tutte = mappa.celle().toList();
        assertEquals(12, tutte.size());
        assertEquals(new Posizione(0, 0), tutte.get(0).posizione());
        assertEquals(new Posizione(1, 0), tutte.get(1).posizione());
        assertEquals(new Posizione(0, 1), tutte.get(4).posizione());
        assertEquals(new Posizione(3, 2), tutte.get(11).posizione());
    }

    @Test
    void celleDiBioma() {
        assertEquals(6, mappa.celleDiBioma(BiomaEnum.SAVANA).size());
        assertEquals(6, mappa.celleDiBioma(BiomaEnum.PALUDE).size());
        assertTrue(mappa.celleDiBioma(BiomaEnum.DESERTO).isEmpty());
    }

    @Test
    void posizioneVersoDirezione() {
        assertEquals(new Posizione(2, 1), new Posizione(2, 2).verso(Direzione.NORD));
    }
}