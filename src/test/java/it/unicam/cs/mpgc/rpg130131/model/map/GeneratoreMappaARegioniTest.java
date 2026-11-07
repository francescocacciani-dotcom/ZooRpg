package it.unicam.cs.mpgc.rpg130131.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratoreMappaARegioniTest {

    private final GeneratoreMappa generatore = new GeneratoreMappaARegioni();

    @Test
    void laMappaHaLeDimensioniRichieste() {
        Mappa2D mappa = generatore.genera(12, 8);
        assertEquals(12, mappa.getLarghezza());
        assertEquals(8, mappa.getAltezza());
    }

    @Test
    void ognibiomaEPresente() {
        Mappa2D mappa = generatore.genera(12, 8);
        for (BiomaEnum bioma : BiomaEnum.values()) {
            assertFalse(mappa.celleDiBioma(bioma).isEmpty(), "Manca il bioma " + bioma);
        }
    }

    @Test
    void ognibiomaEPresenteAncheInUnaMappaMinima() {
        Mappa2D mappa = generatore.genera(2, 2);
        for (BiomaEnum bioma : BiomaEnum.values()) {
            assertEquals(1, mappa.celleDiBioma(bioma).size());
        }
    }

    @Test
    void gliAngoliHannoIBiomiPrevisti() {
        Mappa2D mappa = generatore.genera(12, 8);
        assertEquals(BiomaEnum.SAVANA, mappa.getCella(new Posizione(0, 0)).bioma());
        assertEquals(BiomaEnum.FORESTA, mappa.getCella(new Posizione(11, 0)).bioma());
        assertEquals(BiomaEnum.PALUDE, mappa.getCella(new Posizione(0, 7)).bioma());
        assertEquals(BiomaEnum.DESERTO, mappa.getCella(new Posizione(11, 7)).bioma());
    }

    @Test
    void laGenerazioneEDeterministica() {
        assertEquals(generatore.genera(12, 8).celle().toList(), generatore.genera(12, 8).celle().toList());
    }

    @Test
    void dimensioniNonValideRifiutate() {
        assertThrows(IllegalArgumentException.class, () -> generatore.genera(0, 5));
        assertThrows(IllegalArgumentException.class, () -> generatore.genera(5, -1));
    }
}