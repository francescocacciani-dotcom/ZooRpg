package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.GameController;
import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.animals.RaritaEnum;
import it.unicam.cs.mpgc.rpg130131.model.animals.StatoAnimale;
import it.unicam.cs.mpgc.rpg130131.model.map.BiomaEnum;
import it.unicam.cs.mpgc.rpg130131.model.map.Cella;
import it.unicam.cs.mpgc.rpg130131.model.map.Mappa2D;
import it.unicam.cs.mpgc.rpg130131.model.map.Posizione;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Disegna la mappa: colore per bioma, un cerchio per ogni animale (colore = rarità,
 * bordo rosso = in pericolo) e il giocatore evidenziato con un bordo e un segnalino.
 */
public class MappaView extends Canvas implements VistaAggiornabile {

    private static final double CELLA = 48;
    private static final double RAGGIO_ANIMALE = 16;
    private static final Color COLORE_GIOCATORE = Color.web("#1B1F8A");

    @Override
    public void aggiorna(GameController controller) {
        Mappa2D mappa = controller.getMappa();
        setWidth(mappa.getLarghezza() * CELLA);
        setHeight(mappa.getAltezza() * CELLA);

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        mappa.celle().forEach(cella -> disegnaCella(gc, cella));
        controller.getAvvistamenti().forEach((posizione, animale) -> disegnaAnimale(gc, posizione, animale));
        disegnaGiocatore(gc, controller.getGiocatore().getPosizione());
    }

    private static void disegnaCella(GraphicsContext gc, Cella cella) {
        double x = cella.posizione().x() * CELLA;
        double y = cella.posizione().y() * CELLA;
        gc.setFill(coloreBioma(cella.bioma()));
        gc.fillRect(x, y, CELLA, CELLA);
        gc.setStroke(Color.rgb(0, 0, 0, 0.15));
        gc.setLineWidth(1);
        gc.strokeRect(x, y, CELLA, CELLA);
    }

    private static void disegnaAnimale(GraphicsContext gc, Posizione posizione, Animale animale) {
        double cx = posizione.x() * CELLA + CELLA / 2;
        double cy = posizione.y() * CELLA + CELLA / 2;
        double diametro = RAGGIO_ANIMALE * 2;

        gc.setFill(coloreRarita(animale.getRarita()));
        gc.fillOval(cx - RAGGIO_ANIMALE, cy - RAGGIO_ANIMALE, diametro, diametro);

        boolean inPericolo = animale.getStato() == StatoAnimale.IN_PERICOLO;
        gc.setStroke(inPericolo ? Color.CRIMSON : Color.rgb(0, 0, 0, 0.6));
        gc.setLineWidth(inPericolo ? 3 : 1.5);
        gc.strokeOval(cx - RAGGIO_ANIMALE, cy - RAGGIO_ANIMALE, diametro, diametro);

        disegnaTesto(gc, animale.getSpecie().substring(0, 1), cx, cy, 16, Color.BLACK);
    }

    private static void disegnaGiocatore(GraphicsContext gc, Posizione posizione) {
        double x = posizione.x() * CELLA;
        double y = posizione.y() * CELLA;

        gc.setStroke(COLORE_GIOCATORE);
        gc.setLineWidth(3);
        gc.strokeRect(x + 1.5, y + 1.5, CELLA - 3, CELLA - 3);

        double cx = x + CELLA - 12;
        double cy = y + CELLA - 12;
        gc.setFill(COLORE_GIOCATORE);
        gc.fillOval(cx - 9, cy - 9, 18, 18);
        disegnaTesto(gc, "@", cx, cy, 12, Color.WHITE);
    }

    private static void disegnaTesto(GraphicsContext gc, String testo, double cx, double cy,
                                     double dimensione, Color colore) {
        gc.setFont(Font.font("System", FontWeight.BOLD, dimensione));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(colore);
        gc.fillText(testo, cx, cy);
    }

    private static Color coloreBioma(BiomaEnum bioma) {
        return switch (bioma) {
            case SAVANA -> Color.web("#C9B458");
            case FORESTA -> Color.web("#4E8F4E");
            case PALUDE -> Color.web("#5F9EA0");
            case DESERTO -> Color.web("#E8B87A");
        };
    }

    private static Color coloreRarita(RaritaEnum rarita) {
        return switch (rarita) {
            case COMUNE -> Color.web("#F2F2F2");
            case RARO -> Color.web("#8EC9FF");
            case EPICO -> Color.web("#C77DFF");
            case LEGGENDARIO -> Color.web("#FFD43B");
        };
    }
}