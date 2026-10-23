package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
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
}