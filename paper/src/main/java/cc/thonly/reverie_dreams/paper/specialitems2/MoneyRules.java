package cc.thonly.reverie_dreams.paper.specialitems2;

import cc.thonly.reverie_dreams.paper.util.Ids;

import java.util.OptionalInt;

/** Pure source ItemUtils.PRICE accounting, separate from Faith prayer's1/3/8 values. */
public final class MoneyRules {
    public record Coins(int gold,int silver,int copper){public long value(){return gold*100L+silver*10L+copper;}}
    public static int denomination(String id){return switch(id){case Ids.MOD_ID + ":copper_coin"->1;case Ids.MOD_ID + ":silver_coin"->10;case Ids.MOD_ID + ":gold_coin"->100;default->0;};}
    public static Coins calculate(int price){return price<=0?new Coins(0,0,0):new Coins(price/100,price%100/10,price%10);}
    public static OptionalInt add(int current,long cash){try{long value=Math.addExact(current,cash);return cash<0||cash>Integer.MAX_VALUE||value>Integer.MAX_VALUE||value<Integer.MIN_VALUE?OptionalInt.empty():OptionalInt.of((int)value);}catch(ArithmeticException overflow){return OptionalInt.empty();}}
    private MoneyRules(){}
}
