package com.renyigesai.bakeries.common.items;

import com.renyigesai.bakeries.api.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Set;
import java.util.function.Consumer;

public class EternalBaguetteItem extends Item{
    public static final Set<ItemAbility> BAGUETTE_ACTIONS;

    public static final Identifier BASE_ATTACK_KNOCKBACK_ID = ResourceLocation.fromNamespaceAndPath("bakeries","base_attack_knockback");

    public EternalBaguetteItem(Identifier identifier) {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).enchantable(15).attributes(createAttributes()).setId(ResourceKey.create(Registries.ITEM,identifier)));
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_ID,7d,AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_ID,-2.5d,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_KNOCKBACK,new AttributeModifier(BASE_ATTACK_KNOCKBACK_ID,2d,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
        Vec3 vec3 = entity.getDeltaMovement();
        double s = 0.85;
        Vec3 vec31 = (new Vec3(Mth.sin(sourceentity.getYRot() * ((float)Math.PI / 180F)), 0.8D,(-Mth.cos(sourceentity.getYRot() * ((float)Math.PI / 180F))))).normalize().scale(s);
        entity.setDeltaMovement(vec3.x / 2.0D - vec31.x, entity.onGround() ? Math.min(0.4D, vec3.y / 2.0 + s) : vec3.y, vec3.z / 2.0D - vec31.z);
        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,50,2));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltips.bakeries.eternal_baguette").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.SWEEPING_EDGE) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.SWEEPING_EDGE) && super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return BAGUETTE_ACTIONS.contains(itemAbility);
    }

    @Override
    public int getEnchantmentLevel(ItemInstance stack, Holder<Enchantment> enchantment) {
        return 15;
    }

    static {
        BAGUETTE_ACTIONS = Set.of(ItemAbilities.SHEARS_CARVE, ItemAbilities.SWORD_SWEEP);
    }

}
