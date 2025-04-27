package paulevs.multichest.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.container.Container;
import net.minecraft.container.slot.Slot;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ItemStack;
import paulevs.multichest.block.CombinedInventory;
import paulevs.multichest.block.MultichestBlock;

public class MultichestContainer extends Container {
	private final CombinedInventory inventory;
	
	public MultichestContainer(PlayerInventory playerInventory) {
		inventory = new CombinedInventory(MultichestBlock.currentList);
		
		// Content
		for (byte row = 0; row < 4; ++row) {
			for (byte column = 0; column < 8; ++column) {
				addSlot(new Slot(inventory, row << 3 | column, column * 18 + 8, row * 18 + 17));
			}
		}
		
		// Player inventory
		for (byte row = 0; row < 3; ++row) {
			for (byte column = 0; column < 9; ++column) {
				addSlot(new Slot(playerInventory, column + row * 9 + 9, column * 18 + 8, row * 18 + 92));
			}
		}
		
		// Player hotbar
		for (byte i = 0; i < 9; ++i) {
			addSlot(new Slot(playerInventory, i, i * 18 + 8, 150));
		}
	}
	
	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}
	
	@Override
	public ItemStack transferSlot(int slotIndex) {
		Slot source = (Slot) slots.get(slotIndex);
		if (source == null || !source.hasItem()) return null;
		ItemStack stack = source.getItem();
		source.markDirty();
		
		if (slotIndex > 31) stack = inventory.addItem(stack);
		else insertItem(stack, 32, slots.size(), false);
		
		if (stack == null || stack.count == 0) source.setStack(null);
		
		return stack;
	}
	
	public ItemStack addItem(ItemStack stack) {
		return inventory.addItem(stack);
	}
	
	@Environment(EnvType.CLIENT)
	public int getUsage() {
		return Math.round(inventory.getUsedSlots() * 100.0F / inventory.getInventorySize());
	}
	
	@Environment(EnvType.CLIENT)
	public int getInventorySize() {
		return inventory.getInventorySize();
	}
	
	@Environment(EnvType.CLIENT)
	public void setUIOffset(int row) {
		inventory.setUIOffset(row << 3);
		for (int i = 0; i < slots.size(); i++) {
			Slot slot = getSlot(i);
			if (slot != null) slot.markDirty();
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void setNameFilter(String nameFilter) {
		inventory.setNameFilter(nameFilter);
	}
}
