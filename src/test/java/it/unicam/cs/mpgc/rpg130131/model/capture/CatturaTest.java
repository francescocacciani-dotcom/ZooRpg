package it.unicam.cs.mpgc.rpg130131.model.capture;

import it.unicam.cs.mpgc.rpg130131.model.animals.*;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class CatturaTest {

    private static final double EPS = 1e-9;
    private final CalcolatoreProbabilita calcolatore = new CalcolatoreProbabilitaStandard();
    private final StrumentoCattura rete = new StrumentoCattura("Rete", 0.1);

    @Test
    void formulaBase() {
        Animale leone = new Leone("Simba", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        // (1 - 0.7) * 1.0 * 1.0 + 0.1
        assertEquals(0.4, calcolatore.calcola(leone, rete, Cibo.NESSUNA_ESCA), EPS);
    }

    @Test
    void esevaEraRaritaInfluiscono() {
        Animale leone = new Leone("Simba", RaritaEnum.EPICO, StatoAnimale.SELVATICO);
        Cibo carne = new Cibo("Carne", 0.15);
        // (1 - 0.7) * 0.5 + 0.1 + 0.15
        assertEquals(0.4, calcolatore.calcola(leone, rete, carne), EPS);
    }

    @Test
    void animaleInPericoloEPiuFacile() {
        Animale selvatico = new Leone("A", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        Animale inPericolo = new Leone("B", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO);
        double ps = calcolatore.calcola(selvatico, rete, Cibo.NESSUNA_ESCA);
        double pp = calcolatore.calcola(inPericolo, rete, Cibo.NESSUNA_ESCA);
        assertTrue(pp > ps);
        assertEquals(0.3 * 1.25 + 0.1, pp, EPS);
    }

    @Test
    void probabilitaLimitataAgliEstremi() {
        Animale difficile = new Leone("A", RaritaEnum.LEGGENDARIO, StatoAnimale.SELVATICO);
        Animale facile = new Capibara("B", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO);
        StrumentoCattura mani = new StrumentoCattura("Mani nude", 0.0);
        assertEquals(CalcolatoreProbabilitaStandard.PROBABILITA_MINIMA,
                calcolatore.calcola(difficile, mani, Cibo.NESSUNA_ESCA), EPS);
        assertEquals(CalcolatoreProbabilitaStandard.PROBABILITA_MASSIMA,
                calcolatore.calcola(facile, rete, Cibo.NESSUNA_ESCA), EPS);
    }

    @Test
    void engineRiuscitaConDadoBasso() {
        RandomGenerator dadoBasso = () -> 0L; // nextDouble() = 0.0
        EngineCattura engine = new EngineCattura((a, s, e) -> 0.5, dadoBasso);
        RisultatoCattura r = engine.tenta(
                new Giraffa("Gigi", RaritaEnum.COMUNE, StatoAnimale.SELVATICO), rete);
        assertTrue(r.riuscita());
        assertEquals(0.5, r.probabilita(), EPS);
    }

    @Test
    void engineFallimentoConDadoAlto() {
        RandomGenerator dadoAlto = () -> -1L; // nextDouble() ≈ 0.99999
        EngineCattura engine = new EngineCattura(calcolatore, dadoAlto);
        RisultatoCattura r = engine.tenta(
                new Giraffa("Gigi", RaritaEnum.COMUNE, StatoAnimale.SELVATICO), rete);
        assertFalse(r.riuscita());
    }

    @Test
    void engineRifiutaProbabilitaNonValida() {
        EngineCattura engine = new EngineCattura((a, s, e) -> 1.5, () -> 0L);
        assertThrows(IllegalStateException.class, () -> engine.tenta(
                new Giraffa("Gigi", RaritaEnum.COMUNE, StatoAnimale.SELVATICO), rete));
    }
}