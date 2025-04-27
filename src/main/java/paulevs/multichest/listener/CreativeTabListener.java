package paulevs.multichest.listener;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.item.ItemStack;
import paulevs.bhcreative.listeners.VanillaTabListener;
import paulevs.bhcreative.registry.TabRegistryEvent;

public class CreativeTabListener {
	@EventListener
	public void registerTab(TabRegistryEvent event) {
		VanillaTabListener.tabFullBlocks.addItem(new ItemStack(CommonListener.multichest));
	}
}
