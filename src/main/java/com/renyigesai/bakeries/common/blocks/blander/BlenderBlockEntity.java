package com.renyigesai.bakeries.common.blocks.blander;

import com.renyigesai.bakeries.common.client.gui.blender.BlenderMenu;
import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import com.renyigesai.bakeries.common.init.BakeriesRecipes;
import com.renyigesai.bakeries.common.recipe.blender.BlenderInput;
import com.renyigesai.bakeries.common.recipe.blender.BlenderRecipe;
import com.renyigesai.bakeries.common.utils.ItemUtils;
import com.renyigesai.bakeries.common.utils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@SuppressWarnings("removal")
public class BlenderBlockEntity extends BaseContainerBlockEntity {

    private static final int CONTAINER_SLOT = 9;
    private static final int OUTPUT_SLOT = 10;
    private static final int[] SLOTS_FOR_DOWN = new int[]{10};

    protected ItemStackHandler inventory = new ItemStackHandler(11);//11个槽位

    protected ItemStackHandler filtrationinventory = new ItemStackHandler(10){
    };//9个过滤槽位

    public int cookingTotalTime;
    public int filtrationIndex;

    public State state = State.CLOSE;
    public float progress;
    public float progressOld;
    public float rProgress;
    public float rProgressOld;

    private final RecipeManager.CachedCheck<BlenderInput, BlenderRecipe> quickCheck;
    public BlenderBlockEntity(BlockPos pos, BlockState blockState) {
        super(BakeriesBlocks.Entities.BLENDER_ENTITY.get(), pos, blockState);
        quickCheck = RecipeManager.createCheck(BakeriesRecipes.BLENDER_TYPE.get());
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    public ItemStackHandler getFiltrationinventory() {
        return this.filtrationinventory;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.bakeries.blender");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return ItemUtils.toNonNullList(inventory);  // 直接返回内部列表
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        // 将传入的列表复制到 inventory 的对应槽位
        for (int i = 0; i < Math.min(items.size(), this.inventory.getSlots()); i++) {
            this.inventory.setStackInSlot(i, items.get(i));
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new BlenderMenu(i, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return this.inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean getIntList(int i,int[] intList){
        for (int j = 0; j < intList.length; j++) {
            if (intList[j] == i){
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = inventory.getStackInSlot(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.split(amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        for (int i = 0; i < filtrationinventory.getSlots(); ++i) {
            ItemStack filtrationStack = filtrationinventory.getStackInSlot(i);
            if (stack.is(filtrationStack.getItem()) && inventory.getStackInSlot(i).isEmpty()) {
                ItemStack singleStack = stack.copy();
                stack.shrink(1);
                singleStack.setCount(1);
                inventory.setStackInSlot(i, singleStack);
                break;
            }
        }
        setChanged();
    }

    private boolean hasInput() {
        for(int i = 0; i < 9; ++i) {
            if (!this.inventory.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void setFiltrationIndex(int filtrationIndex) {
        this.filtrationIndex = filtrationIndex;
    }

    public int getFiltrationIndex() {
        return filtrationIndex;
    }


    public float getProgress(float pPartialTicks) {
        return Mth.lerp(pPartialTicks, this.progressOld, this.progress);
    }

    public float getRprogress(float pPartialTicks) {
        return Mth.lerp(pPartialTicks, this.rProgressOld, this.rProgress);
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

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.inventory = new ItemStackHandler(11);
        WorldUtils.loadItemsToHandler(input,this.inventory,"Items");

        this.filtrationinventory = new ItemStackHandler(10);
        WorldUtils.loadItemsToHandler(input,this.filtrationinventory,"FiltrationItems");

        cookingTotalTime = input.getIntOr("CookingTotalTime",0);
        filtrationIndex = input.getIntOr("FiltrationIndex",0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        WorldUtils.saveAllItems(output, ItemUtils.toNonNullList(this.inventory),"Items");
        WorldUtils.saveAllItems(output, ItemUtils.toNonNullList(this.filtrationinventory),"FiltrationItems");
        output.putInt("CookingTotalTime",cookingTotalTime);
        output.putInt("FiltrationIndex",filtrationIndex);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D,
                (double) this.worldPosition.getY() + 0.5D,
                (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlenderBlockEntity blockEntity){
        blockEntity.progressOld = blockEntity.progress;
        blockEntity.rProgressOld = blockEntity.rProgress;
        if (!state.getValue(BlenderBlock.POWERED)) {
            switch (blockEntity.state) {
                case OPEN_PROCESS:
                    blockEntity.progress += 0.5F;
                    if (blockEntity.progress >= 1.0F) {
                        blockEntity.progress = 1.0F;
                        blockEntity.state = State.OPEN;
                    }
                    break;
                case OPEN:
                    blockEntity.progress = 1.0F;
                    break;
                case CLOSE_PROCESS:
                    blockEntity.progress -= 0.5F;
                    if (blockEntity.progress <= 0F) {
                        blockEntity.progress = 0F;
                        blockEntity.state = State.CLOSE;
                    }
                    break;
                case CLOSE:
                    blockEntity.progress = 0.0F;
                    break;
            }
        }else {
            blockEntity.rProgress += 0.1F;
            if (blockEntity.rProgress >= 1.0F){
                blockEntity.rProgress = 0.0F;
            }
            if (blockEntity.progress > 0F){
                blockEntity.progress -= 0.25F;
            }
            if (blockEntity.progress <= 0){
                blockEntity.progress = 0F;
            }
            blockEntity.state = State.CLOSE;
        }
    }


    public static void craftTick(Level level, BlockPos pos, BlockState state, BlenderBlockEntity blockEntity) {
        if (blockEntity.hasInput()) {
            blockEntity.craftItem();
            boolean temp = blockEntity.cookingTotalTime > 0;
            level.setBlock(pos, state.setValue(BlenderBlock.POWERED, temp), 3);
            setChanged(level, pos, state);
            if (!level.isClientSide()) {
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    private void craftItem() {
        if (!(this.level instanceof ServerLevel serverLevel)){
            return;
        }
        Optional<RecipeHolder<BlenderRecipe>> recipe = getBlenderRecipe(ItemUtils.toNonNullList(inventory),serverLevel);
        if (recipe.isPresent()) {

        }else {
            cookingTotalTime = 0;
            return;
        }
        BlenderRecipe blenderRecipe = recipe.get().value();
        ItemStack resultItem = blenderRecipe.result().create();
        ItemStack outputStack = inventory.getStackInSlot(OUTPUT_SLOT);

        if (!canCraft(resultItem, outputStack) || !isContainer(recipe.get())) {
            cookingTotalTime = 0;
            return;
        }

        if (cookingTotalTime < 100) {
            cookingTotalTime++;
            spawnParticle();
        }else {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.is(Items.WATER_BUCKET)){
                    if (stack.getCraftingRemainder() != null) {
                        ejectIngredientRemainder(stack.getCraftingRemainder().create());
                    }
                    stack.shrink(1);
                }
            }

            if (!blenderRecipe.getContainer().isEmpty()) {
                ItemStack stackInSlot = inventory.getStackInSlot(CONTAINER_SLOT);
                stackInSlot.shrink(1);
            }

            if (outputStack.isEmpty()) {
                inventory.setStackInSlot(OUTPUT_SLOT, resultItem);
            } else {
                outputStack.grow(resultItem.getCount());
            }

            cookingTotalTime = 0;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        }
    }

    protected void ejectIngredientRemainder(ItemStack remainderStack) {
        BlockPos pos = this.getBlockPos();
        Level level = this.level;
        Direction facing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING).getClockWise();
        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;
        double newX = x + (facing.getStepX()*1.0D);
        double newZ = z + (facing.getStepZ()*1.0D);
        ItemUtils.spawnItemEntity(this.level,remainderStack, newX, pos.getY(), newZ,new Vec3(0.0D,0.0D,0.0D));
    }

    private boolean isContainer(RecipeHolder<BlenderRecipe> recipeHolder){
        BlenderRecipe recipe = recipeHolder.value();
        ItemStack container = recipe.getContainer();
        return container.is(this.inventory.getStackInSlot(CONTAINER_SLOT).getItem());
    }

    private boolean canCraft(ItemStack resultItem,ItemStack outputStack){
        if (outputStack.isEmpty()){
            return true;
        }
        if (resultItem.is(outputStack.getItem())){
            int count = resultItem.getCount();
            return outputStack.getCount() + count <= outputStack.getMaxStackSize();
        }
        return false;
    }

    private Optional<RecipeHolder<BlenderRecipe>> getBlenderRecipe(List<ItemStack> stacks, ServerLevel level){
        return quickCheck.getRecipeFor(new BlenderInput(stacks),level);
    }

    private void spawnParticle(){
        BlockPos pos = this.getBlockPos();
        Level pLevel = this.level;
        if (pLevel instanceof ServerLevel serverLevel){
            ArrayList<ItemStack> stacks = new ArrayList<>();
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stackInSlot = inventory.getStackInSlot(i);
                if (!stackInSlot.isEmpty()){
                    stacks.add(stackInSlot);
                }
            }
            for (int i = 0; i < stacks.size(); i++) {
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stacks.get(i).getItem()),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.075);
            }
        }
    }

    @Override
    public boolean canPlaceItem(int pIndex, ItemStack stack) {
        for (int i = 0; i < filtrationinventory.getSlots(); ++i) {
            ItemStack filtrationStack = filtrationinventory.getStackInSlot(i);
            if (stack.is(filtrationStack.getItem()) && inventory.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canTakeItem(Container into, int slot, ItemStack itemStack) {
        return slot == OUTPUT_SLOT;
    }

    public enum State {
        OPEN_PROCESS,
        OPEN,
        CLOSE_PROCESS,
        CLOSE,
    }

}
