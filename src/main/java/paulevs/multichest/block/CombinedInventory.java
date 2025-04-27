package paulevs.multichest.block;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.modificationstation.stationapi.api.registry.ItemRegistry;
import net.modificationstation.stationapi.api.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CombinedInventory implements Inventory {
	private final List<Inventory> inventories;
	private final IntIntPair[] connections;
	private String nameFilter = "";
	private int usedCount;
	private int uiOffset;
	
	public CombinedInventory(List<Inventory> inventories) {
		this.inventories = inventories;
		connections = new IntIntPair[inventories.size() << 5];
		for (int i = 0; i < connections.length; i++) {
			connections[i] = IntIntPair.of(i >> 5, i & 31);
		}
		sortInventory();
	}
	
	@Override
	public int getInventorySize() {
		return connections.length;
	}
	
	@Override
	public ItemStack getItem(int index) {
		IntIntPair pair = connections[index + uiOffset];
		return inventories.get(pair.leftInt()).getItem(pair.rightInt());
	}
	
	@Override
	public ItemStack takeItem(int index, int count) {
		IntIntPair pair = connections[index + uiOffset];
		markDirty();
		return inventories.get(pair.leftInt()).takeItem(pair.rightInt(), count);
	}
	
	@Override
	public void setItem(int index, ItemStack stack) {
		IntIntPair pair = connections[index + uiOffset];
		Inventory inventory = inventories.get(pair.leftInt());
		inventory.setItem(pair.rightInt(), stack);
		inventory.markDirty();
		markDirty();
	}
	
	@Override
	public String getInventoryName() {
		return "multichest:combined_inventory";
	}
	
	@Override
	public int getMaxStackSize() {
		return 64;
	}
	
	@Override
	public void markDirty() {
		sortInventory();
	}
	
	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return true;
	}
	
	public ItemStack addItem(ItemStack stack) {
		for (IntIntPair pair : connections) {
			Inventory inventory = inventories.get(pair.leftInt());
			int index = pair.rightInt();
			ItemStack stored = inventory.getItem(index);
			
			if (stored == null) {
				inventory.setItem(index, stack);
				sortInventory();
				return null;
			}
			
			if (stored.isDamageAndIDIdentical(stack) && stored.count < stored.getMaxStackSize()) {
				stored.count += stack.count;
				stack.count = Math.max(stored.count - stored.getMaxStackSize(), 0);
				stored.count = Math.min(stored.count, stored.getMaxStackSize());
				if (stack.count == 0) {
					updateCount();
					return null;
				}
			}
		}
		
		updateCount();
		return stack.copy();
	}
	
	public int getUsedSlots() {
		return usedCount;
	}
	
	@Environment(EnvType.CLIENT)
	public void setUIOffset(int uiOffset) {
		this.uiOffset = uiOffset;
	}
	
	@Environment(EnvType.CLIENT)
	public void setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
		sortInventory();
	}
	
	@Environment(EnvType.CLIENT)
	public boolean noFilter() {
		return nameFilter.isEmpty();
	}
	
	public boolean isInFilter(ItemStack stack) {
		if (nameFilter.isEmpty()) return true;
		Item item = stack.getType();
		String name = item.getTranslatedName();
		if (name != null && name.toLowerCase(Locale.ROOT).contains(nameFilter)) return true;
		Identifier id = ItemRegistry.INSTANCE.getId(item);
		return id != null && id.toString().toLowerCase(Locale.ROOT).contains(nameFilter);
	}
	
	private void sortInventory() {
		usedCount = 0;
		Arrays.sort(connections, this::pairComparator);
		for (int i = 1; i < connections.length; i++) {
			IntIntPair pair1 = connections[i];
			Inventory inventory1 = inventories.get(pair1.leftInt());
			int index1 = pair1.rightInt();
			ItemStack stack1 = inventory1.getItem(index1);
			
			if (stack1 == null || stack1.count >= stack1.getMaxStackSize()) break;
			usedCount++;
			
			for (int j = i - 1; j >= 0; j--) {
				IntIntPair pair2 = connections[j];
				Inventory inventory2 = inventories.get(pair2.leftInt());
				int index2 = pair2.rightInt();
				ItemStack stack2 = inventory2.getItem(index2);
				
				if (stack2 == null || !stack2.isDamageAndIDIdentical(stack1) || stack2.count >= stack2.getMaxStackSize()) break;
				
				stack2.count += stack1.count;
				if (stack2.count <= stack2.getMaxStackSize()) {
					inventory1.setItem(index1, null);
					for (int n = i; n < connections.length - 1; n++) {
						connections[n] = connections[n + 1];
					}
					connections[connections.length - 1] = pair1;
					usedCount--;
					break;
				}
				stack1.count = stack2.getMaxStackSize() - stack2.count;
				stack2.count = stack2.getMaxStackSize();
			}
		}
		updateCount();
	}
	
	private void updateCount() {
		usedCount = 0;
		for (IntIntPair pair : connections) {
			Inventory inventory = inventories.get(pair.leftInt());
			int index = pair.rightInt();
			ItemStack stack = inventory.getItem(index);
			if (stack == null) return;
			usedCount++;
		}
	}
	
	private int pairComparator(IntIntPair a, IntIntPair b) {
		ItemStack ia = inventories.get(a.leftInt()).getItem(a.rightInt());
		ItemStack ib = inventories.get(b.leftInt()).getItem(b.rightInt());
		return itemComparator(ia, ib);
	}
	
	private int itemComparator(ItemStack a, ItemStack b) {
		if (a == null && b != null) return 1;
		if (a != null && b == null) return -1;
		if (a == null) return 0;
		
		if (!nameFilter.isEmpty()) {
			boolean fa = isInFilter(a);
			boolean fb = isInFilter(b);
			if (fa && fb) return 0;
			if (fa) return -1;
			if (fb) return 1;
		}
		
		Identifier idA = ItemRegistry.INSTANCE.getId(a.getType());
		assert idA != null;
		Identifier idB = ItemRegistry.INSTANCE.getId(b.getType());
		assert idB != null;
		
		int compare = idA.compareTo(idB);
		if (compare != 0) return compare;
		compare = Integer.compare(a.getDamage(), b.getDamage());
		if (compare != 0) return compare;
		return Integer.compare(b.count, a.count);
	}
}
