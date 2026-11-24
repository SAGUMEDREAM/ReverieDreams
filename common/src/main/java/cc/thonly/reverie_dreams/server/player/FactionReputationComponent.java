
package cc.thonly.reverie_dreams.server.player;

import cc.thonly.reverie_dreams.api.player.PlayerComponent;
import cc.thonly.reverie_dreams.api.player.PlayerComponentInitializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.world.entity.player.Player;

@Getter
@ToString
public class FactionReputationComponent implements PlayerComponent<FactionReputationComponent> {
    public static final int MAX_VALUE = 200;
    public static final int MIN_VALUE = -100;
    public static final int DEFAULT_VALUE = 0;
    public static final int YOUKAI_FEEDS_PER_DAY = 4;

    private static final Codec<Integer> REPUTATION_CODEC = Codec.intRange(MIN_VALUE, MAX_VALUE);

    public static final Codec<FactionReputationComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            REPUTATION_CODEC.fieldOf("human").forGetter(FactionReputationComponent::getHuman),
            REPUTATION_CODEC.fieldOf("fairy").forGetter(FactionReputationComponent::getFairy),
            REPUTATION_CODEC.fieldOf("yokai").forGetter(FactionReputationComponent::getYokai),
            REPUTATION_CODEC.fieldOf("deity").forGetter(FactionReputationComponent::getDeity),
            REPUTATION_CODEC.fieldOf("oni").forGetter(FactionReputationComponent::getOni),
            REPUTATION_CODEC.fieldOf("immortal").forGetter(FactionReputationComponent::getImmortal),
            REPUTATION_CODEC.fieldOf("celestial").forGetter(FactionReputationComponent::getCelestial),
            REPUTATION_CODEC.fieldOf("lunarian").forGetter(FactionReputationComponent::getLunarian),
            REPUTATION_CODEC.optionalFieldOf("house_of_eternity", DEFAULT_VALUE).forGetter(FactionReputationComponent::getHouseOfEternity),
            Codec.LONG.optionalFieldOf("youkai_feed_day", Long.MIN_VALUE).forGetter(FactionReputationComponent::getYoukaiFeedDay),
            Codec.intRange(0, YOUKAI_FEEDS_PER_DAY).optionalFieldOf("youkai_feeds", 0).forGetter(FactionReputationComponent::getYoukaiFeeds)
    ).apply(instance, FactionReputationComponent::new));

    private Player player;

    // 人类
    private int human = DEFAULT_VALUE;
    // 妖精
    private int fairy = DEFAULT_VALUE;
    // 妖怪
    private int yokai = DEFAULT_VALUE;
    // 神明
    private int deity = DEFAULT_VALUE;
    // 贵族
    private int oni = DEFAULT_VALUE;
    // 仙族
    private int immortal = DEFAULT_VALUE;
    // 天人
    private int celestial = DEFAULT_VALUE;
    // 月人
    private int lunarian = DEFAULT_VALUE;
    // 月人
    private int houseOfEternity = DEFAULT_VALUE;
    private long youkaiFeedDay = Long.MIN_VALUE;
    private int youkaiFeeds;

    public FactionReputationComponent(int human, int fairy, int yokai, int deity, int oni, int immortal,
                                     int celestial, int lunarian, int houseOfEternity, long youkaiFeedDay, int youkaiFeeds) {
        this(human, fairy, yokai, deity, oni, immortal, celestial, lunarian, houseOfEternity);
        this.youkaiFeedDay = youkaiFeedDay;
        this.youkaiFeeds = Math.clamp(youkaiFeeds, 0, YOUKAI_FEEDS_PER_DAY);
    }

    public FactionReputationComponent(
            int human,
            int fairy,
            int yokai,
            int deity,
            int oni,
            int immortal,
            int celestial,
            int lunarian,
            int houseOfEternity
    ) {
        this.human = validate(human);
        this.fairy = validate(fairy);
        this.yokai = validate(yokai);
        this.deity = validate(deity);
        this.oni = validate(oni);
        this.immortal = validate(immortal);
        this.celestial = validate(celestial);
        this.lunarian = validate(lunarian);
        this.houseOfEternity = validate(houseOfEternity);
    }

    private static int validate(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Reputation must be between " + MIN_VALUE + " and " + MAX_VALUE
                            + ", got: " + value
            );
        }
        return value;
    }

    public void setHuman(int human) {
        this.human = validate(human);
    }

    public void setFairy(int fairy) {
        this.fairy = validate(fairy);
    }

    public void setYokai(int yokai) {
        this.yokai = validate(yokai);
        this.markDirty();
    }

    public void addYokai(int amount) {
        this.setYokai((int) Math.clamp((long) this.yokai + amount, MIN_VALUE, MAX_VALUE));
    }

    public boolean canFeedYoukai(long day) {
        return day != this.youkaiFeedDay || this.youkaiFeeds < YOUKAI_FEEDS_PER_DAY;
    }

    public boolean acceptYoukaiFeeding(long day) {
        if (!this.canFeedYoukai(day)) return false;
        if (day != this.youkaiFeedDay) {
            this.youkaiFeedDay = day;
            this.youkaiFeeds = 0;
        }
        this.youkaiFeeds++;
        this.addYokai(5);
        return true;
    }

    public void setDeity(int deity) {
        this.deity = validate(deity);
    }

    public void setOni(int oni) {
        this.oni = validate(oni);
    }

    public void setImmortal(int immortal) {
        this.immortal = validate(immortal);
    }

    public void setCelestial(int celestial) {
        this.celestial = validate(celestial);
    }

    public void setLunarian(int lunarian) {
        this.lunarian = validate(lunarian);
    }

    public void setHouseOfEternity(int reputation) {
        this.houseOfEternity = validate(reputation);
        this.markDirty();
    }

    public void addHouseOfEternity(int amount) {
        this.setHouseOfEternity((int)Math.clamp((long)this.houseOfEternity + amount, MIN_VALUE, MAX_VALUE));
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public Codec<FactionReputationComponent> getCodec() {
        return CODEC;
    }

    public static class FactionReputationComponentInitializer
            implements PlayerComponentInitializer<FactionReputationComponent> {

        @Override
        public PlayerComponent<FactionReputationComponent> create() {
            return new FactionReputationComponent(
                    DEFAULT_VALUE,
                    DEFAULT_VALUE,
                    DEFAULT_VALUE,
                    DEFAULT_VALUE,
                    DEFAULT_VALUE,
                    DEFAULT_VALUE,
                    DEFAULT_VALUE,
                    DEFAULT_VALUE,
                    DEFAULT_VALUE
            );
        }

        @Override
        public Codec<FactionReputationComponent> getCodec() {
            return CODEC;
        }
    }
}
