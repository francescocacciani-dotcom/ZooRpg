package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.GameController;
import it.unicam.cs.mpgc.rpg130131.model.shop.Negozio;
import it.unicam.cs.mpgc.rpg130131.model.shop.VoceNegozio;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Pagina del negozio: articoli in vendita e ampliamento dei recinti.
 * Non contiene regole: mostra i dati e inoltra le scelte del giocatore.
 */
public class NegozioView extends VBox implements VistaAggiornabile {

    private final Label monete = new Label();
    private final ListView<VoceNegozio> articoli = new ListView<>();
    private final ListView<Recinto> recinti = new ListView<>();
    private final Button bottoneCompra = new Button("Compra");
    private final Button bottoneAmplia = new Button("Amplia recinto");
    private final Label messaggio = new Label();
    private Negozio negozio;
    private Consumer<VoceNegozio> alAcquisto = articolo -> { };
    private Consumer<Recinto> alAmpliamento = recinto -> { };

    public NegozioView() {
        super(8);
        setPadding(new Insets(12));
        messaggio.setWrapText(true);
        VBox.setVgrow(articoli, Priority.ALWAYS);
        recinti.setPrefHeight(130);

        articoli.setCellFactory(lista -> new ListCell<VoceNegozio>() {
            @Override
            protected void updateItem(VoceNegozio articolo, boolean vuoto) {
                super.updateItem(articolo, vuoto);
                setText(vuoto || articolo == null ? null : Messaggi.articolo(articolo));
            }
        });
        recinti.setCellFactory(lista -> new ListCell<Recinto>() {
            @Override
            protected void updateItem(Recinto recinto, boolean vuoto) {
                super.updateItem(recinto, vuoto);
                if (vuoto || recinto == null || negozio == null) {
                    setText(null);
                } else {
                    setText(Messaggi.ampliamento(recinto, Negozio.POSTI_PER_AMPLIAMENTO,
                            negozio.prezzoAmpliamento(recinto)));
                }
            }
        });

        bottoneCompra.disableProperty().bind(articoli.getSelectionModel().selectedItemProperty().isNull());
        bottoneAmplia.disableProperty().bind(recinti.getSelectionModel().selectedItemProperty().isNull());
        bottoneCompra.setOnAction(e -> {
            VoceNegozio scelto = articoli.getSelectionModel().getSelectedItem();
            if (scelto != null) {
                alAcquisto.accept(scelto);
            }
        });
        bottoneAmplia.setOnAction(e -> {
            Recinto scelto = recinti.getSelectionModel().getSelectedItem();
            if (scelto != null) {
                alAmpliamento.accept(scelto);
            }
        });

        getChildren().addAll(
                monete,
                new Label("Articoli"), articoli, bottoneCompra,
                new Label("Spazio"), recinti, bottoneAmplia,
                messaggio);
    }

    public void setOnCompra(Consumer<VoceNegozio> azione) {
        this.alAcquisto = azione;
    }

    public void setOnAmplia(Consumer<Recinto> azione) {
        this.alAmpliamento = azione;
    }

    /** Mostra in fondo alla pagina l'esito dell'ultima operazione. */
    public void mostraMessaggio(String testo) {
        messaggio.setText(testo);
    }

    @Override
    public void aggiorna(GameController controller) {
        negozio = controller.getNegozio();
        monete.setText(Messaggi.monete(controller.getGiocatore().getMonete()));
        sostituisci(articoli, negozio.getArticoli());
        sostituisci(recinti, controller.getZoo().getRecinti());
    }

    /** Aggiorna le voci mantenendo la selezione (per posizione) e ridisegna le celle. */
    private static <T> void sostituisci(ListView<T> lista, List<T> elementi) {
        int selezionato = lista.getSelectionModel().getSelectedIndex();
        lista.getItems().setAll(elementi);
        if (selezionato >= 0 && selezionato < elementi.size()) {
            lista.getSelectionModel().select(selezionato);
        }
        lista.refresh();
    }
}