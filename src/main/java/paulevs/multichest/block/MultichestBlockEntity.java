package paulevs.multichest.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;

public class MultichestBlockEntity extends BlockEntity implements Inventory {
	private final ItemStack[] storage = new ItemStack[32];
	
	@Override
	public int getInventorySize() {
		return storage.length;
	}
	
	@Override
	public ItemStack getItem(int index) {
		return storage[index];
	}
	
	@Override
	public ItemStack takeItem(int index, int count) {
		if (storage[index] == null) return null;
		
		if (storage[index].count <= count) {
			ItemStack stack = storage[index];
			storage[index] = null;
			this.markDirty();
			return stack;
		}
		
		ItemStack stack = storage[index].split(count);
		if (storage[index].count == 0) {
			storage[index] = null;
		}
		
		this.markDirty();
		return stack;
	}
	
	@Override
	public void setItem(int slot, ItemStack stack) {
		this.storage[slot] = stack;
		if (stack != null && stack.count > this.getMaxStackSize()) {
			stack.count = this.getMaxStackSize();
		}
		this.markDirty();
	}
	
	@Override
	public String getInventoryName() {
		return "multichest:storage";
	}
	
	@Override
	public int getMaxStackSize() {
		return 64;
	}
	
	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		if (level.getBlockEntity(x, y, z) != this) return false;
		return player.distanceToSqr(this.x + 0.5, this.y + 0.5, this.z + 0.5) < 64.0;
	}
	
	@Override
	public void readIdentifyingData(CompoundTag tag) {
		super.readIdentifyingData(tag);
		tagToArray(tag.getListTag("storage"), storage);
	}
	
	@Override
	public void writeIdentifyingData(CompoundTag tag) {
		super.writeIdentifyingData(tag);
		tag.put("storage", arrayToTag(storage));
	}
	
	private void tagToArray(ListTag tag, ItemStack[] items) {
		for (int i = 0; i < tag.size(); ++i) {
			CompoundTag itemTag = (CompoundTag) tag.get(i);
			int slot = itemTag.getByte("Slot");
			items[slot] = new ItemStack(itemTag);
		}
	}
	
	private ListTag arrayToTag(ItemStack[] items) {
		ListTag tag = new ListTag();
		for (int index = 0; index < items.length; index++) {
			if (items[index] == null || items[index].count < 1) continue;
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.put("Slot", (byte) index);
			items[index].toTag(compoundTag);
			tag.add(compoundTag);
		}
		return tag;
	}
}
