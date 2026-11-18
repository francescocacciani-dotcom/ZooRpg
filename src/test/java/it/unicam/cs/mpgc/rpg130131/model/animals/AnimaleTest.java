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

    @Test
    void laVitaInizialeDipendeDallaCondizione() {
        assertEquals(Animale.VITA_MASSIMA,
                new Leone("A", RaritaEnum.COMUNE, StatoAnimale.SELVATICO).getVita());
        assertTrue(new Leone("B", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO).getVita()
                < Animale.SOGLIA_PERICOLO);
    }

    @Test
    void sottoLaSogliaLAnimaleEInPericolo() {
        Animale leone = new Leone("Simba", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);

        leone.impostaVita(Animale.SOGLIA_PERICOLO - 1);
        assertEquals(StatoAnimale.IN_PERICOLO, leone.getStato());

        leone.impostaVita(Animale.SOGLIA_PERICOLO);
        assertEquals(StatoAnimale.SELVATICO, leone.getStato());
    }

    @Test
    void curaRestituisceIPuntiEffettivamenteRecuperati() {
        Animale leone = new Leone("Simba", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        leone.impostaVita(90);

        assertEquals(10, leone.cura(25));
        assertEquals(Animale.VITA_MASSIMA, leone.getVita());
        assertEquals(0, leone.cura(5));
    }

    @Test
    void vitaFuoriRangeRifiutata() {
        Animale leone = new Leone("Simba", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        assertThrows(IllegalArgumentException.class, () -> leone.impostaVita(-1));
        assertThrows(IllegalArgumentException.class, () -> leone.impostaVita(Animale.VITA_MASSIMA + 1));
        assertThrows(IllegalArgumentException.class, () -> leone.cura(0));
    }
}