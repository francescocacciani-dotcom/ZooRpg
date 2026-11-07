package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.GameController;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Recinto;
import it.unicam.cs.mpgc.rpg130131.model.zoo.Zoo;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Statistiche dello zoo e albero recinti → animali. */
public class ZooView extends VBox implements VistaAggiornabile {

    private final Label statistiche = new Label();
    private final TreeView<String> albero = new TreeView<>();

    public ZooView() {
        super(4);
        statistiche.setWrapText(true);
        VBox.setVgrow(albero, Priority.ALWAYS);
        getChildren().addAll(new Label("Zoo"), statistiche, albero);
    }

    @Override
    public void aggiorna(GameController controller) {
        Zoo zoo = controller.getZoo();
        statistiche.setText(Messaggi.statistiche(zoo.getStatistiche()));

        TreeItem<String> radice = new TreeItem<>(zoo.getNome());
        radice.setExpanded(true);
        for (Recinto recinto : zoo.getRecinti()) {
            TreeItem<String> nodo = new TreeItem<>(Messaggi.recinto(recinto));
            nodo.setExpanded(true);
            recinto.getAnimali().forEach(animale ->
                    nodo.getChildren().add(new TreeItem<>(Messaggi.animale(animale))));
            radice.getChildren().add(nodo);
        }
        albero.setRoot(radice);
    }
}