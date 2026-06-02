package pl.simulation.ocean.logic;

import pl.simulation.ocean.model.LivingEntity;
import pl.simulation.ocean.model.Ocean;
import pl.simulation.ocean.util.Position;

/**
 * Strategia ruchu polegająca na podążaniu obiektu w kierunku zdefiniowanego celu.
 * Algorytm oblicza pojedynczy krok (o 1 pole w pionie lub poziomie),
 * w pierwszej kolejności preferując ruch wzdłuż tej osi (X lub Y), 
 * na której dystans do celu jest największy.
 */
public class MoveToTargetStrategy implements MovementStrategy {

    /** Docelowa pozycja, do której ma zmierzać obiekt. */
    private final Position target;

    /**
     * Tworzy nową strategię ruchu nakierowaną na konkretny punkt.
     *
     * @param target Pozycja docelowa na mapie oceanu
     */
    public MoveToTargetStrategy(Position target) {
        this.target = target;
    }

    /**
     * Oblicza i zwraca następną pozycję dla obiektu, przybliżając go do celu.
     * Metoda uwzględnia granice mapy i wybiera ruch (lewo/prawo lub góra/dół).
     * Jeśli ruch w preferowanym kierunku jest zablokowany przez krawędź planszy, 
     * algorytm próbuje wykorzystać drugą oś.
     *
     * @param entity Obiekt żywy (np. ryba, rekin), dla którego wyznaczany jest nowy krok
     * @param ocean  Instancja oceanu, wykorzystywana do weryfikacji współrzędnych i granic planszy
     * @return Nowa pozycja po wykonaniu ruchu lub obecna pozycja, jeśli żaden ruch nie jest możliwy
     */
    @Override
    public Position nextPosition(LivingEntity entity, Ocean ocean) {
        Position current = entity.getPosition();
        int cx = current.getX();
        int cy = current.getY();

        // Kierunek ruchu: -1, 0 lub 1
        int dx = Integer.signum(target.getX() - cx);
        int dy = Integer.signum(target.getY() - cy);

        // Bezwzględna odległość do pokonania na obu osiach
        int absDx = Math.abs(target.getX() - cx);
        int absDy = Math.abs(target.getY() - cy);

        int nx = cx;
        int ny = cy;

        // Preferuj ruch po osi z większą różnicą
        if (absDx >= absDy && ocean.isWithinBounds(cx + dx, cy)) {
            nx = cx + dx;
        } else if (absDy > 0 && ocean.isWithinBounds(cx, cy + dy)) {
            ny = cy + dy;
        } else if (absDx > 0 && ocean.isWithinBounds(cx + dx, cy)) {
            nx = cx + dx;
        }

        return new Position(nx, ny);
    }
}
