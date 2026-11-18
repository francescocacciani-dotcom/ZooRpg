package it.unicam.cs.mpgc.rpg130131.model.zoo;

import it.unicam.cs.mpgc.rpg130131.model.animals.*;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ZooTest {

    private Zoo zoo;
    private Recinto savana;
    private Recinto palude;

    @BeforeEach
    void setUp() {
        zoo = new Zoo("Zoo di prova");
        savana = new Recinto("Savana", BiomaEnum.SAVANA, 3);
        palude = new Recinto("Palude", BiomaEnum.PALUDE, 2);
        zoo.aggiungiRecinto(savana);
        zoo.aggiungiRecinto(palude);
    }

    @Test
    void recintoNonValido() {
        assertThrows(IllegalArgumentException.class, () -> new Recinto(" ", BiomaEnum.SAVANA, 2));
        assertThrows(IllegalArgumentException.class, () -> new Recinto("R", BiomaEnum.SAVANA, 0));
    }

    @Test
    void accogliePlaceNelRecintoDelBiomaGiusto() {
        Animale leone = new Leone("Simba", RaritaEnum.RARO, StatoAnimale.SELVATICO);
        assertEquals(Optional.of(savana), zoo.accogli(leone));
        assertTrue(savana.getAnimali().contains(leone));
        assertEquals(2, savana.getPostiLiberi());
    }

    @Test
    void rifiutaSeNonCeRecintoAdatto() {
        Zoo soloPalude = new Zoo("Solo palude");
        soloPalude.aggiungiRecinto(palude);
        Animale leone = new Leone("Simba", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        assertFalse(soloPalude.puoAccogliere(leone));
        assertEquals(Optional.empty(), soloPalude.accogli(leone));
    }

    @Test
    void rifiutaSeRecintoPieno() {
        for (int i = 0; i < 3; i++) {
            zoo.accogli(new Giraffa("G" + i, RaritaEnum.COMUNE, StatoAnimale.SELVATICO));
        }
        assertTrue(savana.isPieno());
        assertEquals(Optional.empty(),
                zoo.accogli(new Leone("Extra", RaritaEnum.COMUNE, StatoAnimale.SELVATICO)));
    }

    @Test
    void nonAccoglieDueVolteLoStessoAnimale() {
        Animale capibara = new Capibara("Cappy", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO);
        assertTrue(zoo.accogli(capibara).isPresent());
        assertFalse(zoo.puoAccogliere(capibara));
        assertThrows(IllegalStateException.class, () -> palude.aggiungi(capibara));
    }

    @Test
    void rimuoviLiberaIlPosto() {
        Animale capibara = new Capibara("Cappy", RaritaEnum.COMUNE, StatoAnimale.SELVATICO);
        zoo.accogli(capibara);
        assertTrue(palude.rimuovi(capibara));
        assertEquals(2, palude.getPostiLiberi());
        assertFalse(palude.rimuovi(capibara));
    }

    @Test
    void statisticheCorrette() {
        zoo.accogli(new Leone("Simba", RaritaEnum.RARO, StatoAnimale.SELVATICO));
        zoo.accogli(new Giraffa("Gigi", RaritaEnum.COMUNE, StatoAnimale.SELVATICO));
        zoo.accogli(new Capibara("Cappy", RaritaEnum.EPICO, StatoAnimale.IN_PERICOLO));

        StatisticheZoo s = zoo.getStatistiche();
        assertEquals(3, s.totaleAnimali());
        assertEquals(5, s.capienzaTotale());
        assertEquals(1L, s.animaliPerSpecie().get("Leone"));
        assertEquals(1L, s.animaliPerRarita().get(RaritaEnum.EPICO));
        assertEquals(3 + 1 + 7, s.punteggio());
    }

    @Test
    void listeEsposteNonModificabili() {
        assertThrows(UnsupportedOperationException.class, () -> zoo.getRecinti().clear());
        assertThrows(UnsupportedOperationException.class, () -> savana.getAnimali().clear());
    }

    @Test
    void rilasciaLiberaIlPosto() {
        Animale leone = new Leone("Simba", RaritaEnum.RARO, StatoAnimale.SELVATICO);
        zoo.accogli(leone);

        assertTrue(zoo.rilascia(leone));

        assertEquals(3, savana.getPostiLiberi());
        assertFalse(zoo.rilascia(leone));
    }

    @Test
    void laMedicinaCuraEdELimitataAllaVitaMassima() {
        Animale leone = new Leone("Simba", RaritaEnum.RARO, StatoAnimale.SELVATICO);
        zoo.accogli(leone);
        leone.impostaVita(20);

        RisultatoCura risultato = zoo.cura(leone, new Medicina("Antibiotico", 15));
        assertEquals(EsitoCura.CURATO, risultato.esito());
        assertEquals(15, risultato.puntiRecuperati());
        assertEquals(35, leone.getVita());

        leone.impostaVita(95);
        assertEquals(5, zoo.cura(leone, new Medicina("Antibiotico", 15)).puntiRecuperati());
        assertEquals(Animale.VITA_MASSIMA, leone.getVita());
    }

    @Test
    void ilCiboCuraSoloSopraLaSoglia() {
        Animale leone = new Leone("Simba", RaritaEnum.RARO, StatoAnimale.SELVATICO);
        zoo.accogli(leone);
        Cibo frutta = new Cibo("Frutta", 0.05, 3);

        leone.impostaVita(Zoo.VITA_MINIMA_PER_CIBO);
        assertEquals(EsitoCura.CIBO_NON_ADATTO, zoo.cura(leone, frutta).esito());
        assertEquals(Zoo.VITA_MINIMA_PER_CIBO, leone.getVita());

        leone.impostaVita(Zoo.VITA_MINIMA_PER_CIBO + 1);
        assertEquals(EsitoCura.CURATO, zoo.cura(leone, frutta).esito());
        assertEquals(Zoo.VITA_MINIMA_PER_CIBO + 4, leone.getVita());
    }

    @Test
    void unAnimaleSanoNonHaBisognoDiCure() {
        Animale leone = new Leone("Simba", RaritaEnum.RARO, StatoAnimale.SELVATICO);
        zoo.accogli(leone);

        assertEquals(EsitoCura.GIA_IN_SALUTE, zoo.cura(leone, new Medicina("Siero", 60)).esito());
    }

    @Test
    void unOggettoNonCurativoNonCura() {
        Animale leone = new Leone("Simba", RaritaEnum.RARO, StatoAnimale.IN_PERICOLO);
        zoo.accogli(leone);

        assertEquals(EsitoCura.NON_CURATIVO,
                zoo.cura(leone, new StrumentoCattura("Rete", 0.1)).esito());
        assertEquals(EsitoCura.NON_CURATIVO, zoo.cura(leone, new Cibo("Sasso", 0.0)).esito());
    }

    @Test
    void curaUnAnimaleEsternoLanciaEccezione() {
        Animale estraneo = new Leone("X", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO);
        assertThrows(IllegalArgumentException.class,
                () -> zoo.cura(estraneo, new Medicina("Siero", 60)));
    }

    @Test
    void ampliareUnRecintoAumentaLaCapienza() {
        savana.amplia(2);
        assertEquals(5, savana.getCapienza());
        assertThrows(IllegalArgumentException.class, () -> savana.amplia(0));
    }
}