package pl.simulation.ocean.logic;

import pl.simulation.ocean.model.LivingEntity;
import pl.simulation.ocean.model.Ocean;
import pl.simulation.ocean.util.Position;

/**
 * Strategia ruchu polegająca na ucieczce obiektu przed zagrożeniem (np. rekinem).
 * Algorytm wyznacza wektor ruchu w kierunku przeciwnym do położenia zagrożenia.
 * W przypadku napotkania granicy planszy, strategia próbuje "ślizgać się" wzdłuż krawędzi,
 * a w sytuacji ostatecznego osaczenia nakazuje obiektowi pozostać w miejscu.
 */
public class FleeFromTargetStrategy implements MovementStrategy {
    /** Pozycja źródła zagrożenia, od którego obiekt próbuje się oddalić. */
    private final Position threat;

    /**
     * Tworzy nową strategię ucieczki przed określonym punktem zagrożenia.
     *
     * @param threat Pozycja zagrożenia (np. aktualna pozycja rekina dla uciekającej rybki)
     */
    public FleeFromTargetStrategy(Position threat) {
        this.threat = threat;
    }

    /**
     * Oblicza następną pozycję obiektu, starając się zwiększyć dystans od źródła zagrożenia.
     * Algorytm najpierw podejmuje próbę ruchu po przekątnej. Jeśli taki ruch wyprowadziłby
     * obiekt poza granice oceanu, koryguje go do ruchu tylko w jednej osi (X lub Y).
     *
     * @param entity Obiekt żywy (np. ryba), który próbuje uciekać
     * @param ocean  Instancja oceanu, używana do weryfikacji granic planszy
     * @return Nowa, bezpieczniejsza pozycja na mapie lub aktualna pozycja, jeśli ruch jest niemożliwy
     */
    @Override
    public Position nextPosition(LivingEntity entity, Ocean ocean) {
        Position current = entity.getPosition();
        int cx = current.getX();
        int cy = current.getY();

        // Wyznaczenie kierunku ucieczki (odwrotnie niż w przypadku pogoni: cx - threat)
        int dx = Integer.signum(cx - threat.getX());
        int dy = Integer.signum(cy - threat.getY());

        // Potencjalna nowa pozycja (domyślnie ruch po przekątnej)
        int nx = cx + dx;
        int ny = cy + dy;

        // Korekta ruchu w przypadku zderzenia z granicą planszy
        if (!ocean.isWithinBounds(nx, ny)) {
            if (ocean.isWithinBounds(nx, cy)) {
                // Jeśli ruch w pionie jest zablokowany, uciekaj tylko w poziomie
                ny = cy;
            } else if (ocean.isWithinBounds(cx, ny)) {
                // Jeśli ruch w poziomie jest zablokowany, uciekaj tylko w pionie
                nx = cx;
            } else {
                // Obiekt jest w rogu i nie ma gdzie uciekać – zostaje w miejscu
                return new Position(cx, cy);
            }
        }

        return new Position(nx, ny);
    }
}
