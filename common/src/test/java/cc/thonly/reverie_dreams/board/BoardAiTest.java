package cc.thonly.reverie_dreams.board;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static cc.thonly.reverie_dreams.board.BoardPosition.*;

class BoardAiTest {
    BoardPosition setup(BoardKind kind,int... entries) {
        int[] b=new int[kind.width*kind.height];for(int i=0;i<entries.length;i+=2)b[entries[i]]=entries[i+1];
        return restore(new Saved(kind,b,1,0,-1,0,0,0,-1,-1,Phase.PLAY,"",List.of(),new int[0]));
    }
    @Test void everyGameAndDifficultyReturnsLegalMoveWithoutChangingSnapshot() {
        for(var kind:BoardKind.values())for(var difficulty:BoardGame.Difficulty.values()) {
            var p=new BoardPosition(kind);String before=Arrays.toString(p.save().pieces());
            var choice=BoardAiSearch.choose(p,difficulty,42);assertNotNull(choice.move(),kind+" "+difficulty);
            assertTrue(p.legalMoves().contains(choice.move()));assertEquals(before,Arrays.toString(p.save().pieces()));
            assertTrue(choice.nodes()<=6001);assertTrue(choice.millis()<2500,"Search must remain bounded: "+choice);
        }
    }
    @Test void searchRecognizesImmediateMateInsteadOfTakingRandomPiece() {
        var p=setup(BoardKind.CHESS,7,-KING,21,KING,13,QUEEN);
        var move=BoardAiSearch.choose(p,BoardGame.Difficulty.HARD,5).move();
        var next=p.play(move);assertEquals(Phase.FINISHED,next.phase(),"Should choose mate: "+move);assertEquals("first",next.result());
    }
    @Test void normalChessWinsUndefendedQueen() {
        var p=setup(BoardKind.CHESS,60,KING,4,-KING,56,ROOK,0,-QUEEN);
        assertEquals(new Move(56,0),BoardAiSearch.choose(p,BoardGame.Difficulty.NORMAL,9).move());
    }
    @Test void goSearchPrioritizesImmediateCapture() {
        var p=setup(BoardKind.GO,10,-1,1,1,9,1,11,1);
        for(var level:List.of(BoardGame.Difficulty.NORMAL,BoardGame.Difficulty.HARD)) {
            var move=BoardAiSearch.choose(p,level,1).move();assertEquals(19,move.to());assertEquals(0,p.play(move).at(10));
        }
    }
    @Test void aiSeatIsExclusiveAndDifficultySurvivesSave() {
        var g=new BoardGame(BoardKind.CHESS);UUID player=UUID.randomUUID();assertTrue(g.join(player,"Player",1));
        assertTrue(g.configureAI(player,BoardGame.Difficulty.HARD));assertFalse(g.join(UUID.randomUUID(),"Other",0));g.ready(player);
        assertTrue(g.started);assertEquals(1,g.aiSide);var restored=BoardGame.decode(g.encode());assertEquals(BoardGame.Difficulty.HARD,restored.difficulty);
        assertTrue(restored.move(1,new Move(52,36)));assertTrue(restored.move(-1,new Move(12,28)));assertTrue(restored.undo());
        assertEquals(1,restored.position.ply());assertEquals(-1,restored.position.turn()); // Keep AI's opening when the human plays second.
        assertTrue(restored.move(-1,new Move(12,28)));assertTrue(restored.move(1,new Move(62,45)));
        assertTrue(restored.undo());assertEquals(1,restored.position.ply());assertEquals(-1,restored.position.turn());
    }
}
