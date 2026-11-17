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

/**
 * Implementazione concreta di {@link CatalogReader} che legge il catalogo da un file XML.
 *
 * <p>Questa classe carica il catalogo dal file resources {@code /catalog/animali.xml} e lo valida
 * rispetto allo schema XSD definito in {@code /catalog/animali.xsd}. Il catalogo contiene:</p>
 * <ul>
 *   <li>Animali: specie, rarità, peso e lista dei nomi alternativi</li>
 *   <li>Strumenti di cattura: nome e bonus per la cattura</li>
 *   <li>Cibi: nome e bonus come esca</li>
 * </ul>
 *
 * <p>Il metodo {@link #predefinito()} restituisce un'istanza pronta all'uso che punta alle risorse
 * dell'applicazione.</p>
 *
 * @see CatalogReader
 * @see it.unicam.cs.mpgc.rpg130131.model.catalog.Catalogo
 */
public class XMLCatalogReader implements CatalogReader {

    /** Percorso delle risorse XML e XSD del catalogo all'interno della classe. */
    private static final String RISORSA_XML = "/catalog/animali.xml";
    /** Percorso dello schema XSD per la validazione del catalogo. */
    private static final String RISORSA_XSD = "/catalog/animali.xsd";

    /** URL al file XML contenente il catalogo. */
    private final URL documento;
    /** URL allo schema XSD per la validazione del catalogo. */
    private final URL schema;

    /**
     * Costruttore che inietta le risorse XML e XSD necessarie per leggere il catalogo.
     *
     * @param documento URL al file XML contenente i dati del catalogo
     * @param schema URL allo schema XSD per validare il documento XML
     * @throws IllegalArgumentException se uno dei parametri è null
     */
    public XMLCatalogReader(URL documento, URL schema) {
        this.documento = Objects.requireNonNull(documento, "documento");
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    /**
     * Restituisce un'istanza preconfigurata di {@link XMLCatalogReader} che punta alle risorse
     * incorporate dell'applicazione (/catalog/animali.xml e /catalog/animali.xsd).
     *
     * @return un lettore del catalogo pronto all'uso, configurato per leggere dalle risorse dell'applicazione
     * @throws IllegalStateException se le risorse del catalogo non sono disponibili nel classpath
     */
    public static XMLCatalogReader predefinito() {
        URL xml = XMLCatalogReader.class.getResource(RISORSA_XML);
        URL xsd = XMLCatalogReader.class.getResource(RISORSA_XSD);
        if (xml == null || xsd == null) {
            throw new IllegalStateException("Risorse del catalogo mancanti");
        }
        return new XMLCatalogReader(xml, xsd);
    }

    @Override
    /**
     * Legge il file XML del catalogo e restituisce un oggetto {@link Catalogo} popolato.
     *
     * <p>Il metodo apre lo stream alle risorse dell'applicazione, valida il documento contro
     * lo schema XSD e costruisce l'oggetto catalogo contenente tutte le informazioni sugli animali,
     * gli strumenti di cattura e i cibi.</p>
     *
     * @return un oggetto Catalogo contenente:
     *   <ul>
     *     <li>La lista degli animali con specie, rarità, peso e nomi</li>
     *     <li>La lista degli strumenti di cattura con nome e bonus</li>
     *     <li>La lista dei cibi con nome e bonus esca</li>
     *   </ul>
     * @throws PersistenzaException se il file non può essere aperto o contiene dati non validi secondo lo schema
     */
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

    /**
     * Estrarre gli animali dal documento XML radice.
     *
     * @param radice elemento root del documento XML
     * @return lista degli oggetti VoceAnimale estratti dagli elementi XML
     */
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

    /**
     * Estrarre gli strumenti di cattura dal documento XML radice.
     *
     * @param radice elemento root del documento XML
     * @return lista degli oggetti StrumentoCattura estratti dagli elementi XML
     */
    private static List<StrumentoCattura> strumenti(Element radice) {
        return figli(figlio(radice, "strumenti"), "strumento").stream()
                .map(e -> new StrumentoCattura(
                        e.getAttribute("nome"),
                        Double.parseDouble(e.getAttribute("bonus"))))
                .toList();
    }

    /**
     * Estrarre i cibi dal documento XML radice.
     *
     * @param radice elemento root del documento XML
     * @return lista degli oggetti Cibo estratti dagli elementi XML
     */
    private static List<Cibo> cibi(Element radice) {
        return figli(figlio(radice, "cibi"), "cibo").stream()
                .map(e -> new Cibo(
                        e.getAttribute("nome"),
                        Double.parseDouble(e.getAttribute("bonus"))))
                .toList();
    }
}