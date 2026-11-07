package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo;
import it.unicam.cs.mpgc.rpg130131.controller.GameController;
import it.unicam.cs.mpgc.rpg130131.controller.GameFactory;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.Direzione;
import it.unicam.cs.mpgc.rpg130131.persistence.PersistenzaException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Schermata di gioco: dispone i pannelli, gestisce menu e tasti e li collega al {@link GameController}.
 * Non contiene regole di gioco: ogni azione viene delegata al controller e poi si ridisegna tutto.
 */
public class GamePane extends BorderPane {

    private final GameFactory factory;
    private final MappaView mappaView = new MappaView();
    private final PannelloCattura pannelloCattura = new PannelloCattura();
    private final InventarioView inventarioView = new InventarioView();
    private final ZooView zooView = new ZooView();
    private final LogView logView = new LogView();
    private final List<VistaAggiornabile> viste =
            List.of(mappaView, pannelloCattura, inventarioView, zooView);

    private GameController controller;

    public GamePane(GameFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory");

        StackPane centro = new StackPane(mappaView);
        centro.setPadding(new Insets(10));

        VBox laterale = new VBox(10, pannelloCattura, inventarioView, zooView);
        laterale.setPadding(new Insets(10));
        laterale.setPrefWidth(310);

        setTop(creaMenu());
        setCenter(centro);
        setRight(laterale);
        setBottom(logView);

        pannelloCattura.setOnCattura(this::tentaCattura);
    }

    public boolean haPartita() {
        return controller != null;
    }

    /** Crea e avvia una nuova partita; in caso di errore mostra un avviso e lascia tutto com'era. */
    public void nuovaPartita(String nomeGiocatore) {
        try {
            controller = factory.nuovaPartita(nomeGiocatore);
        } catch (PersistenzaException | IllegalArgumentException e) {
            mostraErrore("Impossibile creare la partita", e);
            return;
        }
        logView.svuota();
        logView.aggiungi("Benvenuto, %s! Muoviti con le frecce o WASD e cerca gli animali.".formatted(nomeGiocatore));
        aggiornaTutto();
    }

    /** Da collegare come filtro degli eventi della scena: gestisce movimento e cattura da tastiera. */
    public void gestisciTasto(KeyEvent evento) {
        if (controller == null) {
            return;
        }
        Optional<Direzione> direzione = direzioneDa(evento.getCode());
        if (direzione.isPresent()) {
            evento.consume();
            if (controller.muovi(direzione.get())) {
                controller.animaleQui().ifPresent(
                        animale -> logView.aggiungi("Hai trovato: " + Messaggi.animale(animale)));
                aggiornaTutto();
            }
        } else if (evento.getCode() == KeyCode.SPACE) {
            evento.consume();
            pannelloCattura.cattura();
        }
    }

    private static Optional<Direzione> direzioneDa(KeyCode tasto) {
        return switch (tasto) {
            case UP, W -> Optional.of(Direzione.NORD);
            case DOWN, S -> Optional.of(Direzione.SUD);
            case RIGHT, D -> Optional.of(Direzione.EST);
            case LEFT, A -> Optional.of(Direzione.OVEST);
            default -> Optional.empty();
        };
    }

    private void tentaCattura() {
        if (controller == null) {
            return;
        }
        Optional<StrumentoCattura> strumento = pannelloCattura.getStrumentoScelto();
        if (strumento.isEmpty()) {
            return;
        }
        EsitoTentativo esito = controller.tentaCattura(strumento.get(), pannelloCattura.getEscaScelta());
        logView.aggiungi(Messaggi.esito(esito));
        aggiornaTutto();
    }

    private void salva() {
        if (controller == null) {
            return;
        }
        try {
            controller.salva();
            logView.aggiungi("Partita salvata.");
        } catch (PersistenzaException e) {
            mostraErrore("Salvataggio non riuscito", e);
        }
    }

    private void carica() {
        if (controller == null) {
            return;
        }
        try {
            if (controller.carica()) {
                logView.aggiungi("Partita caricata.");
                aggiornaTutto();
            } else {
                logView.aggiungi("Nessun salvataggio trovato.");
            }
        } catch (PersistenzaException e) {
            mostraErrore("Caricamento non riuscito", e);
        }
    }

    private void chiediNomeENuovaPartita() {
        TextInputDialog dialogo = new TextInputDialog("Giocatore");
        dialogo.setTitle("Nuova partita");
        dialogo.setHeaderText("Come ti chiami?");
        dialogo.setContentText("Nome:");
        dialogo.showAndWait()
                .map(String::strip)
                .filter(nome -> !nome.isEmpty())
                .ifPresent(this::nuovaPartita);
    }

    private void aggiornaTutto() {
        viste.forEach(vista -> vista.aggiorna(controller));
    }

    private MenuBar creaMenu() {
        MenuItem nuova = new MenuItem("Nuova partita");
        nuova.setAccelerator(KeyCombination.keyCombination("Shortcut+N"));
        nuova.setOnAction(e -> chiediNomeENuovaPartita());

        MenuItem salva = new MenuItem("Salva");
        salva.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        salva.setOnAction(e -> salva());

        MenuItem carica = new MenuItem("Carica");
        carica.setAccelerator(KeyCombination.keyCombination("Shortcut+L"));
        carica.setOnAction(e -> carica());

        MenuItem esci = new MenuItem("Esci");
        esci.setOnAction(e -> Platform.exit());

        return new MenuBar(new Menu("Partita", null,
                nuova, new SeparatorMenuItem(), salva, carica, new SeparatorMenuItem(), esci));
    }

    private static void mostraErrore(String titolo, Exception errore) {
        Alert avviso = new Alert(Alert.AlertType.ERROR);
        avviso.setTitle("Errore");
        avviso.setHeaderText(titolo);
        avviso.setContentText(Objects.requireNonNullElse(errore.getMessage(), errore.toString()));
        avviso.showAndWait();
    }
}