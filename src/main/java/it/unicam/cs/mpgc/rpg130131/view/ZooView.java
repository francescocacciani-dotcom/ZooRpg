package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.GameController;
import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.function.Consumer;

/** Statistiche dello zoo e albero recinti → animali, con i comandi per rilasciare e curare. */
public final class ZooView extends VBox implements VistaAggiornabile {

    private final Label statistiche = new Label();
    private final TreeView<Object> albero = new TreeView<>();
    private final Button bottoneRilascia = new Button("Rilascia");
    private final Button bottoneCura = new Button("Cura…");
    private Consumer<Animale> alRilascio = animale -> { };
    private Consumer<Animale> allaCura = animale -> { };

    public ZooView() {
        super(4);
        statistiche.setWrapText(true);
        VBox.setVgrow(albero, Priority.ALWAYS);

        albero.setCellFactory(vista -> new TreeCell<Object>() {
            @Override
            protected void updateItem(Object elemento, boolean vuoto) {
                super.updateItem(elemento, vuoto);
                setText(vuoto || elemento == null ? null : testo(elemento));
            }
        });

        var nessunAnimale = Bindings.createBooleanBinding(
                () -> animaleSelezionato().isEmpty(),
                albero.getSelectionModel().selectedItemProperty());
        bottoneRilascia.disableProperty().bind(nessunAnimale);
        bottoneCura.disableProperty().bind(nessunAnimale);
        bottoneRilascia.setOnAction(e -> animaleSelezionato().ifPresent(animale -> alRilascio.accept(animale)));
        bottoneCura.setOnAction(e -> animaleSelezionato().ifPresent(animale -> allaCura.accept(animale)));

        getChildren().addAll(new Label("Zoo"), statistiche, albero, new HBox(6, bottoneRilascia, bottoneCura));
    }

    /** Azione da eseguire quando il giocatore preme "Rilascia" con un animale selezionato. */
    public void setOnRilascia(Consumer<Animale> azione) {
        this.alRilascio = azione;
    }

    /** Azione da eseguire quando il giocatore preme "Cura…" con un animale selezionato. */
    public void setOnCura(Consumer<Animale> azione) {
        this.allaCura = azione;
    }

    @Override
    public void aggiorna(GameController controller) {
        Optional<Animale> precedente = animaleSelezionato();
        Zoo zoo = controller.getZoo();
        statistiche.setText(Messaggi.statistiche(zoo.getStatistiche()));

        TreeItem<Object> radice = new TreeItem<>(zoo.getNome());
        radice.setExpanded(true);
        for (Recinto recinto : zoo.getRecinti()) {
            TreeItem<Object> nodo = new TreeItem<>(recinto);
            nodo.setExpanded(true);
            for (Animale animale : recinto.getAnimali()) {
                nodo.getChildren().add(new TreeItem<>(animale));
            }
            radice.getChildren().add(nodo);
        }
        albero.setRoot(radice);
        precedente.ifPresent(this::seleziona);
    }

    private Optional<Animale> animaleSelezionato() {
        TreeItem<Object> nodo = albero.getSelectionModel().getSelectedItem();
        if (nodo != null && nodo.getValue() instanceof Animale animale) {
            return Optional.of(animale);
        }
        return Optional.empty();
    }

    /** Dopo un aggiornamento ripristina la selezione sullo stesso animale, se è ancora nello zoo. */
    private void seleziona(Animale animale) {
        for (TreeItem<Object> nodo : albero.getRoot().getChildren()) {
            for (TreeItem<Object> foglia : nodo.getChildren()) {
                if (animale.equals(foglia.getValue())) {
                    albero.getSelectionModel().select(foglia);
                    return;
                }
            }
        }
    }

    private static String testo(Object elemento) {
        return switch (elemento) {
            case Animale animale -> Messaggi.animale(animale);
            case Recinto recinto -> Messaggi.recinto(recinto);
            default -> elemento.toString();
        };
    }
}