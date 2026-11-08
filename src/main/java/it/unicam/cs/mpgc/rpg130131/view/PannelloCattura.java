package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.GameController;
import it.unicam.cs.mpgc.rpg130131.model.animals.Animale;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Oggetto;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/** Mostra l'animale presente nella cella e permette di scegliere strumento ed esca per catturarlo. */
public class PannelloCattura extends VBox implements VistaAggiornabile {

    private final Label descrizione = new Label();
    private final Label probabilita = new Label();
    private final ChoiceBox<StrumentoCattura> sceltaStrumento = new ChoiceBox<>();
    private final ChoiceBox<Cibo> sceltaEsca = new ChoiceBox<>();
    private final Button bottoneCattura = new Button("Cattura (Spazio)");
    private GameController controller;
    private Inventario inventario;

    public PannelloCattura() {
        super(6);
        setPadding(new Insets(0, 0, 6, 0));

        descrizione.setWrapText(true);
        sceltaStrumento.setMaxWidth(Double.MAX_VALUE);
        sceltaEsca.setMaxWidth(Double.MAX_VALUE);
        bottoneCattura.setMaxWidth(Double.MAX_VALUE);

        sceltaStrumento.setConverter(new StringConverter<StrumentoCattura>() {
            @Override
            public String toString(StrumentoCattura strumento) {
                return strumento == null ? "" : Messaggi.strumento(strumento, quantita(strumento));
            }

            @Override
            public StrumentoCattura fromString(String testo) {
                throw new UnsupportedOperationException();
            }
        });
        sceltaEsca.setConverter(new StringConverter<Cibo>() {
            @Override
            public String toString(Cibo cibo) {
                return cibo == null ? "" : Messaggi.cibo(cibo, quantita(cibo));
            }

            @Override
            public Cibo fromString(String testo) {
                throw new UnsupportedOperationException();
            }
        });
        sceltaStrumento.valueProperty().addListener((osservato, vecchio, nuovo) -> aggiornaProbabilita());
        sceltaEsca.valueProperty().addListener((osservato, vecchia, nuova) -> aggiornaProbabilita());

        getChildren().addAll(
                new Label("Cattura"), descrizione,
                new Label("Strumento"), sceltaStrumento,
                new Label("Esca"), sceltaEsca,
                probabilita, bottoneCattura);
    }

    /** Imposta l'azione da eseguire quando il giocatore preme il pulsante. */
    public void setOnCattura(Runnable azione) {
        bottoneCattura.setOnAction(e -> azione.run());
    }

    /** Simula la pressione del pulsante (non fa nulla se è disabilitato). */
    public void cattura() {
        bottoneCattura.fire();
    }

    public Optional<StrumentoCattura> getStrumentoScelto() {
        return Optional.ofNullable(sceltaStrumento.getValue());
    }

    public Cibo getEscaScelta() {
        return Objects.requireNonNullElse(sceltaEsca.getValue(), Cibo.NESSUNA_ESCA);
    }

    @Override
    public void aggiorna(GameController controller) {
        this.controller = controller;
        this.inventario = controller.getGiocatore().getInventario();

        List<StrumentoCattura> strumenti =
                inventario.getContenutoDi(StrumentoCattura.class).keySet().stream().toList();
        List<Cibo> esche = new ArrayList<>();
        esche.add(Cibo.NESSUNA_ESCA);
        esche.addAll(inventario.getContenutoDi(Cibo.class).keySet());
        aggiornaScelta(sceltaStrumento, strumenti);
        aggiornaScelta(sceltaEsca, esche);

        Optional<Animale> animale = controller.animaleQui();
        String testo = animale.map(a -> "Qui c'è: " + Messaggi.animale(a))
                .orElse("Nessun animale in questa cella.");
        if (strumenti.isEmpty()) {
            testo += "\nNon hai strumenti di cattura.";
        }
        descrizione.setText(testo);
        bottoneCattura.setDisable(animale.isEmpty() || strumenti.isEmpty());
        aggiornaProbabilita();
    }

    private void aggiornaProbabilita() {
        StrumentoCattura strumento = sceltaStrumento.getValue();
        if (controller == null || strumento == null) {
            probabilita.setText("");
            return;
        }
        OptionalDouble stima = controller.anteprimaProbabilita(strumento, getEscaScelta());
        probabilita.setText(stima.isPresent() ? Messaggi.anteprima(stima.getAsDouble()) : "");
    }

    private int quantita(Oggetto oggetto) {
        return inventario == null ? 0 : inventario.quantita(oggetto);
    }

    /** Aggiorna le voci mantenendo la scelta precedente, se è ancora disponibile. */
    private static <T> void aggiornaScelta(ChoiceBox<T> scelta, List<T> elementi) {
        T precedente = scelta.getValue();
        scelta.getItems().setAll(elementi);
        if (precedente != null && elementi.contains(precedente)) {
            scelta.setValue(precedente);
        } else {
            scelta.setValue(elementi.isEmpty() ? null : elementi.getFirst());
        }
    }
}