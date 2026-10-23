package it.unicam.cs.mpgc.rpg130131.persistence;

import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo;
import it.unicam.cs.mpgc.rpg130131.model.catalog.VoceAnimale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static it.unicam.cs.mpgc.rpg130131.persistence.UtilitaXML.figli;
import static it.unicam.cs.mpgc.rpg130131.persistence.UtilitaXML.figlio;

/** Legge il catalogo da un documento XML validato con il suo schema XSD. */
public class XMLCatalogReader implements CatalogReader {

    private static final String RISORSA_XML = "/catalog/animali.xml";
    private static final String RISORSA_XSD = "/catalog/animali.xsd";

    private final URL documento;
    private final URL schema;

    public XMLCatalogReader(URL documento, URL schema) {
        this.documento = Objects.requireNonNull(documento, "documento");
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    /** @return un lettore del catalogo incluso nelle risorse dell'applicazione */
    public static XMLCatalogReader predefinito() {
        URL xml = XMLCatalogReader.class.getResource(RISORSA_XML);
        URL xsd = XMLCatalogReader.class.getResource(RISORSA_XSD);
        if (xml == null || xsd == null) {
            throw new IllegalStateException("Risorse del catalogo mancanti");
        }
        return new XMLCatalogReader(xml, xsd);
    }

    @Override
    public Catalogo leggi() throws PersistenzaException {
        try (InputStream flusso = documento.openStream()) {
            Element radice = UtilitaXML.leggi(flusso, schema).getDocumentElement();
            return new Catalogo(animali(radice), strumenti(radice), cibi(radice));
        } catch (IOException e) {
            throw new PersistenzaException("Impossibile leggere " + documento, e);
        } catch (IllegalArgumentException e) {
            throw new PersistenzaException("Catalogo incoerente: " + e.getMessage(), e);
        }
    }

    private static List<VoceAnimale> animali(Element radice) {
        return figli(figlio(radice, "animali"), "animale").stream()
                .map(e -> new VoceAnimale(
                        e.getAttribute("specie"),
                        RaritaEnum.valueOf(e.getAttribute("rarita")),
                        Integer.parseInt(e.getAttribute("peso")),
                        figli(figlio(e, "nomi"), "nome").stream()
                                .map(n -> n.getTextContent().trim())
                                .toList()))
                .toList();
    }

    private static List<StrumentoCattura> strumenti(Element radice) {
        return figli(figlio(radice, "strumenti"), "strumento").stream()
                .map(e -> new StrumentoCattura(
                        e.getAttribute("nome"),
                        Double.parseDouble(e.getAttribute("bonus"))))
                .toList();
    }

    private static List<Cibo> cibi(Element radice) {
        return figli(figlio(radice, "cibi"), "cibo").stream()
                .map(e -> new Cibo(
                        e.getAttribute("nome"),
                        Double.parseDouble(e.getAttribute("bonus"))))
                .toList();
    }
}