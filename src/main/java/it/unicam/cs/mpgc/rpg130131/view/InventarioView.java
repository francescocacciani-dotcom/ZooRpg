package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.GameController;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Elenco di strumenti e cibi posseduti dal giocatore. */
public class InventarioView extends VBox implements VistaAggiornabile {

    private final ListView<String> elenco = new ListView<>();

    public InventarioView() {
        super(4);
        elenco.setPrefHeight(110);
        getChildren().addAll(new Label("Inventario"), elenco);
    }

    @Override
    public void aggiorna(GameController controller) {
        Inventario inventario = controller.getGiocatore().getInventario();
        List<String> righe = new ArrayList<>();
        inventario.getContenutoDi(StrumentoCattura.class)
                .forEach((strumento, quantita) -> righe.add(Messaggi.strumento(strumento, quantita)));
        inventario.getContenutoDi(Cibo.class)
                .forEach((cibo, quantita) -> righe.add(Messaggi.cibo(cibo, quantita)));
        if (righe.isEmpty()) {
            righe.add("Vuoto");
        }
        elenco.getItems().setAll(righe);
    }
}