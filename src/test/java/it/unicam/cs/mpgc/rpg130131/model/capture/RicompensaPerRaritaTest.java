package it.unicam.cs.mpgc.rpg130131.model.capture;

import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.Leone;
import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RicompensaPerRaritaTest {

    private final StrumentoCattura rete = new StrumentoCattura("Rete", 0.1);
    private final StrumentoCattura trappola = new StrumentoCattura("Trappola", 0.2);
    private final StrumentoCattura tranquillante = new StrumentoCattura("Tranquillante", 0.3);
    private final Cibo frutta = new Cibo("Frutta", 0.05);
    private final Cibo carne = new Cibo("Carne", 0.10);

    // volutamente in ordine sparso: la classe deve ordinarli per bonus
    private final RicompensaPerRarita ricompensa = new RicompensaPerRarita(new Catalogo(
            List.of(), List.of(trappola, tranquillante, rete), List.of(carne, frutta)));

    private static Animale animale(RaritaEnum rarita) {
        return new Leone("Leo", rarita, StatoAnimale.SELVATICO);
    }

    @Test
    void animaleComuneDaGliOggettiPiuDeboli() {
        Map<Oggetto, Integer> premio = ricompensa.perCattura(animale(RaritaEnum.COMUNE));

        assertEquals(2, premio.size());
        assertEquals(RicompensaPerRarita.STRUMENTI_PER_CATTURA, premio.get(rete));
        assertEquals(RicompensaPerRarita.CIBI_PER_CATTURA, premio.get(frutta));
    }

    @Test
    void piuRaroPiuPotenti() {
        Map<Oggetto, Integer> premio = ricompensa.perCattura(animale(RaritaEnum.RARO));

        assertTrue(premio.containsKey(trappola));
        assertTrue(premio.containsKey(carne));
    }

    @Test
    void oltreLaFineDellaListaSiUsaIlMigliore() {
        Map<Oggetto, Integer> premio = ricompensa.perCattura(animale(RaritaEnum.LEGGENDARIO));

        assertTrue(premio.containsKey(tranquillante));
        assertTrue(premio.containsKey(carne));
    }

    @Test
    void catalogoSenzaOggettiNonDaNulla() {
        RicompensaPerRarita vuota = new RicompensaPerRarita(new Catalogo(List.of(), List.of(), List.of()));
        assertTrue(vuota.perCattura(animale(RaritaEnum.EPICO)).isEmpty());
    }

    @Test
    void premioNonModificabile() {
        Map<Oggetto, Integer> premio = ricompensa.perCattura(animale(RaritaEnum.COMUNE));
        assertThrows(UnsupportedOperationException.class, premio::clear);
    }
}