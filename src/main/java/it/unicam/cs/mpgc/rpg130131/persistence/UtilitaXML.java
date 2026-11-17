package it.unicam.cs.mpgc.rpg130131.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utility contenente operazioni XML comuni alla gestione della persistenza nel gioco.
 *
 * Fornisce metodi statici per:
 * <ul>
 *   <li>Creare e validare documenti XML rispetto a uno schema XSD</li>
 *   <li>Scrivere documenti XML su file con salvataggio atomico (tramite file temporaneo)</li>
 *   <li>Estrarre elementi figli da un documento XML in base al nome dell'etichetta</li>
 * </ul>
 *
 * <p>Questa classe è utilizzata internamente da {@link XMLCatalogReader} e {@link XMLSaveRepository}.</p>
 *
 * <h3>Sicurezza del parsing XML</h3>
 * Tutti i metodi di parsing disabilitano il processing sicuro:
 * <ul>
 *   <li>Disabilitano la processing delle doctype (previene XXE attacks)</li>
 *   <li>Attivano {@code FEATURE_SECURE_PROCESSING} per limitare l'elaborazione di entità esterne</li>
 * </ul>
 */
final class UtilitaXML {

    /**
     * ErrorHandler utilizzato durante il parsing XML per garantire la validità del documento.
     *
     * Fa fallire immediatamente il parsing quando si verificano errori o fatalità, mentre gli avvisi
     * non interrompono il processo di parsing (consentono al documento di essere letto anche con alcuni warning).
     */
    private static final ErrorHandler RIGOROSO = new ErrorHandler() {
        @Override
        public void warning(SAXParseException e) {
            // gli avvisi non invalidano il documento
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    };

    private UtilitaXML() {
    }

    /**
     * Legge un documento XML da uno stream e lo valida rispetto allo schema XSD specificato.
     *
     * @param flusso lo stream contenente il documento XML da leggere
     * @param schemaUrl URL dello schema XSD da utilizzare per la validazione
     * @return il documento XML parseato e validato
     * @throws PersistenzaException se il file non può essere letto o non è valido secondo lo schema
     */
    static Document leggi(InputStream flusso, URL schemaUrl) throws PersistenzaException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Schema schema = schemaFactory.newSchema(schemaUrl);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setIgnoringComments(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(RIGOROSO);
            return builder.parse(flusso);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new PersistenzaException("Documento XML non valido: " + e.getMessage(), e);
        }
    }

    /**
     * Crea un nuovo documento XML vuoto pronto per essere popolato con elementi e attributi.
     *
     * @return un nuovo documento XML vuoto creato dal parser
     * @throws PersistenzaException se il parser non può inizializzare il documento
     */
    static Document nuovoDocumento() throws PersistenzaException {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new PersistenzaException("Impossibile creare il documento XML", e);
        }
    }

    /**
     * Scrive un documento XML su un file, utilizzando una strategia di scrittura sicura
     * che prima scrive in un file temporaneo (.tmp) e poi sposta il file finale per
     * evitare la corruzione dei dati in caso di interruzioni.
     *
     * <p>Il file risultante è formattato con indentazione a 2 spazi.</p>
     *
     * @param documento il documento XML da scrivere
     * @param file il percorso del file dove salvare il documento
     * @throws PersistenzaException se la scrittura fallisce (errori I/O, directory non scrivibile, ecc.)
     */
    static void scrivi(Document documento, Path file) throws PersistenzaException {
        Path temporaneo = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());

            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            try (OutputStream out = Files.newOutputStream(temporaneo)) {
                transformer.transform(new DOMSource(documento), new StreamResult(out));
            }
            Files.move(temporaneo, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (TransformerException | IOException e) {
            throw new PersistenzaException("Impossibile scrivere " + file, e);
        }
    }

    /**
     * Esegue una ricerca di tutti gli elementi figli del nodo specificato che hanno
     * il nome dell'etichetta desiderato.
     *
     * <p>Il metodo controlla ogni nodo figlio diretto (non discendenti) e restituisce
     * solo quelli che sono Element con un tagName corrispondente.</p>
     *
     * @param genitore il nodo padre da cui cercare i figli
     * @param nome il nome dell'etichetta che gli elementi restituiti devono avere
     * @return una lista contenente tutti gli elementi figlio con il nome specificato,
     *         nell'ordine in cui appaiono nel documento XML (potrebbe restituire una lista vuota)
     */
    static List<Element> figli(Element genitore, String nome) {
        List<Element> risultato = new ArrayList<>();
        for (Node nodo = genitore.getFirstChild(); nodo != null; nodo = nodo.getNextSibling()) {
            if (nodo instanceof Element elemento && nome.equals(elemento.getTagName())) {
                risultato.add(elemento);
            }
        }
        return risultato;
    }

    /**
     * Restituisce il primo elemento figlio del nodo specificato che ha il nome dell'etichetta desiderato.
     *
     * <p>Il metodo assume che lo schema XSD garantisca la presenza di questo elemento, quindi
     * restituirà null o sollevare un'eccezione se non viene trovato alcun elemento corrispondente.</p>
     *
     * @param genitore il nodo padre da cui cercare il figlio
     * @param nome il nome dell'etichetta che l'elemento restituito deve avere
     * @return il primo elemento figlio con il nome specificato
     */
    static Element figlio(Element genitore, String nome) {
        return figli(genitore, nome).getFirst();
    }
}