package it.unicam.cs.mpgc.rpg130131.model.animals;

import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimaleTest {

    @Test
    void laSpecieDefinisceLeProprieCaratteristiche() {
        Animale leone = new Leone("Simba", RaritaEnum.RARO, StatoAnimale.SELVATICO);
        assertEquals("Leone", leone.getSpecie());
        assertEquals(BiomaEnum.SAVANA, leone.getBiomaNativo());
        assertEquals(RaritaEnum.RARO, leone.getRarita());
    }

    @Test
    void ognEsemplareHaUnIdentitaPropria() {
        Animale a = new Capibara("Cappy", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        Animale b = new Capibara("Cappy", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        assertNotEquals(a, b);
        assertEquals(a, new Capibara(a.getId(), "Cappy", RaritaEnum.COMUNE, StatoAnimale.SELVATICO));
    }

    @Test
    void nomeVuotoNonAmmesso() {
        assertThrows(IllegalArgumentException.class,
                () -> new Giraffa("  ", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO));
    }

    @Test
    void parametriNulliNonAmmessi() {
        assertThrows(NullPointerException.class,
                () -> new Giraffa("Gigi", null, StatoAnimale.SELVATICO));
    }

    @Test
    void difficoltaBaseInRange() {
        for (Animale a : new Animale[]{
                new Capibara("a", RaritaEnum.COMUNE, StatoAnimale.SELVATICO),
                new Leone("b", RaritaEnum.COMUNE, StatoAnimale.SELVATICO),
                new Giraffa("c", RaritaEnum.COMUNE, StatoAnimale.SELVATICO)}) {
            assertTrue(a.getDifficoltaBase() >= 0 && a.getDifficoltaBase() <= 1);
        }
    }
}