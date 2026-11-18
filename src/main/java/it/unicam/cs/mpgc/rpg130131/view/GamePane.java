package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.EsitoTentativo;
import it.unicam.cs.mpgc.rpg130131.controller.GameController;
import it.unicam.cs.mpgc.rpg130131.controller.GameFactory;
import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.map.Direzione;
import it.unicam.cs.mpgc.rpg130131.model.shop.EsitoAcquisto;
import it.unicam.cs.mpgc.rpg130131.model.shop.Negozio;
import it.unicam.cs.mpgc.rpg130131.model.shop.VoceNegozio;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.RisultatoCura;
import it.unicam.cs.mpgc.rpg130131.persistence.PersistenzaException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Schermata di gioco: dispone i pannelli, gestisce menu e tasti e li collega al {@link GameController}.
 * Non contiene regole di gioco: ogni azione viene delegata al controller e poi si ridisegna tutto.
 */
public class GamePane extends BorderPane {

    private final GameFactory factory;
    private final MappaView mappaView = new MappaView(new ImmaginiAnimali());
    private final PannelloCattura pannelloCattura = new PannelloCattura();
    private final InventarioView inventarioView = new InventarioView();
    private final ZooView zooView = new ZooView();
    private final NegozioView negozioView = new NegozioView();
    private final LogView logView = new LogView();
    private final List<VistaAggiornabile> viste =
            List.of(mappaView, pannelloCattura, inventarioView, zooView, negozioView);

    private GameController controller;
    private Stage finestraNegozio;

    public GamePane(GameFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory");

        StackPane centro = new StackPane(mappaView);
        centro.setPadding(new Insets(10));

        Button bottoneNegozio = new Button("Negozio (Ctrl+B)");
        bottoneNegozio.setMaxWidth(Double.MAX_VALUE);
        bottoneNegozio.setOnAction(e -> apriNegozio());

        VBox laterale = new VBox(10, bottoneNegozio, pannelloCattura, inventarioView, zooView);
        laterale.setPadding(new Insets(10));
        laterale.setPrefWidth(330);

        setTop(creaMenu());
        setCenter(centro);
        setRight(laterale);
        setBottom(logView);

        pannelloCattura.setOnCattura(this::tentaCattura);
        zooView.setOnRilascia(this::rilascia);
        zooView.setOnCura(this::curaAnimale);
        negozioView.setOnCompra(this::compra);
        negozioView.setOnAmplia(this::amplia);
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

    // ---------- azioni di gioco ----------

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

    private void rilascia(Animale animale) {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION,
                "Rilasciare %s?\nRiceverai %d monete.".formatted(
                        Messaggi.animale(animale), animale.getRarita().getMoneteRilascio()),
                ButtonType.OK, ButtonType.CANCEL);
        conferma.setHeaderText("Rilascia animale");
        if (conferma.showAndWait().filter(bottone -> bottone == ButtonType.OK).isEmpty()) {
            return;
        }
        int monete = controller.rilascia(animale);
        logView.aggiungi(Messaggi.rilascio(animale, monete));
        aggiornaTutto();
    }

    private void curaAnimale(Animale animale) {
        Inventario inventario = controller.getGiocatore().getInventario();
        Map<String, Oggetto> scelte = new LinkedHashMap<>();
        inventario.getContenutoDi(Medicina.class)
                .forEach((medicina, quantita) -> scelte.put(Messaggi.oggetto(medicina, quantita), medicina));
        inventario.getContenutoDi(Cibo.class)
                .forEach((cibo, quantita) -> scelte.put(Messaggi.oggetto(cibo, quantita), cibo));
        if (scelte.isEmpty()) {
            logView.aggiungi("Non hai medicine né cibo: compra qualcosa nel Negozio.");
            return;
        }

        ChoiceDialog<String> dialogo = new ChoiceDialog<>(scelte.keySet().iterator().next(), scelte.keySet());
        dialogo.setTitle("Cura");
        dialogo.setHeaderText("Cura " + Messaggi.animale(animale));
        dialogo.setContentText("Usa:");
        dialogo.showAndWait().map(scelte::get).ifPresent(curativo -> {
            RisultatoCura risultato = controller.cura(animale, curativo);
            logView.aggiungi(Messaggi.cura(animale, curativo, risultato));
            aggiornaTutto();
        });
    }

    // ---------- negozio ----------

    private void apriNegozio() {
        if (controller == null) {
            return;
        }
        if (finestraNegozio == null) {
            finestraNegozio = new Stage();
            finestraNegozio.setTitle("Negozio");
            finestraNegozio.initOwner(getScene().getWindow());
            finestraNegozio.initModality(Modality.WINDOW_MODAL);
            finestraNegozio.setScene(new Scene(negozioView, 440, 560));
        }
        negozioView.aggiorna(controller);
        negozioView.mostraMessaggio("");
        finestraNegozio.show();
    }

    private void compra(VoceNegozio articolo) {
        EsitoAcquisto esito = controller.compra(articolo);
        String testo = Messaggi.acquisto(esito, Messaggi.descrizione(articolo.oggetto()), articolo.prezzo());
        logView.aggiungi(testo);
        negozioView.mostraMessaggio(testo);
        aggiornaTutto();
    }

    private void amplia(Recinto recinto) {
        int prezzo = controller.getNegozio().prezzoAmpliamento(recinto);
        EsitoAcquisto esito = controller.ampliaRecinto(recinto);
        String cosa = "+%d posti in %s".formatted(Negozio.POSTI_PER_AMPLIAMENTO, recinto.getNome());
        String testo = Messaggi.acquisto(esito, cosa, prezzo);
        logView.aggiungi(testo);
        negozioView.mostraMessaggio(testo);
        aggiornaTutto();
    }

    // ---------- salvataggio e menu ----------

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

        MenuItem negozio = new MenuItem("Negozio");
        negozio.setAccelerator(KeyCombination.keyCombination("Shortcut+B"));
        negozio.setOnAction(e -> apriNegozio());

        MenuItem salva = new MenuItem("Salva");
        salva.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        salva.setOnAction(e -> salva());

        MenuItem carica = new MenuItem("Carica");
        carica.setAccelerator(KeyCombination.keyCombination("Shortcut+L"));
        carica.setOnAction(e -> carica());

        MenuItem esci = new MenuItem("Esci");
        esci.setOnAction(e -> Platform.exit());

        return new MenuBar(new Menu("Partita", null,
                nuova, negozio, new SeparatorMenuItem(), salva, carica, new SeparatorMenuItem(), esci));
    }

    private static void mostraErrore(String titolo, Exception errore) {
        Alert avviso = new Alert(Alert.AlertType.ERROR);
        avviso.setTitle("Errore");
        avviso.setHeaderText(titolo);
        avviso.setContentText(Objects.requireNonNullElse(errore.getMessage(), errore.toString()));
        avviso.showAndWait();
    }
}