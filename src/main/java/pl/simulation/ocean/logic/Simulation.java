package pl.simulation.ocean.logic;

import pl.simulation.ocean.model.*;

import javax.swing.*;
import java.util.List;
import java.util.Random;

/**
 * Główna klasa kontrolująca przebieg symulacji życia w oceanie.
 * Pełni rolę silnika całej aplikacji – odpowiada za inicjalizację struktur danych,
 * zarządzanie pętlą czasu (turami) oraz koordynowaniem działań poszczególnych
 * obsługiwaczy (turn handlers) dla organizmów żywych.
 */
public class Simulation {

    /** Obiekt reprezentujący środowisko oceanu i przechowujący jego aktualny stan. */
    private Ocean ocean;
    
    /** Obsługiwacz odpowiedzialny za fazę ruchu i odżywiania ryb. */
    private final FishTurnHandler fishHandler;
    
    /** Obsługiwacz odpowiedzialny za fazę ruchu i polowania rekinów. */
    private final SharkTurnHandler sharkHandler;
    
    /** Licznik odmierzający aktualny numer tury symulacji. */
    private int turnNumber = 0;

    /**
     * Tworzy nową instancję symulacji z domyślnie włączonym trybem raportowania (verbose).
     *
     * @param random Generator liczb pseudolosowych przekazywany do komponentów logicznych
     */
    public Simulation(Random random) {
        this(random, true);
    }

    /**
     * Tworzy nową instancję symulacji, inicjalizuje świat oceanu oraz rozmieszcza
     * na nim początkowe obiekty za pomocą klasy {@code OceanInitializer}.
     *
     * @param random  Generator liczb pseudolosowych zapewniający losowość rozstawienia i ruchu
     * @param verbose Flaga określająca, czy komunikaty o przebiegu inicjalizacji mają być wypisane w konsoli
     */
    public Simulation(Random random, boolean verbose) {
        this.ocean = new Ocean();
        this.fishHandler = new FishTurnHandler(random, verbose);
        this.sharkHandler = new SharkTurnHandler(random, verbose);

        // Automatyczne napełnienie oceanu organizmami na starcie
        OceanInitializer initializer = new OceanInitializer(random);
        initializer.initialize(ocean, verbose);
    }

    /**
     * Uruchamia automatyczną symulację w pętli tekstowej.
     * Metoda wykonuje kolejne tury jedna po drugiej, dopóki nie zostanie spełniony
     * warunek zakończenia symulacji. Na koniec wypisuje statystyki końcowe.
     */
    public void run() {
        System.out.println("- Start symulacji -\n");

        // Pętla wykonuje się dopóki executeNextTurn zwraca true
        while (executeNextTurn(true)) {}

        printFinalStats();
    }

    /**
     * Wykonuje pojedynczy krok (turę) symulacji.
     * W ramach jednej tury czasowej zachodzi określona sekwencja zdarzeń:
     * 1. Inkrementacja licznika tur.
     * 2. Wykonanie akcji przez wszystkie żywe ryby (ruch, zjedzenie planktonu).
     * 3. Wykonanie akcji przez wszystkie żywe rekiny (ruch, polowanie na ryby, zjedzenie planktonu).
     * 4. Opcjonalne wypisanie aktualnego stanu populacji w oceanie.
     *
     * @param verbose Flaga decydująca o wypisywaniu nagłówka tury i stanu oceanu w konsoli
     * @return {@code true} jeśli symulacja powinna być kontynuowana; {@code false} jeśli osiągnięto stan końca symulacji
     */
    public boolean executeNextTurn(boolean verbose) {
        // Sprawdzenie warunku stopu przed rozpoczęciem tury
        if (ocean.isSimulationOver()) {
            return false;
        }

        turnNumber++;
        if (verbose) {
            System.out.println("- Tura " + turnNumber + " -");
        }

        // Faza akcji ryb
        List<Fish> liveFish = ocean.getLiveFish();
        for (Fish fish : liveFish) {
            fishHandler.executeTurn(fish, ocean);
        }

        // Faza akcji rekinów
        List<Shark> liveSharks = ocean.getLiveSharks();
        for (Shark shark : liveSharks) {
            sharkHandler.executeTurn(shark, ocean);
        }

        if (verbose) {
            ocean.printStatus(turnNumber);
        }

        // Zwróć true, jeśli symulacja ma trwać dalej
        return !ocean.isSimulationOver();
    }

    /**
     * Sprawdza, czy symulacja dobiegła już końca (np. z powodu wymarcia populacji).
     *
     * @return {@code true} jeśli warunki końca symulacji zostały spełnione; w przeciwnym razie {@code false}
     */
    public boolean isFinished() {
        return ocean.isSimulationOver();
    }

    /**
     * Wypisuje w konsoli podsumowanie oraz końcowe statystyki liczbowe 
     * po zakończeniu działania algorytmu symulacji.
     */
    private void printFinalStats() {
        System.out.println("- Koniec symulacji po " + turnNumber + " turach -");
        System.out.println("  Żywe rekiny:  " + ocean.getLiveSharks().size());
        System.out.println("  Żywy plankton:" + ocean.getLivePlankton().size());
        System.out.println("  Wszystkie rybki zginęły.");
    }

    /**
     * Zwraca obiekt oceanu powiązany z tą symulacją.
     *
     * @return Aktualna instancja klasy {@code Ocean}
     */
    public Ocean getOcean() {
        return ocean;
    }

    /**
     * Pobiera aktualny numer tury.
     *
     * @return Liczba całkowita reprezentująca upływ czasu w turach
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * Ustawia numer tury symulacji na określoną wartość
     *
     * @param turnNumber Nowy numer tury do ustawienia
     */
    void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }
}
