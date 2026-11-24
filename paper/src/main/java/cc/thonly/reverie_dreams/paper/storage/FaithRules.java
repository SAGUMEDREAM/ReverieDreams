package cc.thonly.reverie_dreams.paper.storage;

import java.util.random.RandomGenerator;

/** Pure Java source FaithComponent and CashBox arithmetic, including its deliberately preserved setter edge. */
public final class FaithRules {
    public static final int MAX_VALUE=200;
    public record State(int value,long lastPrayerDay){public static final State EMPTY=new State(0,-1);}
    public enum Status { SUCCESS, FULL, ALREADY_PRAYED, NOT_A_COIN }
    public record Prayer(Status status,State state,int displayedValue,int requestedCoins){}
    private FaithRules(){}
    /** Source rejects values over200, rather than clamping; negative source values are accepted. */
    public static State setValue(State state,int value){return value>MAX_VALUE?state:new State(value,state.lastPrayerDay);}
    public static Prayer pray(State state,int coinValue,long gameTime,RandomGenerator random){
        if(coinValue<=0)return new Prayer(Status.NOT_A_COIN,state,state.value,0);
        long today=gameTime/24000;
        if(today==state.lastPrayerDay)return new Prayer(Status.ALREADY_PRAYED,state,state.value,0);
        int base=state.value+coinValue;
        int proposed=(int)(base+7*1.5f*random.nextDouble()); // The source consumes this RNG draw even on the FULL branch.
        if(base>MAX_VALUE)return new Prayer(Status.FULL,new State(MAX_VALUE,state.lastPrayerDay),MAX_VALUE,0);
        State updated=setValue(state,proposed);
        return new Prayer(Status.SUCCESS,new State(updated.value,today),proposed,random.nextInt(1,4));
    }
}
