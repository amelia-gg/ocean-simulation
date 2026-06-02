package pl.simulation.ocean.logic;

import pl.simulation.ocean.model.*;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Klasa odpowiedzialna za początkową inicjalizację symulacji życia w oceanie.
 * Zajmuje się generowaniem i rozmieszczaniem obiektów (rekinów, ryb, planktonu) 
 * na losowych, unikalnych pozycjach w obrębie planszy.
 */
public class OceanInitializer {

    /** Domyślna początkowa liczba rekinów w oceanie. */
    private static final int SHARK_COUNT = 4;
    
    /** Domyślna początkowa liczba ryb w oceanie. */
    private static final int FISH_COUNT = 8;
    
    /** Domyślna początkowa liczba jednostek planktonu w oceanie. */
    private static final int PLANKTON_COUNT = 12;

    /** Generator liczb pseudolosowych wykorzystywany do losowania współrzędnych. */
    private final Random random;

    /**
     * Tworzy nowy obiekt inicjalizujący ocean.
     * * @param random Instancja klasy Random, zapewniająca determinizm lub losowość symulacji
     */
    public OceanInitializer(Random random) {
        this.random = random;
    }

    /**
     * Inicjalizuje podany ocean z domyślnie włączonym logowaniem (tryb verbose).
     * * @param ocean Obiekt oceanu, do którego zostaną dodane zwierzęta i plankton
     */
    public void initialize(Ocean ocean) {
        initialize(ocean, true);
    }

    /**
     * Główna metoda inicjalizująca ocean. Rozmieszcza określoną liczbę rekinów, 
     * ryb i planktonu, dbając o to, by dwa obiekty nie trafiły na to samo pole.
     * * @param ocean   Obiekt oceanu, który ma zostać zainicjalizowany
     * @param verbose Flaga określająca, czy wypisywać podsumowanie inicjalizacji w konsoli
     */
    public void initialize(Ocean ocean, boolean verbose) {
        Set<String> usedPositions = new HashSet<>();

        for (int i = 1; i <= SHARK_COUNT; i++) {
            int[] pos = randomFreePosition(usedPositions, ocean);
            ocean.addShark(new Shark("Rekin" + i, pos[0], pos[1]));
        }

        for (int i = 1; i <= FISH_COUNT; i++) {
            int[] pos = randomFreePosition(usedPositions, ocean);
            ocean.addFish(new Fish("Rybka" + i, pos[0], pos[1]));
        }

        for (int i = 1; i <= PLANKTON_COUNT; i++) {
            int[] pos = randomFreePosition(usedPositions, ocean);
            ocean.addPlankton(new Plankton(pos[0], pos[1]));
        }

        if (verbose) {
            System.out.println("- Inicjalizacja oceanu -");
            System.out.println("  Rekiny:   " + ocean.getSharks().size());
            System.out.println("  Rybki:    " + ocean.getFish().size());
            System.out.println("  Plankton: " + ocean.getPlanktons().size());
            System.out.println();
        }
    }

    /**
     * Losuje i zwraca niezajętą pozycję na mapie oceanu.
     * Wylosowana pozycja jest od razu dodawana do zbioru zajętych miejsc.
     * * @param used  Zbiór przechowujący użyte już koordynaty w formacie tekstowym "x,y"
     * @param ocean Obiekt oceanu udostępniający wymiary mapy (szerokość i wysokość)
     * @return Dwuelementowa tablica liczb całkowitych, gdzie indeks 0 to współrzędna X, a indeks 1 to współrzędna Y
     */
    private int[] randomFreePosition(Set<String> used, Ocean ocean) {
        int x, y;
        String key;
        do {
            x = random.nextInt(Ocean.WIDTH);
            y = random.nextInt(Ocean.HEIGHT);
            key = x + "," + y;
        } while (used.contains(key));
        used.add(key);
        return new int[] { x, y };
    }
}
