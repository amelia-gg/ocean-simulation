package pl.simulation.ocean.logic;

import pl.simulation.ocean.model.*;
import pl.simulation.ocean.util.Position;

/**
 * Klasa reprezentująca migawkę (snapshot) stanu symulacji w konkretnym punkcie w czasie.
 * Umożliwia "zamrożenie" aktualnego stanu.
 * oceanu (pozycji i energii organizmów) oraz jego późniejsze odtworzenie, co pozwala 
 * np. na implementację mechanizmu cofania tur lub zapisywania przebiegu symulacji.
 * <p>
 * Dane obiektów są serializowane do tablic liczb całkowitych.
 * </p>
 */
public final class SimulationSnapshot {

    /** Numer tury, w której wykonano migawkę stanu. */
    private final int turnNumber;

    /** * Tablica stanów ryb. 
     * Każdy wiersz reprezentuje jedną rybę i zawiera 3 wartości: [0] - oś X, [1] - oś Y, [2] - energia.
     */
    private final int[][] fishState;

    /** * Tablica stanów rekinów. 
     * Każdy wiersz reprezentuje jednego rekina i zawiera 3 wartości: [0] - oś X, [1] - oś Y, [2] - energia.
     */
    private final int[][] sharkState;

    /** * Tablica stanów planktonu. 
     * Każdy wiersz reprezentuje jednostkę planktonu i zawiera 3 wartości: [0] - oś X, [1] - oś Y, [2] - status życia (0 = żywy, 1 = zjedzony).
     */
    private final int[][] planktonState;

    /**
     * Prywatny konstruktor inicjalizujący strukturę migawki.
     * Tworzenie obiektów tej klasy odbywa się wyłącznie poprzez statyczną metodę fabryczną {@link #capture(Simulation)}.
     *
     * @param turnNumber    Numer tury symulacji
     * @param fishState     Zrzut stanów ryb
     * @param sharkState    Zrzut stanów rekinów
     * @param planktonState Zrzut stanów planktonu
     */
    private SimulationSnapshot(int turnNumber, int[][] fishState, int[][] sharkState, int[][] planktonState) {
        this.turnNumber = turnNumber;
        this.fishState = fishState;
        this.sharkState = sharkState;
        this.planktonState = planktonState;
    }

    /**
     * Tworzy i zwraca nową migawkę zawierającą aktualny stan podanej symulacji.
     * Metoda pobiera dane (pozycji i energii) ze wszystkich list organizmów i zapisuje je do tablic.
     *
     * @param simulation Instancja aktywnej symulacji, której stan ma zostać zabezpieczony
     * @return Nowy obiekt {@code SimulationSnapshot} będący kopią bezpieczeństwa bieżącej tury
     */
    public static SimulationSnapshot capture(Simulation simulation) {
        Ocean ocean = simulation.getOcean();
        
        // Przechwytywanie stanu ryb
        int[][] fish = new int[ocean.getFish().size()][3];
        int i = 0;
        for (Fish f : ocean.getFish()) {
            Position p = f.getPosition();
            fish[i][0] = p.getX();
            fish[i][1] = p.getY();
            fish[i][2] = f.getEnergy();
            i++;
        }
        
        // Przechwytywanie stanu rekinów
        int[][] sharks = new int[ocean.getSharks().size()][3];
        i = 0;
        for (Shark s : ocean.getSharks()) {
            Position p = s.getPosition();
            sharks[i][0] = p.getX();
            sharks[i][1] = p.getY();
            sharks[i][2] = s.getEnergy();
            i++;
        }
        
        // Przechwytywanie stanu planktonu
        int[][] plankton = new int[ocean.getPlanktons().size()][3];
        i = 0;
        for (Plankton pl : ocean.getPlanktons()) {
            Position p = pl.getPosition();
            plankton[i][0] = p.getX();
            plankton[i][1] = p.getY();
            plankton[i][2] = pl.isAlive() ? 0 : 1; // 0 oznacza żywy, 1 oznacza nieżywy
            i++;
        }
        
        return new SimulationSnapshot(simulation.getTurnNumber(), fish, sharks, plankton);
    }

    /**
     * Przywraca stan zapisany w tej migawce do wskazanej instancji symulacji.
     * Metoda nadpisuje aktualny numer tury oraz iteruje po "polach" oceanu, wywołując na nich
     * metody {@code restoreState} z odpowiednimi parametrami z pamięci podręcznej.
     *
     * @param simulation Instancja symulacji, do której ma zostać wpisany stan z migawki
     */
    public void restore(Simulation simulation) {
        simulation.setTurnNumber(turnNumber);
        Ocean ocean = simulation.getOcean();
        
        // Odtwarzanie stanu ryb
        int i = 0;
        for (Fish f : ocean.getFish()) {
            f.restoreState(fishState[i][0], fishState[i][1], fishState[i][2]);
            i++;
        }
        
        // Odtwarzanie stanu rekinów
        i = 0;
        for (Shark s : ocean.getSharks()) {
            s.restoreState(sharkState[i][0], sharkState[i][1], sharkState[i][2]);
            i++;
        }
        
        // Odtwarzanie stanu planktonu
        i = 0;
        for (Plankton pl : ocean.getPlanktons()) {
            // Zamiana wartości całkowitej (0 lub 1) z powrotem na typ logiczny (boolean)
            pl.restoreState(planktonState[i][0], planktonState[i][1], planktonState[i][2] == 1);
            i++;
        }
    }
}
