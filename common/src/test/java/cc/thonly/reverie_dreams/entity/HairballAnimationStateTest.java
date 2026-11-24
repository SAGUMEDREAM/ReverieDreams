package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.HairballAnimationState;
import org.junit.jupiter.api.Test;
import static cc.thonly.reverie_dreams.entity.variant.HairballAnimationState.Clip.*;
import static org.junit.jupiter.api.Assertions.*;

class HairballAnimationStateTest {
    @Test void jumpFollowsRealVerticalMotionAndLandingOnlyRunsOnce() {
        var state = new HairballAnimationState();
        state.update(1, true, false, 0, 0, 0, false);
        assertEquals(IDLE, state.clip());
        state.update(2, false, false, .3, .02, 0, false);
        assertEquals(JUMP, state.clip());
        state.update(3, false, false, -.1, .02, 0, false);
        assertEquals(FALL, state.clip());
        state.update(4, true, false, 0, .02, 0, false);
        assertEquals(LAND, state.clip());
        state.update(9, true, false, 0, .02, 0, false);
        assertEquals(LAND, state.clip());
        state.update(10, true, false, 0, .02, 0, false);
        assertEquals(WALK, state.clip());
    }

    @Test void hurtInterruptsAttackWithoutResumingItAndNewHitsRestart() {
        var state = new HairballAnimationState();
        state.beginAttack(10);
        state.update(10, true, false, 0, 0, 0, false);
        assertEquals(ATTACK, state.clip());
        int beforeHit = state.revision();
        state.update(11, true, false, 0, 0, 10, false);
        assertEquals(HURT, state.clip());
        assertTrue(state.revision() > beforeHit);
        state.update(12, true, false, 0, 0, 9, false);
        int beforeSecondHit = state.revision();
        state.update(13, true, false, 0, 0, 10, false);
        assertTrue(state.revision() > beforeSecondHit);
        state.update(14, true, false, 0, 0, 0, false);
        assertEquals(IDLE, state.clip());
    }

    @Test void attackExpiresAndEveryFreshAttackHasARevision() {
        var state = new HairballAnimationState();
        state.beginAttack(20);
        state.update(27, true, false, 0, 0, 0, false);
        assertEquals(ATTACK, state.clip());
        state.update(28, true, false, 0, 0, 0, false);
        assertEquals(IDLE, state.clip());
        int previous = state.revision();
        state.beginAttack(29);
        assertTrue(state.revision() > previous);
    }

    @Test void attackDuringHurtDoesNotQueueALateLungeAfterRecovery() {
        var state = new HairballAnimationState();
        state.update(1, true, false, 0, 0, 10, false);
        state.update(2, true, false, 0, 0, 5, false);
        state.beginAttack(2);
        state.update(3, true, false, 0, 0, 0, false);
        assertEquals(IDLE, state.clip());
    }

    @Test void swimmingDoesNotMasqueradeAsJumpAndDeathCancelsActions() {
        var state = new HairballAnimationState();
        state.update(1, false, true, .2, .02, 0, false);
        assertEquals(WALK, state.clip());
        state.update(2, true, false, 0, 0, 0, false);
        assertEquals(IDLE, state.clip());
        state.beginAttack(3);
        state.update(3, false, false, .1, .02, 10, true);
        assertEquals(IDLE, state.clip());
    }

    @Test void singleTickGroundJitterDoesNotCreateRepeatedLandingAndSpeedIsBounded() {
        var state = new HairballAnimationState();
        state.update(1, false, false, 0, 0, 0, false);
        state.update(2, true, false, 0, 0, 0, false);
        assertEquals(IDLE, state.clip());
        assertEquals(.65, HairballAnimationState.walkSpeed(0));
        assertEquals(1, HairballAnimationState.walkSpeed(.09));
        assertEquals(1.65, HairballAnimationState.walkSpeed(5));
    }
}
