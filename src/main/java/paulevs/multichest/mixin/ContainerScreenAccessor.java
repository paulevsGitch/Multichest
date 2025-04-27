package paulevs.multichest.mixin;

import net.minecraft.client.gui.screen.container.ContainerScreen;
import net.minecraft.container.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ContainerScreen.class)
public interface ContainerScreenAccessor {
	@Accessor("containerWidth")
	int multichest_containerWidth();
	
	@Accessor("containerHeight")
	int multichest_containerHeight();
	
	@Invoker("renderContainerBackground")
	void multichest_renderContainerBackground(float delta);
	
	@Invoker("renderForeground")
	void multichest_renderForeground();
	
	@Invoker("getSlot")
	Slot multichest_getSlot(int mouseX, int mouseY);
}
