package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.ConfigurazioneGioco;
import it.unicam.cs.mpgc.rpg130131.controller.GameFactory;
import it.unicam.cs.mpgc.rpg130131.model.animals.RegistroSpecie;
import it.unicam.cs.mpgc.rpg130131.model.map.GeneratoreMappaARegioni;
import it.unicam.cs.mpgc.rpg130131.persistence.XMLCatalogReader;
import it.unicam.cs.mpgc.rpg130131.persistence.XMLSaveRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.random.RandomGenerator;

/**
 * Applicazione JavaFX e composition root: qui, e solo qui, si scelgono le implementazioni
 * concrete (catalogo e salvataggio XML, generatore di mappa a regioni).
 */
public class GameApp extends Application {

    private static final String NOME_PREDEFINITO = "Giocatore";

    @Override
    public void start(Stage finestra) {
        RegistroSpecie registro = RegistroSpecie.predefinito();
        Path fileSalvataggio = Path.of(System.getProperty("user.home"), ".zoorescue", "partita.xml");


        GameFactory factory = new GameFactory(
                XMLCatalogReader.predefinito(),
                registro,
                new GeneratoreMappaARegioni(),
                new XMLSaveRepository(fileSalvataggio, registro),
                ConfigurazioneGioco.predefinita(),
                RandomGenerator.getDefault());

        GamePane schermata = new GamePane(factory);
        schermata.nuovaPartita(NOME_PREDEFINITO);
        if (!schermata.haPartita()) {
            Platform.exit();
            return;
        }

        Scene scena = new Scene(schermata);
        scena.addEventFilter(KeyEvent.KEY_PRESSED, schermata::gestisciTasto);
        finestra.setTitle("Zoo Rescue RPG");
        finestra.setScene(scena);
        finestra.show();
    }
}
