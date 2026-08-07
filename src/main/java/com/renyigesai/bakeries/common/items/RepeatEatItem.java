package com.renyigesai.bakeries.common.items;

import com.renyigesai.bakeries.api.items.PileItem;
import com.renyigesai.bakeries.common.init.BakeriesDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class RepeatEatItem extends PileItem {
    private final boolean canDrink;

    public RepeatEatItem(Block block, Properties properties, int eatCount,boolean canDrink) {
        super(block, properties.component(BakeriesDataComponents.EAT_COUNT_MAX,eatCount).component(BakeriesDataComponents.EAT_COUNT,eatCount));
        this.canDrink = canDrink;
    }

    public RepeatEatItem(Block block, Properties properties,PileProperties pileProperties,int eatCount, boolean canDrink) {
        super(block, pileProperties.itemProperties(properties.component(BakeriesDataComponents.EAT_COUNT_MAX,eatCount).component(BakeriesDataComponents.EAT_COUNT,eatCount)));
//        super(block, pileProperties);
        this.canDrink = canDrink;
    }

    public RepeatEatItem(Block block, Properties pProperties, boolean canDrink) {
        super(block, pProperties);
        this.canDrink = canDrink;
    }

    @Override
    public boolean isExtra(UseOnContext pContext) {
        ItemStack itemInHand = pContext.getItemInHand();
        if (isRepeatEat(itemInHand)){
            int eatCount = itemInHand.getOrDefault(BakeriesDataComponents.EAT_COUNT,-1);
            int eatCountMax = itemInHand.getOrDefault(BakeriesDataComponents.EAT_COUNT_MAX,-1);
            return eatCount == eatCountMax;
        }
        return super.isExtra(pContext);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pStack) {
        if (!this.canDrink){
            return ItemUseAnimation.EAT;
        }
        return ItemUseAnimation.DRINK;
    }

    public static boolean isRepeatEat(ItemStack stack){
        return stack.has(BakeriesDataComponents.EAT_COUNT_MAX) && stack.has(BakeriesDataComponents.EAT_COUNT);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return this.canDrink ? 5592575 : 15574564;
    }

    public ItemStack onConsume(Level level,LivingEntity living,ItemStack stack){
        if (isRepeatEat(stack)){
            int eatCount = stack.getOrDefault(BakeriesDataComponents.EAT_COUNT,-1);
            ItemStack cache = ItemStack.EMPTY;
            eat(level, living, stack);
            if (eatCount - 1 == 0){
                cache = stack.copy();
                stack.consume(1,living);
            }else {
                stack.set(BakeriesDataComponents.EAT_COUNT,eatCount - 1);
            }
            return (eatCount - 1 == 0 && cache.getCraftingRemainder() != null) ? cache.getCraftingRemainder().create() : stack;
        }
        return stack;
    }
    public void eat(Level level,LivingEntity living,ItemStack stack){

    }

    public int getBarWidth(ItemStack stack) {
        if (isRepeatEat(stack)) {
            Integer eatCount = stack.get(BakeriesDataComponents.EAT_COUNT);
            Integer eatCountMax = stack.get(BakeriesDataComponents.EAT_COUNT_MAX);
            if (eatCount != null && eatCountMax != null && eatCountMax > 0) {
                return Mth.clamp(Math.round(13.0F * eatCount / eatCountMax), 0, 13);
            }
        }
        return super.getBarWidth(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (!isRepeatEat(stack)){
            return false;
        }
        Integer eatCount = stack.get(BakeriesDataComponents.EAT_COUNT);
        Integer eatCountMax = stack.get(BakeriesDataComponents.EAT_COUNT_MAX);
        if (eatCount != null &&  eatCountMax != null){
            return eatCount < eatCountMax;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
        if (isRepeatEat(stack)){
            int eatCount = stack.getOrDefault(BakeriesDataComponents.EAT_COUNT,-1);
            int eatCountMax = stack.getOrDefault(BakeriesDataComponents.EAT_COUNT_MAX,-1);
            String translatable = this.canDrink ? "tooltips.bakeries.repeat_eat_item_drink" : "tooltips.bakeries.repeat_eat_item_eat";
            builder.accept(Component.literal(Component.translatable(translatable).getString() + eatCount + " / " + eatCountMax));
        }

    }

}
