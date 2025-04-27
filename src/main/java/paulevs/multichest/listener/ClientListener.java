package paulevs.multichest.listener;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.client.gui.screen.GuiHandler;
import net.modificationstation.stationapi.api.client.registry.GuiHandlerRegistry;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.client.texture.atlas.ExpandableAtlas;
import net.modificationstation.stationapi.api.event.registry.GuiHandlerRegistryEvent;
import net.modificationstation.stationapi.api.registry.Registry;
import net.modificationstation.stationapi.api.util.Identifier;
import paulevs.multichest.Multichest;
import paulevs.multichest.block.MultichestBlock;
import paulevs.multichest.block.MultichestBlockEntity;
import paulevs.multichest.gui.MultichestContainer;
import paulevs.multichest.gui.MultichestScreen;

public class ClientListener {
	@EventListener
	public void onTextureRegister(TextureRegisterEvent event) {
		final ExpandableAtlas blockAtlas = Atlases.getTerrain();
		for (byte i = 0; i < 47; i++) {
			Identifier id = Multichest.NAMESPACE.id("block/multichest_" + i);
			MultichestBlock.TEXTURES[i] = blockAtlas.addTexture(id).index;
		}
	}
	
	@EventListener
	public void onGUIRegister(GuiHandlerRegistryEvent event) {
		Registry.register(GuiHandlerRegistry.INSTANCE, MultichestBlock.GUI_ID, new GuiHandler(
			(player, inventory, packet) -> new MultichestScreen(new MultichestContainer(player.inventory)),
			MultichestBlockEntity::new
		));
		/*Registry.register(GuiHandlerRegistry.INSTANCE, MultichestBlock.GUI_ID, new GuiHandler(
			(player, inventory, packet) -> new MultichestWrapperScreen(new MultichestContainer(player.inventory)),
			MultichestBlockEntity::new
		));*/
	}
}
