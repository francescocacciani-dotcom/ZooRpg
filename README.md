# 🐾 Zoo Rescue RPG

Gioco di ruolo in Java (JavaFX) in cui si esplorano mappe per catturare animali selvatici o in pericolo e portarli nel proprio zoo.
Gli animali si possono curare o rilasciare in cambio di monete, da spendere nel negozio per comprare strumenti, cibo, medicine e nuovo spazio per i recinti.

Progetto d'esame di *Metodologie di Programmazione* (Università di Camerino, A.A. 2025/26) — package `it.unicam.cs.mpgc.rpg130131`.
La documentazione completa è nella [**Wiki**](https://github.com/francescocacciani-dotcom/ZooRpg/wiki).

---

## 🚀 Come eseguire il progetto

### Prerequisiti
- Java 25 (LTS)
- Gradle (non serve installarlo: il repository include il Gradle Wrapper)

### Istruzioni

```bash
git clone https://github.com/francescocacciani-dotcom/ZooRpg.git
cd ZooRpg
```

### Build del progetto
```bash
./gradlew build
```

### Esecuzione
```bash
./gradlew run
```

> Su Windows: `gradlew.bat build` e `gradlew.bat run`.


### Comandi di gioco
- **Frecce** o **W A S D**: muoversi
- **Barra spaziatrice**: tentare la cattura dell'animale nella cella
- **Rilascia** / **Cura…** (pannello Zoo) e **Negozio** (`Ctrl+B`); menu *Partita*: nuova partita, salva, carica

---

## 🤖 Uso di strumenti di IA

Sono state usate diverse IA per scopi diversi tra di loro

## NotebookLM

- **Studio** di argomenti più complessi.
- Verifica delle conoscenze tramite test.

## Gemini
- collegato a NotebookLM per avere conferma delle mie idee del progetto e delle dinamiche di gioco
- **Chiarimento** di concetti avanzati di Java

## Claude 

- **Struttura** discussa e confermata da claude 
- **Debug:** risoluzione di problemi di compilazione durante le prime fasi del progetto (Problemi con OneDrive).
- **Controllo finale** del progetto

## Qwen3.5:9b (su claude code)

- **Scrittura codice boilerplate** (metodi getter e setter, classi degli animali)
- **Scrittura test** per la validazione del codice
- **Scrittura JavaDoc** 

### Documentazione
La dichiarazione dettagliata è nella [Wiki](https://github.com/francescocacciani-dotcom/ZooRpg/wiki/Home).
