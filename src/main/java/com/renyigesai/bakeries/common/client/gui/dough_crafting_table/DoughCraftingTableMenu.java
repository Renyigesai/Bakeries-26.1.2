package com.renyigesai.bakeries.common.client.gui.dough_crafting_table;

import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import com.renyigesai.bakeries.common.init.BakeriesMenuType;
import com.renyigesai.bakeries.common.init.BakeriesRecipes;
import com.renyigesai.bakeries.common.recipe.DoughCraftingTableRecipe;
import com.renyigesai.bakeries.integration.jei.JeiRecipeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DoughCraftingTableMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT = 0;
    public static final int RESULT_SLOT = 1;
    private static final int INV_SLOT_START = 2;
    private static final int INV_SLOT_END = 29;
    private static final int USE_ROW_SLOT_START = 29;
    private static final int USE_ROW_SLOT_END = 38;
    private final ContainerLevelAccess access;
    private final DataSlot selectedRecipeIndex = DataSlot.standalone();
    private final Level level;
    // ???? List ?洢??
    private List<RecipeHolder<DoughCraftingTableRecipe>> recipesForInput = new ArrayList<>();
    private ItemStack input = ItemStack.EMPTY;
    private long lastSoundTime;
    final Slot inputSlot;
    final Slot resultSlot;
    private Runnable slotUpdateListener = () -> {};
    public final Container container = new SimpleContainer(1) {
        {
            Objects.requireNonNull(DoughCraftingTableMenu.this);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            DoughCraftingTableMenu.this.slotsChanged(this);
            DoughCraftingTableMenu.this.slotUpdateListener.run();
        }
    };
    private final ResultContainer resultContainer = new ResultContainer();

    public DoughCraftingTableMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf datay) {
        this(windowId, playerInventory, ContainerLevelAccess.NULL);
    }

    public DoughCraftingTableMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(BakeriesMenuType.DOUGH_CRAFTING_TABLE_MENU.get(), containerId);
        this.access = access;
        this.level = inventory.player.level();
        this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
        this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
            {
                Objects.requireNonNull(DoughCraftingTableMenu.this);
            }

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack carried) {
                carried.onCraftedBy(player, carried.getCount());
                DoughCraftingTableMenu.this.resultContainer.awardUsedRecipes(player, this.getRelevantItems());
                ItemStack remaining = DoughCraftingTableMenu.this.inputSlot.remove(1);
                if (!remaining.isEmpty()) {
                    DoughCraftingTableMenu.this.setupResultSlot(DoughCraftingTableMenu.this.selectedRecipeIndex.get());
                }

                access.execute((level, pos) -> {
                    long gameTime = level.getGameTime();
                    if (DoughCraftingTableMenu.this.lastSoundTime != gameTime) {
                        level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        DoughCraftingTableMenu.this.lastSoundTime = gameTime;
                    }
                });
                super.onTake(player, carried);
            }

            private List<ItemStack> getRelevantItems() {
                return List.of(DoughCraftingTableMenu.this.inputSlot.getItem());
            }
        });
        this.addStandardInventorySlots(inventory, 8, 84);
        this.addDataSlot(this.selectedRecipeIndex);
    }

    public int getSelectedRecipeIndex() {
        return this.selectedRecipeIndex.get();
    }

    public List<RecipeHolder<DoughCraftingTableRecipe>> getVisibleRecipes() {
        return this.recipesForInput;
    }

    public int getNumberOfVisibleRecipes() {
        return this.recipesForInput.size();
    }

    public boolean hasInputItem() {
        return this.inputSlot.hasItem() && !this.recipesForInput.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, BakeriesBlocks.DOUGH_CRAFTING_TABLE.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (this.selectedRecipeIndex.get() == buttonId) {
            return false;
        } else {
            if (this.isValidRecipeIndex(buttonId)) {
                this.selectedRecipeIndex.set(buttonId);
                this.setupResultSlot(buttonId);
            }
            return true;
        }
    }

    private boolean isValidRecipeIndex(int buttonId) {
        return buttonId >= 0 && buttonId < this.recipesForInput.size();
    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack input = this.inputSlot.getItem();
        if (!input.is(this.input.getItem())) {
            this.input = input.copy();
            this.setupRecipeList(input);
        }
    }

    private void setupRecipeList(ItemStack item) {
        this.selectedRecipeIndex.set(-1);
        this.resultSlot.set(ItemStack.EMPTY);
        this.recipesForInput.clear();
        if (!item.isEmpty()) {
            List<RecipeHolder<DoughCraftingTableRecipe>> recipes = null;
            if (level.isClientSide()){
                recipes = JeiRecipeManager.DOUGH_CRAFTING_TABLES;
            }else {
                if (level.recipeAccess() instanceof RecipeManager manager){
                    recipes = manager.getRecipes().stream().filter(holder -> holder.value().getType() == BakeriesRecipes.DOUGH_CRAFTING_TABLE_TYPE.get()).map(holder -> (RecipeHolder<DoughCraftingTableRecipe>) holder).toList();
                }
            }
            if (recipes != null){
                for (RecipeHolder<DoughCraftingTableRecipe> holder : recipes) {
                    if (holder.value().matches(new SingleRecipeInput(item), level)) {
                        this.recipesForInput.add(holder);
                    }
                }
            }
        } else {
            this.recipesForInput = new ArrayList<>();
        }
    }

    private void setupResultSlot(int index) {
        Optional<RecipeHolder<DoughCraftingTableRecipe>> usedRecipe;
        if (!this.recipesForInput.isEmpty() && this.isValidRecipeIndex(index)) {
            usedRecipe = Optional.ofNullable(this.recipesForInput.get(index));
        } else {
            usedRecipe = Optional.empty();
        }

        usedRecipe.ifPresentOrElse(recipe -> {
            this.resultContainer.setRecipeUsed(recipe);
            this.resultSlot.set(recipe.value().assemble(new SingleRecipeInput(this.container.getItem(0))));
        }, () -> {
            this.resultSlot.set(ItemStack.EMPTY);
            this.resultContainer.setRecipeUsed(null);
        });
        this.broadcastChanges();
    }

    @Override
    public MenuType<?> getType() {
        return BakeriesMenuType.DOUGH_CRAFTING_TABLE_MENU.get(); // ???????滻?????? MenuType
    }

    public void registerUpdateListener(Runnable slotUpdateListener) {
        this.slotUpdateListener = slotUpdateListener;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
        return target.container != this.resultContainer && super.canTakeItemForPickAll(carried, target);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack clicked = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            Item item = stack.getItem();
            clicked = stack.copy();
            if (slotIndex == RESULT_SLOT) {
                item.onCraftedBy(stack, player);
                if (!this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, clicked);
            } else if (slotIndex == INPUT_SLOT) {
                if (!this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.acceptsInput(stack)) {
                if (!this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex >= INV_SLOT_START && slotIndex < INV_SLOT_END) {
                if (!this.moveItemStackTo(stack, USE_ROW_SLOT_START, USE_ROW_SLOT_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex >= USE_ROW_SLOT_START && slotIndex < USE_ROW_SLOT_END + 1
                    && !this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (stack.getCount() == clicked.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
            if (slotIndex == RESULT_SLOT) {
                player.drop(stack, false);
            }

            this.broadcastChanges();
        }

        return clicked;
    }

    public List<RecipeHolder<DoughCraftingTableRecipe>> getAllDoughCraftingTableRecipe(){
        return JeiRecipeManager.DOUGH_CRAFTING_TABLES;
    }

    private boolean acceptsInput(ItemStack stack) {
        List<RecipeHolder<DoughCraftingTableRecipe>> allRecipe = getAllDoughCraftingTableRecipe();
        if (allRecipe.isEmpty()){
            return false;
        }
        return allRecipe.stream().anyMatch(holder -> holder.value().input().test(stack));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.resultContainer.removeItemNoUpdate(1);
        this.access.execute((level, pos) -> this.clearContainer(player, this.container));
    }
}