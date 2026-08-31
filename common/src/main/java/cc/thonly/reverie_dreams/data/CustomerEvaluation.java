package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.entity.npc.container.NPCCustomerContainer;
import cc.thonly.reverie_dreams.registry.content.CustomerDefaultEvaluationConsts;
import cc.thonly.reverie_dreams.util.math.ModMth;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

@SuppressWarnings("SpellCheckingInspection")
public record CustomerEvaluation(
        Optional<String> exbad,
        Optional<String> bad,
        Optional<String> norm,
        Optional<String> good,
        Optional<String> exgood,
        Optional<String> lackmoneyangry,
        Optional<String> lackmoneynormal,
        Optional<String> repell,
        Optional<String> seenRepell

) {
    public static final Codec<CustomerEvaluation> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            Codec.STRING.optionalFieldOf("exbad").forGetter(CustomerEvaluation::exbad),
                            Codec.STRING.optionalFieldOf("bad").forGetter(CustomerEvaluation::bad),
                            Codec.STRING.optionalFieldOf("norm").forGetter(CustomerEvaluation::norm),
                            Codec.STRING.optionalFieldOf("good").forGetter(CustomerEvaluation::good),
                            Codec.STRING.optionalFieldOf("exgood").forGetter(CustomerEvaluation::exgood),
                            Codec.STRING.optionalFieldOf("lackmoneyangry").forGetter(CustomerEvaluation::lackmoneyangry),
                            Codec.STRING.optionalFieldOf("lackmoneynormal").forGetter(CustomerEvaluation::lackmoneynormal),
                            Codec.STRING.optionalFieldOf("repell").forGetter(CustomerEvaluation::repell),
                            Codec.STRING.optionalFieldOf("seenRepell").forGetter(CustomerEvaluation::seenRepell)
                    ).apply(instance, CustomerEvaluation::new)
            );
    public static final CustomerEvaluation DEFAULT_1 =
            new CustomerEvaluation(
                    CustomerDefaultEvaluationConsts.DEFAULT_1_EXBAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_1_BAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_1_NORM,
                    CustomerDefaultEvaluationConsts.DEFAULT_1_GOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_1_EXGOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_1_LACK_MONEY_ANGRY,
                    CustomerDefaultEvaluationConsts.DEFAULT_1_LACK_MONEY_NORMAL,
                    CustomerDefaultEvaluationConsts.DEFAULT_1_REPELL,
                    CustomerDefaultEvaluationConsts.DEFAULT_1_SEEN_REPELL
            );

    public static final CustomerEvaluation DEFAULT_2 =
            new CustomerEvaluation(
                    CustomerDefaultEvaluationConsts.DEFAULT_2_EXBAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_2_BAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_2_NORM,
                    CustomerDefaultEvaluationConsts.DEFAULT_2_GOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_2_EXGOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_2_LACK_MONEY_ANGRY,
                    CustomerDefaultEvaluationConsts.DEFAULT_2_LACK_MONEY_NORMAL,
                    CustomerDefaultEvaluationConsts.DEFAULT_2_REPELL,
                    CustomerDefaultEvaluationConsts.DEFAULT_2_SEEN_REPELL
            );

    public static final CustomerEvaluation DEFAULT_3 =
            new CustomerEvaluation(
                    CustomerDefaultEvaluationConsts.DEFAULT_3_EXBAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_3_BAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_3_NORM,
                    CustomerDefaultEvaluationConsts.DEFAULT_3_GOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_3_EXGOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_3_LACK_MONEY_ANGRY,
                    CustomerDefaultEvaluationConsts.DEFAULT_3_LACK_MONEY_NORMAL,
                    CustomerDefaultEvaluationConsts.DEFAULT_3_REPELL,
                    CustomerDefaultEvaluationConsts.DEFAULT_3_SEEN_REPELL
            );

    public static final CustomerEvaluation DEFAULT_4 =
            new CustomerEvaluation(
                    CustomerDefaultEvaluationConsts.DEFAULT_4_EXBAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_4_BAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_4_NORM,
                    CustomerDefaultEvaluationConsts.DEFAULT_4_GOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_4_EXGOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_4_LACK_MONEY_ANGRY,
                    CustomerDefaultEvaluationConsts.DEFAULT_4_LACK_MONEY_NORMAL,
                    CustomerDefaultEvaluationConsts.DEFAULT_4_REPELL,
                    CustomerDefaultEvaluationConsts.DEFAULT_4_SEEN_REPELL
            );

    public static final CustomerEvaluation DEFAULT_5 =
            new CustomerEvaluation(
                    CustomerDefaultEvaluationConsts.DEFAULT_5_EXBAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_5_BAD,
                    CustomerDefaultEvaluationConsts.DEFAULT_5_NORM,
                    CustomerDefaultEvaluationConsts.DEFAULT_5_GOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_5_EXGOOD,
                    CustomerDefaultEvaluationConsts.DEFAULT_5_LACK_MONEY_ANGRY,
                    CustomerDefaultEvaluationConsts.DEFAULT_5_LACK_MONEY_NORMAL,
                    CustomerDefaultEvaluationConsts.DEFAULT_5_REPELL,
                    CustomerDefaultEvaluationConsts.DEFAULT_5_SEEN_REPELL
            );

    public static final CustomerEvaluation[] DEFAULTS = {
            DEFAULT_1,
            DEFAULT_2,
            DEFAULT_3,
            DEFAULT_4,
            DEFAULT_5
    };

    public CustomerEvaluation() {
        this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public CustomerEvaluation(@Nullable String exbad,
                              @Nullable String bad,
                              @Nullable String norm,
                              @Nullable String good,
                              @Nullable String exgood,
                              @Nullable String lackmoneyangry,
                              @Nullable String lackmoneynormal,
                              @Nullable String repell,
                              @Nullable String seenRepell) {
        this(Optional.ofNullable(exbad),
                Optional.ofNullable(bad),
                Optional.ofNullable(norm),
                Optional.ofNullable(good),
                Optional.ofNullable(exgood),
                Optional.ofNullable(lackmoneyangry),
                Optional.ofNullable(lackmoneynormal),
                Optional.ofNullable(repell),
                Optional.ofNullable(seenRepell));
    }

    public static CustomerEvaluation getRandomEvaluation(RandomSource randomSource) {
        return ModMth.getRandomElement(randomSource, Arrays.asList(DEFAULTS));
    }

    public Optional<String> getEvaluationFeedback(
            int score,
            int overBudget
    ) {
        boolean tooExpensive = overBudget > 0;

        // 超预算
        if (tooExpensive) {
            if (score < 0) {
                return this.lackmoneyangry;
            }

            return this.lackmoneynormal;
        }

        // 防止传入超过范围的 score
        score = Math.max(
                NPCCustomerContainer.MIN_SCORE,
                Math.min(score, NPCCustomerContainer.MAX_SCORE)
        );

        if (score <= -6) {
            return this.exbad;
        }

        if (score <= -1) {
            return this.bad;
        }

        if (score <= 2) {
            return this.norm;
        }

        if (score <= 7) {
            return this.good;
        }

        return this.exgood;
    }
}
