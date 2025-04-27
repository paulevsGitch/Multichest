package paulevs.multichest.mixin;

import net.minecraft.container.slot.Slot;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {
	@Accessor("inventory")
	Inventory multichest_getInventory();
}
