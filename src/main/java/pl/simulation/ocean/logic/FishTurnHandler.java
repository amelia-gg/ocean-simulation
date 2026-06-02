package pl.simulation.ocean.logic;

import pl.simulation.ocean.model.*;
import pl.simulation.ocean.util.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Klasa odpowiedzialna za obsługę tury pojedynczej ryby w symulacji.
 * Zarządza procesem podejmowania decyzji o ruchu (wybór strategii),
 * przemieszczaniem się obiektu, zużyciem energii oraz odżywianiem się (zjadaniem planktonu).
 */
public class FishTurnHandler {

    /** Generator liczb pseudolosowych wykorzystywany do losowych ruchów ryby. */
    private final Random random;
    
    /** Flaga określająca, czy w konsoli mają być wyświetlane szczegółowe informacje o akcjach ryby. */
    private final boolean verbose;

    /**
     * Tworzy nowy obiekt obsługujący turę ryby z domyślnie włączonym trybem wypisywania informacji (verbose).
     *
     * @param random Generator liczb pseudolosowych
     */
    public FishTurnHandler(Random random) {
        this(random, true);
    }

    /**
     * Tworzy nowy obiekt obsługujący turę ryby z możliwością konfiguracji trybu verbose.
     *
     * @param random  Generator liczb pseudolosowych
     * @param verbose Flaga włączająca/wyłączająca szczegółowe logowanie akcji w konsoli
     */
    public FishTurnHandler(Random random, boolean verbose) {
        this.random = random;
        this.verbose = verbose;
    }

    /**
     * Wykonuje pełną turę dla podanej ryby. W ramach jednej tury ryba może wykonać
     * określoną liczbę ruchów (zdefiniowaną w {@code LivingEntity.MAX_MOVES_PER_TURN}).
     * W każdym kroku ryba analizuje otoczenie, wybiera strategię, przemieszcza się,
     * traci energię i weryfikuje, czy znalazła pożywienie.
     *
     * @param fish  Ryba, dla której symulowana jest obecna tura
     * @param ocean Stan oceanu w bieżącej turze
     */
    public void executeTurn(Fish fish, Ocean ocean) {
        // Jeśli ryba nie żyje (np. zjedzona w tej samej turze), natychmiast przerwij
        if (!fish.isAlive())
            return;

        for (int move = 0; move < LivingEntity.MAX_MOVES_PER_TURN; move++) {
            if (!fish.isAlive())
                break;

            MovementStrategy strategy = chooseStrategy(fish, ocean);
            Position next = strategy.nextPosition(fish, ocean);
            fish.setPosition(next);
            fish.consumeMoveEnergy();

            checkPlanktonEaten(fish, ocean);
        }
    }

    /**
     * Analizuje otoczenie ryby i wybiera optymalną strategię ruchu na dany krok.
     * Priorytety decyzyjne ryby:
     * 1. Ucieczka: Jeśli w zasięgu detekcji znajduje się rekin, ryba ucieka przed najbliższym.
     * 2. Żerowanie: Jeśli w zasięgu detekcji znajduje się plankton, ryba płynie do najbliższego.
     * 3. Swobodne pływanie: W przeciwnym razie ryba porusza się losowo.
     *
     * @param fish  Ryba podejmująca decyzję o ruchu
     * @param ocean Obiekt oceanu wykorzystywany do skanowania otoczenia
     * @return Wybrana strategia ruchu zaimplementowana z interfejsu {@code MovementStrategy}
     */
    private MovementStrategy chooseStrategy(Fish fish, Ocean ocean) {
        Position pos = fish.getPosition();

        // Poszukiwanie najbliższego rekina w zasięgu detekcji
        Optional<Shark> nearestShark = ocean.getLiveSharks().stream()
                .filter(s -> pos.distanceTo(s.getPosition()) <= Fish.DETECTION_RANGE)
                .min(Comparator.comparingDouble(s -> pos.distanceTo(s.getPosition())));

        if (nearestShark.isPresent()) {
            return new FleeFromTargetStrategy(nearestShark.get().getPosition());
        }

        // Poszukiwanie najbliższego planktonu w zasięgu detekcji
        Optional<Plankton> nearestPlankton = ocean.getLivePlankton().stream()
                .filter(p -> pos.distanceTo(p.getPosition()) <= Fish.DETECTION_RANGE)
                .min(Comparator.comparingDouble(p -> pos.distanceTo(p.getPosition())));

        if (nearestPlankton.isPresent()) {
            return new MoveToTargetStrategy(nearestPlankton.get().getPosition());
        }

        // Domyślny ruch, gdy wokół nie ma zagrożeń ani jedzenia
        return new RandomMovementStrategy(random);
    }

    /**
     * Sprawdza, czy po wykonaniu ruchu ryba znalazła się na tym samym polu co plankton.
     * Jeśli tak, plankton zostaje zjedzony, a ryba odzyskuje część energii.
     *
     * @param fish  Ryba, która potencjalnie zjada plankton
     * @param ocean Obiekt oceanu zawierający listę żywego planktonu
     */
    private void checkPlanktonEaten(Fish fish, Ocean ocean) {
        List<Plankton> planktonList = ocean.getLivePlankton();
        for (Plankton plankton : planktonList) {
            if (fish.getPosition().equals(plankton.getPosition())) {
                plankton.eat();
                fish.gainEnergy(Fish.PLANKTON_ENERGY);
                
                if (verbose) {
                    System.out.println("  " + fish.getName() + " zjadła plankton na " + fish.getPosition()
                            + " | energia: " + fish.getEnergy());
                }
                
                // Ryba zjada tylko jeden plankton naraz (nawet jeśli z jakiegoś powodu na polu byłoby więcej)
                break;
            }
        }
    }
}
