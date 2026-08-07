package com.renyigesai.bakeries.common.items;

import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import com.renyigesai.bakeries.common.init.BakeriesFoodProperties;
import com.renyigesai.bakeries.common.init.BakeriesItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BaguetteItem extends RepeatEatItem {


    public BaguetteItem(Identifier identifier) {
        super(BakeriesBlocks.BAGUETTE.get(),new Item.Properties().food(BakeriesFoodProperties.BAGUETTE).enchantable(15).attributes(createAttributes()).stacksTo(1).useBlockDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM,identifier)),4,false);
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
        return 1;
    }


    @Override
    public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
        onConsume(entity.level(),entity,itemstack);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_ID,3d,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_ID,-3d,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
                .build();
    }

}
