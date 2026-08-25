package com.renyigesai.bakeries.common.blocks.luminous_light_sign;

import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class LuminousLightSignBlockEntity extends BlockEntity {
    private String text;
    private int color = -16777216;
    public LuminousLightSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BakeriesBlocks.Entities.LUMINOUS_LIGHT_SIGN_ENTITY.get(), pPos, pBlockState);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        update();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        update();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Text", Objects.requireNonNullElse(text, ""));
        output.putInt("Color",color);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.text = input.getStringOr("Text","");
        this.color = input.getIntOr("Color",-16777216);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void update(){
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(),this.getBlockState(),this.getBlockState(),3);
        }
    }

}
