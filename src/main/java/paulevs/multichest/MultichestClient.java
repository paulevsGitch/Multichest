package paulevs.multichest;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.container.ContainerScreen;
import net.minecraft.client.gui.screen.container.CraftingScreen;
import net.minecraft.client.gui.screen.container.FurnaceScreen;

import java.util.Set;

@Environment(EnvType.CLIENT)
public class MultichestClient {
	private static final Set<Class<? extends ContainerScreen>> SUPPORTED_SCREENS = new ReferenceOpenHashSet<>();
	
	public static boolean isSupportedScreen(ContainerScreen screen) {
		return SUPPORTED_SCREENS.contains(screen.getClass());
	}
	
	static {
		SUPPORTED_SCREENS.add(CraftingScreen.class);
		SUPPORTED_SCREENS.add(FurnaceScreen.class);
	}
}
