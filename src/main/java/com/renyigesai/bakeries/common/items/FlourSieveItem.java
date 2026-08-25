package com.renyigesai.bakeries.common.items;


import com.renyigesai.bakeries.common.init.BakeriesRecipes;
import com.renyigesai.bakeries.common.recipe.FlourSieveRecipe;
import com.renyigesai.bakeries.common.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class FlourSieveItem extends Item {


    public FlourSieveItem(Identifier identifier) {
        super(new Item.Properties().durability(250).enchantable(14).setId(ResourceKey.create(Registries.ITEM, identifier)));
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack mainHandItem = pPlayer.getMainHandItem();
        if (mainHandItem.is(this)){
            return super.use(pLevel, pPlayer, pUsedHand);
        }
        if (pLevel instanceof ServerLevel serverLevel) {
            Optional<RecipeHolder<FlourSieveRecipe>> recipeFor = getFlourSieveRecipe(serverLevel,mainHandItem);
            if (recipeFor.isEmpty()) {
                pPlayer.getCooldowns().addCooldown(this.getDefaultInstance(), 20);
                pPlayer.sendOverlayMessage(Component.translatable(getFlourSieveRandomText()));
                return InteractionResult.FAIL;
            }
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        Player player = (Player)pLivingEntity;
        ItemStack mainHandItem = player.getMainHandItem();

        if (pLevel instanceof ServerLevel serverLevel) {
            Optional<RecipeHolder<FlourSieveRecipe>> currentRecipe = getFlourSieveRecipe(serverLevel, mainHandItem);
            if (currentRecipe.isPresent()) {
                SingleRecipeInput singleRecipeInput = new SingleRecipeInput(player.getMainHandItem());
                ItemStack resultItemStack = currentRecipe.get().value().assemble(singleRecipeInput);
                if (!player.getAbilities().instabuild) {
                    mainHandItem.shrink(1);
                    pStack.hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
                }
                ItemUtils.givePlayerItem(player, resultItemStack);
            }
        }
        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }

    private String getFlourSieveRandomText(){
        return "tooltips.bakeries.flour_sieve_" + random(3,1);
    }

    private Integer random(int max, int min) {
        Random rand = new Random();
        int value = 0;
        for (int i = 0; i < max; i++) {
            value = rand.nextInt(max - min + 1) + min;
        }
        return value;
    }

    private Optional<RecipeHolder<FlourSieveRecipe>> getFlourSieveRecipe(ServerLevel level, ItemStack stack) {
        if (level == null) {
            return Optional.empty();
        }
        return RecipeManager.createCheck(BakeriesRecipes.FLOUR_SIEVE_TYPE.get()).getRecipeFor(new SingleRecipeInput(stack),level);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pStack) {
        return ItemUseAnimation.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 16;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltips.bakeries.flour_sieve_0").withStyle(ChatFormatting.BLUE));
    }

}
