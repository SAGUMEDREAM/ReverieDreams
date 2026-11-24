package cc.thonly.reverie_dreams.board;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static cc.thonly.reverie_dreams.board.BoardPosition.*;

class BoardRulesTest {
    private BoardPosition setup(BoardKind kind,int side,int rights,int ep,int... entries) {
        int[] pieces=new int[kind.width*kind.height];
        for(int i=0;i<entries.length;i+=2)pieces[entries[i]]=entries[i+1];
        return BoardPosition.restore(new Saved(kind,pieces,side,rights,ep,0,0,0,-1,-1,Phase.PLAY,"",List.of(),new int[0]));
    }
    private long perft(BoardPosition p,int depth) {
        if(depth==0)return 1;long total=0;
        for(Move move:p.legalMoves()) {var next=p.play(move);assertNotNull(next);total+=perft(next,depth-1);}return total;
    }
    @Test void chessOpeningMatchesPublishedPerftCounts() {
        var chess=new BoardPosition(BoardKind.CHESS);
        assertEquals(20,chess.legalMoves().size());assertEquals(400,perft(chess,2));
        assertEquals(8902,perft(chess,3));
    }
    @Test void xiangqiOpeningAndHorseLeg() {
        var p=new BoardPosition(BoardKind.XIANGQI);assertEquals(44,p.legalMoves().size());
        assertFalse(p.legalMoves().contains(new Move(82,75))); // Its adjacent elephant blocks the horizontal leg.
        assertTrue(p.legalMoves().contains(new Move(82,65)));
    }
    @Test void pinnedPieceAndFlyingGeneralsCannotExposeKing() {
        var p=setup(BoardKind.CHESS,1,0,-1,60,KING,52,ROOK,4,-ROOK,0,-KING);
        assertFalse(p.legalMoves().contains(new Move(52,51)));
        var x=setup(BoardKind.XIANGQI,1,0,-1,85,KING,4,-KING,49,ROOK);
        assertFalse(x.inCheck(1));assertFalse(x.legalMoves().contains(new Move(49,48)));
    }
    @Test void castleRequiresRookClearPathAndSafeTransit() {
        var p=setup(BoardKind.CHESS,1,3,-1,60,KING,56,ROOK,63,ROOK,4,-KING);
        assertTrue(p.legalMoves().contains(new Move(60,62)));assertTrue(p.legalMoves().contains(new Move(60,58)));
        var castled=p.play(new Move(60,62));assertEquals(KING,castled.at(62));assertEquals(ROOK,castled.at(61));assertEquals(0,castled.at(63));
        var attacked=setup(BoardKind.CHESS,1,1,-1,60,KING,63,ROOK,4,-KING,5,-ROOK);
        assertFalse(attacked.legalMoves().contains(new Move(60,62)));
    }
    @Test void enPassantRemovesPawnAndCannotExposeHorizontalCheck() {
        var p=setup(BoardKind.CHESS,1,0,19,60,KING,4,-KING,28,PAWN,27,-PAWN);
        var next=p.play(new Move(28,19));assertNotNull(next);assertEquals(0,next.at(27));assertEquals(PAWN,next.at(19));
        var pinned=setup(BoardKind.CHESS,1,0,21,31,KING,4,-KING,30,PAWN,29,-PAWN,24,-ROOK);
        assertFalse(pinned.legalMoves().contains(new Move(30,21)));
    }
    @Test void promotionHasAllFourChoices() {
        var p=setup(BoardKind.CHESS,1,0,-1,63,KING,7,-KING,8,PAWN);
        assertEquals(4,p.legalMoves().stream().filter(m->m.from()==8&&m.to()==0).count());
        assertEquals(KNIGHT,p.play(new Move(8,0,KNIGHT)).at(0));
    }
    @Test void mateAndStalemateAreDifferent() {
        var p=new BoardPosition(BoardKind.CHESS);
        for(Move m:List.of(new Move(53,45),new Move(12,28),new Move(54,38),new Move(3,39))) {p=p.play(m);assertNotNull(p);}
        assertEquals(Phase.FINISHED,p.phase());assertEquals("second",p.result());
        var stalemate=setup(BoardKind.CHESS,1,0,-1,0,-KING,18,KING,26,QUEEN);
        var next=stalemate.play(new Move(26,17));assertNotNull(next);assertEquals("draw",next.result());
    }
    @Test void cannonNeedsExactlyOneScreenAndElephantCannotCrossRiver() {
        var p=setup(BoardKind.XIANGQI,1,0,-1,85,KING,3,-KING,67,CANNON,49,PAWN,22,-ROOK,47,BISHOP);
        assertTrue(p.legalMoves().contains(new Move(67,22)));
        assertFalse(p.legalMoves().contains(new Move(67,49)));
        assertFalse(p.legalMoves().contains(new Move(47,27)));
    }
    @Test void goCaptureSuicideAndSuperko() {
        var p=setup(BoardKind.GO,1,0,-1,10,-1,1,1,9,1,11,1);
        var captured=p.play(new Move(-1,19));assertNotNull(captured);assertEquals(0,captured.at(10));
        assertNull(setup(BoardKind.GO,-1,0,-1,1,1,9,1,11,1,19,1).play(new Move(-1,10)));
        var ko=setup(BoardKind.GO,1,0,-1,10,-1,1,1,9,1,11,1,18,-1,20,-1,28,-1);
        var saved=ko.save();
        ko=BoardPosition.restore(new Saved(saved.kind(),saved.pieces(),1,0,-1,0,0,0,-1,-1,Phase.PLAY,"",List.of(Arrays.toString(saved.pieces())),new int[0]));
        var take=ko.play(new Move(-1,19));assertNotNull(take);assertNull(take.play(new Move(-1,10)));
    }
    @Test void goDoublePassDeadGroupAndResume() {
        var p=setup(BoardKind.GO,1,0,-1,0,1,1,1,80,-1);
        p=p.play(Move.pass()).play(Move.pass());assertEquals(Phase.SCORING,p.phase());
        assertTrue(p.toggleDead(0));assertTrue(p.dead(1));
        double[] score=p.score();assertEquals(0,score[0]);assertEquals(88.5,score[1]);
        p.resumeGo();assertEquals(Phase.PLAY,p.phase());assertFalse(p.dead(0));
    }
    @Test void matchSeatsReadinessPersistenceAndUndo() {
        var game=new BoardGame(BoardKind.CHESS);UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        assertTrue(game.join(a,"A",0));assertFalse(game.join(a,"A",1));assertTrue(game.join(b,"B",1));
        assertFalse(game.move(1,new Move(52,36)));game.ready(a);game.ready(b);assertTrue(game.started);
        assertFalse(game.move(-1,new Move(12,28)));assertTrue(game.move(1,new Move(52,36)));
        var restored=BoardGame.decode(game.encode());assertEquals(a,restored.players[0]);assertEquals(PAWN,restored.position.at(36));
        assertEquals(game.position.save().repetitions(),restored.position.save().repetitions());
        assertTrue(restored.undo());assertEquals(PAWN,restored.position.at(52));assertEquals(0,restored.position.at(36));
    }
    @Test void undoAfterDisputedGoScoreCanReplayResumedGame() {
        var game=new BoardGame(BoardKind.GO);UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        game.join(a,"A",0);game.join(b,"B",1);game.ready(a);game.ready(b);
        assertTrue(game.move(1,new Move(-1,40)));assertTrue(game.move(-1,new Move(-1,41)));
        assertTrue(game.move(1,Move.pass()));assertTrue(game.move(-1,Move.pass()));
        game.position.resumeGo();assertTrue(game.move(1,new Move(-1,42)));assertTrue(game.move(-1,new Move(-1,43)));
        assertTrue(game.undo());assertEquals(Phase.PLAY,game.position.phase());assertEquals(0,game.position.at(43));
        assertTrue(game.undo());assertEquals(Phase.PLAY,game.position.phase());assertEquals(0,game.position.at(42));
        assertTrue(game.move(1,new Move(-1,44)));
    }
}
