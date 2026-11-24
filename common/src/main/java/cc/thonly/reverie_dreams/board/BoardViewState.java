package cc.thonly.reverie_dreams.board;

/**
 * Transient choices belong to a viewer, never to a saved match.
 */
class BoardViewState {
    int selected = -1, promotionFrom = -1, promotionTo = -1;
    private long resignRevision = -1;

    void select(int square) {
        selected = selected == square ? -1 : square;
        promotionFrom = promotionTo = -1;
        cancelResign();
    }

    void reset() {
        selected = promotionFrom = promotionTo = -1;
        cancelResign();
    }

    void requestResign(long revision) {
        reset();
        resignRevision = revision;
    }

    boolean resignPending(long revision) {
        return resignRevision >= 0 && resignRevision == revision;
    }

    boolean confirmResign(long revision) {
        boolean pending = resignPending(revision);
        cancelResign();
        return pending;
    }

    void cancelResign() {
        resignRevision = -1;
    }
}
