package paulevs.multichest.mixin;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.container.ContainerScreen;
import net.minecraft.container.slot.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.multichest.block.CombinedInventory;
import paulevs.multichest.gui.MultichestScreenMark;

@Mixin(ContainerScreen.class)
public class ContainerScreenMixin extends DrawableHelper {
	@Inject(method = "renderSlot", at = @At("RETURN"))
	private void multichest_renderSlot(Slot slot, CallbackInfo info) {
		if (!(ContainerScreen.class.cast(this) instanceof MultichestScreenMark screen)) return;
		if (slot.id < screen.getMinIndex() || slot.id > screen.getMaxIndex()) return;
		
		SlotAccessor accessor = (SlotAccessor) slot;
		if (!(accessor.multichest_getInventory() instanceof CombinedInventory inventory)) return;
		if (inventory.noFilter()) return;
		
		ItemStack stack = slot.getItem();
		if (stack != null && inventory.isInFilter(stack)) return;
		
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		fill(slot.x - 1, slot.y - 1, slot.x + 17, slot.y + 17, 0xCCC6C6C6);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
	}
}
