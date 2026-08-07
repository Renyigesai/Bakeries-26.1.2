package com.renyigesai.bakeries.common.items;

import com.renyigesai.bakeries.BakeriesMod;
import com.renyigesai.bakeries.common.init.BakeriesMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class DrinkItem extends RepeatEatItem{
    private final int upEffect;

    public DrinkItem(Block block, Properties properties, int upEffect) {
        super(block, properties,new PileProperties().placeSound(SoundEvents.GLASS_PLACE), 6, true);
        this.upEffect = upEffect;
    }

    @Override
    public void eat(Level level, LivingEntity living, ItemStack stack) {
        super.eat(level, living, stack);
        if (this.upEffect > 0){
            if (!level.isClientSide()) {
                try {
                    if (living.hasEffect(BakeriesMobEffects.ENJOY) && living.getEffect(BakeriesMobEffects.ENJOY).getAmplifier() < this.upEffect) {
                        int amplifier = living.getEffect(BakeriesMobEffects.ENJOY).getAmplifier();
                        living.addEffect(new MobEffectInstance(BakeriesMobEffects.ENJOY, living.getEffect(BakeriesMobEffects.ENJOY).getDuration() + 200, amplifier + 1), living);
                    }
                }catch (NullPointerException e){
                    BakeriesMod.LOGGER.error("DrinkItem eat {}", e);
                }

            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
        if (this.upEffect > 0) {
            builder.accept(Component.translatable("tooltips.bakeries.drink", Component.translatable("potion.potency." + this.upEffect)).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
