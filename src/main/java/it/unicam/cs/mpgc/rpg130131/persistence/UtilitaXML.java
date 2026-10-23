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

/** Operazioni XML comuni a lettore del catalogo e repository dello zoo. */
final class UtilitaXML {

    /** Fa fallire il parsing al primo errore di validazione. */
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

    /** Legge e valida un documento rispetto allo schema XSD indicato. */
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

    static Document nuovoDocumento() throws PersistenzaException {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new PersistenzaException("Impossibile creare il documento XML", e);
        }
    }

    /** Scrive il documento su file in modo sicuro: prima un file temporaneo, poi lo sostituisce. */
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

    /** @return i soli figli diretti con il nome indicato, nell'ordine del documento */
    static List<Element> figli(Element genitore, String nome) {
        List<Element> risultato = new ArrayList<>();
        for (Node nodo = genitore.getFirstChild(); nodo != null; nodo = nodo.getNextSibling()) {
            if (nodo instanceof Element elemento && nome.equals(elemento.getTagName())) {
                risultato.add(elemento);
            }
        }
        return risultato;
    }

    /** @return il primo figlio con il nome indicato (lo schema ne garantisce la presenza) */
    static Element figlio(Element genitore, String nome) {
        return figli(genitore, nome).getFirst();
    }
}