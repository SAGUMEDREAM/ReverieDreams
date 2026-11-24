package cc.thonly.reverie_dreams.entity.npc;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LabelTarget;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies the actual tick control flow without constructing a world or a fake NPC. */
class NPCPoseLifecycleTest {
    @Test void anAwakeNPCCannotEnterTheVanillaWakeUpMethodEveryDaylightTick() throws Exception {
        var tick = tickCode(NPCSimpleEntity.class);
        int wake = invocation(tick, "stopSleeping", tick.size());
        assertTrue(wake >= 0);
        assertGuardSkips(tick, "isSleeping", Opcode.IFEQ, wake);
        assertGuardSkips(tick, "isClientSide", Opcode.IFNE, wake);
        assertGuardSkips(tick, "isBrightOutside", Opcode.IFEQ, wake);
    }

    @Test void clientTicksNeverOverrideTheServerSynchronizedPose() throws Exception {
        var tick = tickCode(BaseNPCLikeEntity.class);
        int poseChanges = 0;
        for (int i = 0; i < tick.size(); i++) {
            if (tick.get(i) instanceof InvokeInstruction call && call.name().equalsString("setPose")) {
                assertGuardSkips(tick, "isClientSide", Opcode.IFNE, i);
                poseChanges++;
            }
        }
        assertEquals(2, poseChanges, "Both entering and leaving crouch must be server-authoritative");
    }

    private static List<CodeElement> tickCode(Class<?> type) throws Exception {
        try (InputStream input = type.getResourceAsStream(type.getSimpleName() + ".class")) {
            assertNotNull(input);
            var model = ClassFile.of().parse(input.readAllBytes());
            return model.methods().stream().filter(method -> method.methodName().equalsString("tick")
                    && method.methodType().equalsString("()V")).findFirst().orElseThrow()
                    .code().orElseThrow().elementList();
        }
    }

    private static int invocation(List<CodeElement> code, String name, int before) {
        for (int i = before - 1; i >= 0; i--) {
            if (code.get(i) instanceof InvokeInstruction call && call.name().equalsString(name)) return i;
        }
        return -1;
    }

    private static void assertGuardSkips(List<CodeElement> code, String guardName, Opcode skipOpcode, int protectedCall) {
        int guard = invocation(code, guardName, protectedCall);
        assertTrue(guard >= 0, "Missing " + guardName + " guard");
        int next = guard + 1;
        while (!(code.get(next) instanceof Instruction)) next++;
        assertInstanceOf(BranchInstruction.class, code.get(next));
        var branch = (BranchInstruction) code.get(next);
        assertEquals(skipOpcode, branch.opcode(), guardName);
        int destination = -1;
        for (int i = 0; i < code.size(); i++) {
            if (code.get(i) instanceof LabelTarget target && target.label().equals(branch.target())) {
                destination = i;
                break;
            }
        }
        assertTrue(destination > protectedCall, guardName + " must skip the pose mutation");
    }
}
