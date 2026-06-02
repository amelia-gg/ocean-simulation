package pl.simulation.ocean.logic;

import pl.simulation.ocean.model.LivingEntity;
import pl.simulation.ocean.model.Ocean;
import pl.simulation.ocean.util.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Strategia losowego ruchu obiektu w oceanie.
 * Klasa odpowiada za losowanie kolejnego kroku spośród wszystkich dostępnych i poprawnych sąsiednich pól.
 * Ruch może odbywać się w 8 kierunkach (w pionie, poziomie oraz po przekątnych).
 */
public class RandomMovementStrategy implements MovementStrategy {

    /**
     * Dwuwymiarowa tablica przesunięćreprezentująca 8 możliwych kierunków ruchu wokół obiektu.
     * Zawiera kombinacje ruchów: góra, dół, lewo, prawo oraz skosy.
     */
    private static final int[][] DIRECTIONS = {
            { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 },
            { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
    };

    /** Generator liczb pseudolosowych używany do wyboru losowego kierunku z puli dostępnych. */
    private final Random random;

    /**
     * Tworzy nową strategię losowego ruchu.
     *
     * @param random Instancja klasy Random używana do losowania kierunku ruchu
     */
    public RandomMovementStrategy(Random random) {
        this.random = random;
    }

    /**
     * Oblicza losową następną pozycję dla obiektu, sprawdzając przed tym poprawność współrzędnych.
     * Metoda analizuje wszystkie 8 kierunków otaczających obiekt i odrzuca te, które wychodzą 
     * poza granice mapy oceanu, a następnie losowo wybiera jedno z poprawnych pól.
     *
     * @param entity Obiekt żywy, dla którego losowany jest kolejny krok
     * @param ocean  Instancja oceanu, służąca do weryfikacji granic planszy
     * @return Nowa, losowo wybrana pozycja sąsiadująca lub aktualna pozycja, jeśli brak poprawnych pól ruchu
     */
    @Override
    public Position nextPosition(LivingEntity entity, Ocean ocean) {
        Position current = entity.getPosition();
        int cx = current.getX();
        int cy = current.getY();

        List<Position> valid = new ArrayList<>();
        
        // Iteracja po wszystkich zdefiniowanych kierunkach i filtrowanie poprawnych pozycji
        for (int[] dir : DIRECTIONS) {
            int nx = cx + dir[0];
            int ny = cy + dir[1];
            
            // Dodaj pozycję do listy tylko, jeśli mieści się w granicach oceanu
            if (ocean.isWithinBounds(nx, ny)) {
                valid.add(new Position(nx, ny));
            }
        }

        // Zabezpieczenie na wypadek specyficznych sytuacji, gdy żaden ruch nie jest możliwy
        if (valid.isEmpty())
            return new Position(cx, cy);
            
        // Losowy wybór i zwrot jednej z poprawnych pozycji z listy
        return valid.get(random.nextInt(valid.size()));
    }
}
