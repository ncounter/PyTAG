package games.dominion.actions;

import core.AbstractGameState;
import core.actions.*;
import games.dominion.*;
import games.dominion.cards.*;

import java.util.*;

import static java.util.stream.Collectors.*;

public class Mine extends DominionAction implements IExtendedSequence {

    boolean trashedCard;
    boolean gainedCard;
    int trashValue;

    public final int BONUS_OVER_TRASHED_VALUE = 3;

    public Mine(int playerId) {
        super(CardType.MINE, playerId);
    }

    @Override
    boolean _execute(DominionGameState state) {
        if (state.getDeck(DominionConstants.DeckType.HAND, player).stream().anyMatch(DominionCard::isTreasureCard)) {
            state.setActionInProgress(this);
            return true;
        }
        trashedCard = true;
        gainedCard = true;
        return false;
    }

    @Override
    public List<AbstractAction> followOnActions(DominionGameState state) {
        List<AbstractAction> retValue;
        if (!trashedCard) {
            retValue = state.getDeck(DominionConstants.DeckType.HAND, player).stream()
                    .filter(DominionCard::isTreasureCard)
                    .map(c -> new TrashCard(c.cardType(), player))
                    .distinct().collect(toList());
        } else if (!gainedCard) {
            retValue = state.cardsToBuy().stream()
                    .filter(c -> c.isTreasure && c.cost <= trashValue + BONUS_OVER_TRASHED_VALUE)
                    .map(c -> new GainCard(c, player, DominionConstants.DeckType.HAND))
                    .collect(toList());
        } else {
            throw new AssertionError("Should not be here if we have already both trashed and gained a card");
        }
        if (retValue.isEmpty()) {
            retValue.add(new DoNothing());
        }
        return retValue;
    }

    @Override
    public int getCurrentPlayer(DominionGameState state) {
        return player;
    }

    @Override
    public void registerActionTaken(DominionGameState state, AbstractAction action) {
        if (!trashedCard && action instanceof TrashCard && ((TrashCard) action).player == player) {
            trashedCard = true;
            trashValue = ((TrashCard) action).trashedCard.cost;
        }
        if (!gainedCard && action instanceof GainCard && ((GainCard) action).buyingPlayer == player) {
            gainedCard = true;
        }
    }

    @Override
    public boolean executionComplete(DominionGameState state) {
        return trashedCard && gainedCard;
    }

    @Override
    public Mine copy() {
       Mine retValue = new Mine(player);
       retValue.gainedCard = gainedCard;
       retValue.trashedCard = trashedCard;
       retValue.trashValue = trashValue;
       return retValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Mine) {
            Mine other = (Mine) obj;
            return other.player == player
                    && other.trashValue == trashValue
                    && other.trashedCard == trashedCard
                    && other.gainedCard == gainedCard;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, trashedCard, trashValue, gainedCard, CardType.MINE);
    }
}
