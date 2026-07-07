package com.renyigesai.bakeries.common.blocks.bread_rack;

import com.mojang.logging.LogUtils;
import com.renyigesai.bakeries.api.ItemStackHandler;
import com.renyigesai.bakeries.common.blocks.mix_block.MixBlock;
import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ListBackedContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class BreadRackBlockEntity extends BlockEntity implements ItemOwner , ListBackedContainer {


    private ItemStackHandler inventory;
    private static final Logger LOGGER = LogUtils.getLogger();
    public State state = State.CLOSE;
    public float progress;
    public float progressOld;

    public BreadRackBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BakeriesBlocks.Entities.BREAD_RACK_ENTITY.get(), pPos, pBlockState);
        inventory = new ItemStackHandler(4);

        if (pBlockState.hasProperty(BlockStateProperties.OPEN)
                && pBlockState.getValue(BlockStateProperties.OPEN)) {
            this.state = State.OPEN;
            this.progress = 1.0F;
            this.progressOld = 1.0F;
        }
    }

    public int getInventoryCount() {
        int count = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()){
                count ++;
            }
        }
        return count;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.inventory.getItems();
    }

    public boolean isEmpty(){
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()){
                return false;
            }
        }
        return true;
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER);

        CompoundTag var4;
        try {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            ContainerHelper.saveAllItems(output, this.inventory.getItems(), true);
            var4 = output.buildResult();
        } catch (Throwable var7) {
            try {
                reporter.close();
            } catch (Throwable var6) {
                var7.addSuppressed(var6);
            }

            throw var7;
        }
        reporter.close();
        return var4;
    }


    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inventory.getItems().clear();
        ContainerHelper.loadAllItems(input, this.inventory.getItems());
    }

    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory.getItems(), true);
    }

    public boolean addItem(ItemStack stack){
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (stackInSlot.isEmpty()){
                inventory.setStackInSlot(i,stack);
                updateBlock();
                return true;
            }
        }
        return false;
    }

    public Level level(){
        return this.level;
    }

    public Vec3 position() {
        return this.getBlockPos().getCenter();
    }

    public float getVisualRotationYInDegrees() {
        return ((Direction)this.getBlockState().getValue(MixBlock.FACING)).getOpposite().toYRot();
    }

    public ItemStack getItem(int slot){
        return inventory.getStackInSlot(slot);
    }

    public void setItem(int slot , ItemStack stack){
        inventory.setStackInSlot(slot,stack);
        updateBlock();
    }

    public boolean putItem(int slot , ItemStack stack){
        if (inventory.getStackInSlot(slot).isEmpty()){
            inventory.setStackInSlot(slot,stack);
            updateBlock();
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    public int getItemsCount() {
        int count = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()){
                count ++;
            }
        }
        return count;
    }

    public void updateBlock() {
        if (level == null){
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        setChanged(level, worldPosition, state);
        level.sendBlockUpdated(worldPosition, state, state, 3);
    }

    public float getProgress(float pPartialTicks) {
        return Mth.lerp(pPartialTicks, this.progressOld, this.progress);
    }

    @Override
    public boolean triggerEvent(int pId, int pType) {
        if (pId == 0) {
            if (pType == 0) {
                this.state = State.OPEN_PROCESS;
            }
            if (pType == 1) {
                this.state = State.CLOSE_PROCESS;
            }
            doNeighborUpdates(this.getLevel(), this.worldPosition, this.getBlockState());
            return true;
        } else {
            return super.triggerEvent(pId, pType);
        }
    }

    private static void doNeighborUpdates(Level pLevel, BlockPos pPos, BlockState pState) {
        pState.updateNeighbourShapes(pLevel, pPos, 3);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BreadRackBlockEntity blockEntity){
        blockEntity.progressOld = blockEntity.progress;
        switch (blockEntity.state) {
            case OPEN_PROCESS:
                blockEntity.progress += 0.25F;
                if (blockEntity.progress >= 1.0F) {
                    blockEntity.progress = 1.0F;
                    blockEntity.state = State.OPEN;
                }
                break;
            case OPEN:
                blockEntity.progress = 1.0F;
                break;
            case CLOSE_PROCESS:
                blockEntity.progress -= 0.25F;
                if (blockEntity.progress <= 0F) {
                    blockEntity.progress = 0F;
                    blockEntity.state = State.CLOSE;
                }
                break;
            case CLOSE:
                blockEntity.progress = 0.0F;
                break;
        }
    }

    public enum State {
        OPEN_PROCESS,
        OPEN,
        CLOSE_PROCESS,
        CLOSE,
    }
}
