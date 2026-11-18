package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class XMLCatalogReaderTest {

    @Test
    void leggeIlCatalogoPredefinito() throws Exception {
        Catalogo catalogo = XMLCatalogReader.predefinito().leggi();

        assertFalse(catalogo.animali().isEmpty());
        assertTrue(catalogo.strumenti().contains(new StrumentoCattura("Rete", 0.10)));
        assertTrue(catalogo.cibi().stream().anyMatch(c -> c.nome().equals("Carne")));
    }

    @Test
    void tutteLeSpecieDelCatalogoSonoNelRegistro() throws Exception {
        Catalogo catalogo = XMLCatalogReader.predefinito().leggi();
        RegistroSpecie registro = RegistroSpecie.predefinito();

        assertTrue(catalogo.animali().stream().allMatch(v -> registro.contiene(v.specie())));
    }

    @Test
    void rifiutaUnCatalogoNonValido(@TempDir Path cartella) throws Exception {
        Path file = cartella.resolve("rotto.xml");
        Files.writeString(file, "<catalogo><animali/></catalogo>");
        XMLCatalogReader lettore = new XMLCatalogReader(
                file.toUri().toURL(),
                XMLCatalogReader.class.getResource("/catalog/animali.xsd"));

        assertThrows(PersistenzaException.class, lettore::leggi);
    }

    @Test
    void ognBiomaHaAlmenoUnaSpecie() throws Exception {
        Catalogo catalogo = XMLCatalogReader.predefinito().leggi();
        RegistroSpecie registro = RegistroSpecie.predefinito();

        for (BiomaEnum bioma : BiomaEnum.values()) {
            boolean coperto = catalogo.animali().stream().anyMatch(voce ->
                    registro.crea(voce.specie(), "x", voce.rarita(), StatoAnimale.SELVATICO)
                            .getBiomaNativo() == bioma);
            assertTrue(coperto, "Nessun animale per il bioma " + bioma);
        }
    }

    @Test
    void leMedicineSonoNelCatalogo() throws Exception {
        Catalogo catalogo = XMLCatalogReader.predefinito().leggi();

        assertTrue(catalogo.medicine().contains(new Medicina("Antidolorifico", 5)));
        assertTrue(catalogo.medicine().contains(new Medicina("Antibiotico", 15)));
    }

    @Test
    void ilKitInizialeHaSoloLeEscheBase() throws Exception {
        Catalogo catalogo = XMLCatalogReader.predefinito().leggi();

        assertTrue(catalogo.kit().stream().anyMatch(s ->
                s.oggetto() instanceof Cibo cibo && cibo.nome().equals("Frutta") && s.quantita() == 3));
        assertTrue(catalogo.kit().stream().noneMatch(s -> s.oggetto() instanceof Medicina));
    }

    @Test
    void ilNegozioVendeSoloOggettiDefinitiNelCatalogo() throws Exception {
        Catalogo catalogo = XMLCatalogReader.predefinito().leggi();

        assertFalse(catalogo.negozio().isEmpty());
        catalogo.negozio().forEach(voce -> {
            boolean definito = catalogo.strumenti().contains(voce.oggetto())
                    || catalogo.cibi().contains(voce.oggetto())
                    || catalogo.medicine().contains(voce.oggetto());
            assertTrue(definito, "Non definito: " + voce.oggetto());
        });
    }

    @Test
    void unRiferimentoAdUnOggettoSconosciutoERifiutato(@TempDir Path cartella) throws Exception {
        Path file = cartella.resolve("catalogo.xml");
        Files.writeString(file, """
                <catalogo>
                  <animali>
                    <animale specie="Leone" rarita="RARO" peso="1"><nomi><nome>Leo</nome></nomi></animale>
                  </animali>
                  <strumenti><strumento nome="Rete" bonus="0.1"/></strumenti>
                  <cibi><cibo nome="Frutta" bonus="0.05" cura="3"/></cibi>
                  <medicine/>
                  <negozio><articolo tipo="MEDICINA" nome="Fantasma" prezzo="10"/></negozio>
                  <kit/>
                </catalogo>
                """);
        XMLCatalogReader lettore = new XMLCatalogReader(
                file.toUri().toURL(),
                XMLCatalogReader.class.getResource("/catalog/animali.xsd"));

        assertThrows(PersistenzaException.class, lettore::leggi);
    }
}