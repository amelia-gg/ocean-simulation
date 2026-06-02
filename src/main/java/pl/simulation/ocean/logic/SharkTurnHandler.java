package pl.simulation.ocean.logic;

import pl.simulation.ocean.model.*;
import pl.simulation.ocean.util.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Klasa odpowiedzialna za obsługę tury pojedynczego rekina w symulacji.
 * Zarządza procesem decyzyjnym, wykonaniem sekwencji ruchów,
 * zużyciem energii, polowaniem na ryby oraz alternatywnym odżywianiem się planktonem.
 */
public class SharkTurnHandler {

    /** Generator liczb pseudolosowych wykorzystywany przy wyborze strategii losowego ruchu. */
    private final Random random;
    
    /** Flaga określająca, czy w konsoli mają być wyświetlane szczegółowe komunikaty o działaniach rekina. */
    private final boolean verbose;

    /**
     * Tworzy nowy obiekt obsługujący turę rekina z domyślnie włączonym trybem szczegółowego logowania (czyli verbose).
     *
     * @param random Generator liczb pseudolosowych
     */
    public SharkTurnHandler(Random random) {
        this(random, true);
    }

    /**
     * Tworzy nowy obiekt obsługujący turę rekina z możliwością konfiguracji flagi verbose.
     *
     * @param random  Generator liczb pseudolosowych
     * @param verbose Flaga włączająca/wyłączająca szczegółowe komunikaty w konsoli
     */
    public SharkTurnHandler(Random random, boolean verbose) {
        this.random = random;
        this.verbose = verbose;
    }

    /**
     * Wykonuje pełną turę dla wskazanego rekina. W ramach jednej tury rekin podejmuje 
     * maksymalnie tyle prób ruchu, ile definiuje stała {@code LivingEntity.MAX_MOVES_PER_TURN}.
     * W każdym kroku rekin dobiera strategię, przemieszcza się, zużywa energię,
     * a następnie sprawdza, czy na nowym polu może zaatakować rybę lub zjeść plankton.
     *
     * @param shark Rekin, którego tura jest aktualnie przetwarzana
     * @param ocean Stan oceanu w bieżącej turze symulacji
     */
    public void executeTurn(Shark shark, Ocean ocean) {
        // Jeśli rekin stracił życie, natychmiast zakończ jego turę
        if (!shark.isAlive())
            return;

        for (int move = 0; move < LivingEntity.MAX_MOVES_PER_TURN; move++) {
            if (!shark.isAlive())
                break;

            MovementStrategy strategy = chooseStrategy(shark, ocean);
            Position next = strategy.nextPosition(shark, ocean);
            shark.setPosition(next);
            shark.consumeMoveEnergy();

            // Sprawdzenie interakcji z innymi obiektami na nowej pozycji
            checkFishAttack(shark, ocean);
            checkPlanktonEaten(shark, ocean);
        }
    }

    /**
     * Wybiera strategię ruchu dla rekina na podstawie analizy jego najbliższego otoczenia.
     * Priorytety decyzyjne rekina:
     * 1. Polowanie: Jeśli w zasięgu znajduje się przynajmniej jedna żywa ryba, 
     * rekin wybiera strategię podążania za najbliższą z nich.
     * 2. Konsumpcja planktonu: Jeśli w zasięgu nie ma ryb, ale jest plankton, 
     * rekin płynie w kierunku najbliższego planktonu.
     * 3. Ruch losowy: W przypadku braku jakichkolwiek obiektów w zasięgu detekcji, 
     * rekin porusza się w losowym kierunku.
     *
     * @param shark Rekin podejmujący decyzję o kolejnym kroku
     * @param ocean Obiekt oceanu używany do przeszukiwania list żywych organizmów
     * @return Wybrana strategia ruchu zaimplementowana z interfejsu {@code MovementStrategy}
     */
    private MovementStrategy chooseStrategy(Shark shark, Ocean ocean) {
        Position pos = shark.getPosition();

        // 1. Szukanie najbliższej ryby w zasięgu detekcji
        Optional<Fish> nearestFish = ocean.getLiveFish().stream()
                .filter(f -> pos.distanceTo(f.getPosition()) <= Shark.DETECTION_RANGE)
                .min(Comparator.comparingDouble(f -> pos.distanceTo(f.getPosition())));

        if (nearestFish.isPresent()) {
            return new MoveToTargetStrategy(nearestFish.get().getPosition());
        }

        // 2. Szukanie najbliższego planktonu w zasięgu detekcji (plankton jako pokarm alternatywny)
        Optional<Plankton> nearestPlankton = ocean.getLivePlankton().stream()
                .filter(p -> pos.distanceTo(p.getPosition()) <= Shark.DETECTION_RANGE)
                .min(Comparator.comparingDouble(p -> pos.distanceTo(p.getPosition())));

        if (nearestPlankton.isPresent()) {
            return new MoveToTargetStrategy(nearestPlankton.get().getPosition());
        }

        // 3. Brak celów w zasięgu – ruch losowy
        return new RandomMovementStrategy(random);
    }

    /**
     * Weryfikuje, czy rekin po wykonaniu ruchu znalazł się na tym samym polu co żywa ryba.
     * W przypadku wykrycia ryby następuje atak na nią, co skutkuje zmianą jej stanu zdrowia/energii 
     * oraz potencjalnym zasileniem energii rekina.
     *
     * @param shark Rekin wykonujący test ataku
     * @param ocean Obiekt oceanu zawierający aktualną listę żywych ryb
     */
    private void checkFishAttack(Shark shark, Ocean ocean) {
        List<Fish> fishList = ocean.getLiveFish();
        for (Fish fish : fishList) {
            if (shark.getPosition().equals(fish.getPosition())) {
                shark.attack(fish);
                
                if (verbose) {
                    System.out.println("  " + shark.getName() + " zaatakował " + fish.getName()
                            + " na " + shark.getPosition()
                            + " | energia rybki: " + fish.getEnergy()
                            + " | energia rekina: " + shark.getEnergy());
                    if (!fish.isAlive()) {
                        System.out.println("  " + fish.getName() + " zginęła!");
                    }
                }
                // Rekin atakuje tylko jedną rybę w jednym kroku ruchu
                break;
            }
        }
    }

    /**
     * Weryfikuje, czy rekin po wykonaniu ruchu znalazł się na tym samym polu co plankton.
     * Jeśli tak, plankton zostaje zjedzony, a rekin otrzymuje dodatkową energię określoną 
     * przez stałą {@code Shark.PLANKTON_ENERGY}.
     *
     * @param shark Rekin, który potencjalnie zjada plankton
     * @param ocean Obiekt oceanu zawierający aktualną listę żywego planktonu
     */
    private void checkPlanktonEaten(Shark shark, Ocean ocean) {
        List<Plankton> planktonList = ocean.getLivePlankton();
        for (Plankton plankton : planktonList) {
            if (shark.getPosition().equals(plankton.getPosition())) {
                plankton.eat();
                shark.gainEnergy(Shark.PLANKTON_ENERGY);
                
                if (verbose) {
                    System.out.println("  " + shark.getName() + " zjadł plankton na " + shark.getPosition()
                            + " | energia: " + shark.getEnergy());
                }
                // Rekin konsumuje tylko jedną jednostkę planktonu w jednym kroku ruchu
                break;
            }
        }
    }
}
