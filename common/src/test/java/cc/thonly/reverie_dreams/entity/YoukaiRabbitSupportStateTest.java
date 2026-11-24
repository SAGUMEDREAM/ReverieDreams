package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitSupportState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YoukaiRabbitSupportStateTest {
    @Test void groupNeverSummonsAndDoesNotConsumeALaterLoneCall() {
        var state = new YoukaiRabbitSupportState();
        assertFalse(state.claimCall(1, 100)); assertFalse(state.claimCall(3, 100));
        assertTrue(state.claimCall(0, 100));
    }
    @Test void repeatedDamageAndSaveReloadCannotResetCallCooldown() {
        var state = new YoukaiRabbitSupportState();
        assertTrue(state.claimCall(0, 100));
        assertFalse(state.claimCall(0, 101));
        var saved = new YoukaiRabbitSupportState(); saved.restore(state.nextCall(), state.isReinforcement(), state.expires());
        assertFalse(saved.claimCall(0, 6099)); assertTrue(saved.claimCall(0, 6100));
    }
    @Test void summonedAlliesNeverRecruitEvenAfterCooldownAndAcrossReload() {
        var state = new YoukaiRabbitSupportState(); state.markReinforcement(100);
        var restored = new YoukaiRabbitSupportState(); restored.restore(state.nextCall(), state.isReinforcement(), state.expires());
        assertFalse(restored.claimCall(0, 101)); assertFalse(restored.claimCall(0, 10000));
        assertFalse(restored.expired(1299)); assertTrue(restored.expired(1300));
    }
    @Test void ordinaryResidentsNeverExpire() {
        var state = new YoukaiRabbitSupportState();
        assertFalse(state.expired(Long.MAX_VALUE));
    }
}
