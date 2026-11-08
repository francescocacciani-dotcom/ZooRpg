package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo;
import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo.Tipo;
import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.Giraffa;
import it.unicam.cs.mpgc.rpg130131.model.animals.Leone;
import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.zoo.StatisticheZoo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessaggiTest {

    private final Animale leone = new Leone("Leo", RaritaEnum.RARO, StatoAnimale.IN_PERICOLO);

    @Test
    void descrizioneAnimaleInPericolo() {
        String testo = Messaggi.animale(leone);
        assertTrue(testo.contains("Leone"));
        assertTrue(testo.contains("Leo"));
        assertTrue(testo.contains("raro"));
        assertTrue(testo.contains("in pericolo"));
    }

    @Test
    void animaleSelvaticoNonVieneDettoInPericolo() {
        Animale giraffa = new Giraffa("Gigi", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        assertFalse(Messaggi.animale(giraffa).contains("in pericolo"));
    }

    @Test
    void ognEsitoHaUnMessaggio() {
        for (Tipo tipo : Tipo.values()) {
            Animale animale = tipo == Tipo.NESSUN_ANIMALE ? null : leone;
            assertFalse(Messaggi.esito(new EsitoTentativo(tipo, animale, 0.5)).isBlank(), tipo.name());
        }
    }

    @Test
    void catturaMostraNomeEProbabilita() {
        String testo = Messaggi.esito(new EsitoTentativo(Tipo.CATTURATO, leone, 0.45));
        assertTrue(testo.contains("Catturato!"));
        assertTrue(testo.contains("Leo"));
        assertTrue(testo.contains("45%"));
    }

    @Test
    void etichetteDegliOggetti() {
        assertEquals("Rete (+10%) ×3", Messaggi.strumento(new StrumentoCattura("Rete", 0.1), 3));
        assertEquals("Carne (+15%) ×2", Messaggi.cibo(new Cibo("Carne", 0.15), 2));
        assertEquals("Nessuna esca", Messaggi.cibo(Cibo.NESSUNA_ESCA, 0));
    }

    @Test
    void statisticheDiUnoZooVuoto() {
        String testo = Messaggi.statistiche(StatisticheZoo.calcola(List.of()));
        assertTrue(testo.contains("Animali: 0/0"));
        assertTrue(testo.contains("Nessun animale ancora"));
    }

    @Test
    void catturaMostraLaRicompensa() {
        EsitoTentativo esito = new EsitoTentativo(Tipo.CATTURATO, leone, 0.45,
                Map.of(new StrumentoCattura("Rete", 0.1), 2));
        assertTrue(Messaggi.esito(esito).contains("Ricompensa: Rete (+10%) ×2"));
    }

    @Test
    void anteprimaMostraLaPercentuale() {
        assertEquals("Probabilità di cattura: 40%", Messaggi.anteprima(0.4));
    }
}