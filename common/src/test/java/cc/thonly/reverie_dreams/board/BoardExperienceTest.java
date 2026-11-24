package cc.thonly.reverie_dreams.board;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class BoardExperienceTest {
    @Test void quickStartWorksForAllBoardsAndDifficulties() {
        UUID player=UUID.randomUUID();
        for(var kind:BoardKind.values())for(var difficulty:BoardGame.Difficulty.values()) {
            var game=new BoardGame(kind);
            assertTrue(game.startAgainstAI(player,"Player",difficulty));
            assertTrue(game.started);assertEquals(1,game.side(player));
            assertEquals(-1,game.aiSide);assertEquals(difficulty,game.difficulty);
            var restored=BoardGame.decode(game.encode());
            assertTrue(restored.started);assertEquals(player,restored.players[0]);
            assertEquals(difficulty,restored.difficulty);
        }
    }
    @Test void quickStartPreservesChosenSideAndAlreadyReadyPlayer() {
        UUID player=UUID.randomUUID();var game=new BoardGame(BoardKind.CHESS);
        assertTrue(game.join(player,"Player",1));game.ready(player);
        assertTrue(game.startAgainstAI(player,"Player",BoardGame.Difficulty.HARD));
        assertEquals(-1,game.side(player));assertEquals(1,game.aiSide);
        assertTrue(game.started);assertEquals(1,game.position.turn());
    }
    @Test void quickStartCannotHijackHumanLobbyOrRunningGame() {
        UUID first=UUID.randomUUID(),second=UUID.randomUUID(),stranger=UUID.randomUUID();
        var game=new BoardGame(BoardKind.GO);game.join(first,"First",0);
        String before=game.encode();
        assertFalse(game.startAgainstAI(stranger,"Stranger",BoardGame.Difficulty.EASY));
        assertEquals(before,game.encode());
        game.join(second,"Second",1);before=game.encode();
        assertFalse(game.startAgainstAI(first,"First",BoardGame.Difficulty.EASY));
        assertEquals(before,game.encode());
        game.ready(first);game.ready(second);before=game.encode();
        assertFalse(game.startAgainstAI(first,"First",BoardGame.Difficulty.HARD));
        assertEquals(before,game.encode());
        var empty=new BoardGame(BoardKind.GO);before=empty.encode();
        assertFalse(empty.startAgainstAI(first,"First",null));assertEquals(before,empty.encode());
    }
    @Test void aiRematchKeepsSideAndDifficultyButResetsMatch() {
        UUID player=UUID.randomUUID();var game=new BoardGame(BoardKind.CHESS);
        game.join(player,"Player",1);game.startAgainstAI(player,"Player",BoardGame.Difficulty.HARD);
        assertFalse(game.rematchAgainstAI(player));
        assertTrue(game.move(1,new BoardPosition.Move(52,36)));game.resign(-1);
        UUID oldId=game.id;String finished=game.encode();
        assertFalse(game.rematchAgainstAI(UUID.randomUUID()));assertEquals(finished,game.encode());
        assertTrue(game.rematchAgainstAI(player));assertNotEquals(oldId,game.id);
        assertEquals(-1,game.side(player));assertEquals(1,game.aiSide);assertEquals(BoardGame.Difficulty.HARD,game.difficulty);
        assertTrue(game.moves.isEmpty());assertEquals(0,game.position.ply());
        assertEquals(BoardPosition.Phase.PLAY,game.position.phase());assertTrue(game.started);
        var humans=new BoardGame(BoardKind.GO);humans.join(player,"Player",0);humans.position.finish("draw");
        assertFalse(humans.rematchAgainstAI(player));
    }
    @Test void resignationRequiresCurrentRequestAndCannotBeReplayed() {
        var view=new BoardViewState();
        assertFalse(view.confirmResign(7));
        view.requestResign(7);assertFalse(view.confirmResign(8));
        assertFalse(view.resignPending(7));
        view.requestResign(8);view.cancelResign();assertFalse(view.confirmResign(8));
        view.requestResign(9);assertTrue(view.confirmResign(9));assertFalse(view.confirmResign(9));
        view.requestResign(10);view.reset();assertFalse(view.confirmResign(10));
    }
    @Test void selectingAgainCancelsAndChangingPieceClearsPromotion() {
        var view=new BoardViewState();view.select(52);view.promotionFrom=52;view.promotionTo=4;
        view.select(51);assertEquals(51,view.selected);assertEquals(-1,view.promotionFrom);assertEquals(-1,view.promotionTo);
        view.select(51);assertEquals(-1,view.selected);
        view.requestResign(2);view.select(52);assertFalse(view.resignPending(2));
        view.reset();assertEquals(-1,view.selected);
    }
}
