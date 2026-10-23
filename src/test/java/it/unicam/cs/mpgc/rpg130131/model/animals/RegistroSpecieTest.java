package it.unicam.cs.mpgc.rpg130131.model.animals;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RegistroSpecieTest {

    private final RegistroSpecie registro = RegistroSpecie.predefinito();

    @Test
    void predefinitoConosceLeSpecieBase() {
        assertEquals(Set.of("Capibara", "Giraffa", "Leone"), registro.specie());
    }

    @Test
    void creaUnEsemplareDellaSpecieRichiesta() {
        Animale animale = registro.crea("Leone", "Leo", RaritaEnum.RARO, StatoAnimale.SELVATICO);
        assertInstanceOf(Leone.class, animale);
        assertEquals("Leo", animale.getNome());
    }

    @Test
    void ricostruisciMantieneLId() {
        UUID id = UUID.randomUUID();
        Animale animale = registro.ricostruisci("Giraffa", id, "Gigi", RaritaEnum.COMUNE, StatoAnimale.IN_PERICOLO);
        assertEquals(id, animale.getId());
    }

    @Test
    void specieSconosciutaRifiutata() {
        assertThrows(IllegalArgumentException.class,
                () -> registro.crea("Drago", "Smaug", RaritaEnum.LEGGENDARIO, StatoAnimale.SELVATICO));
    }

    @Test
    void registrazioneDuplicataRifiutata() {
        assertThrows(IllegalArgumentException.class, () -> registro.registra("Leone", Leone::new));
    }

    @Test
    void nomeSpecieIncoerenteRilevato() {
        RegistroSpecie sbagliato = new RegistroSpecie().registra("Tigre", Leone::new);
        assertThrows(IllegalStateException.class,
                () -> sbagliato.crea("Tigre", "Rajah", RaritaEnum.COMUNE, StatoAnimale.SELVATICO));
    }
}