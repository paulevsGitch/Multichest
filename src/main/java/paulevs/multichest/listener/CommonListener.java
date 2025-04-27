package paulevs.multichest.listener;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import paulevs.multichest.Multichest;
import paulevs.multichest.block.MultichestBlock;
import paulevs.multichest.block.MultichestBlockEntity;

public class CommonListener {
	public static MultichestBlock multichest;
	
	@EventListener
	public void onBlockInit(BlockRegistryEvent event) {
		multichest = new MultichestBlock(Multichest.NAMESPACE.id("multichest"));
	}
	
	@EventListener
	public void onBlockEntityRegister(BlockEntityRegisterEvent event) {
		event.register(MultichestBlockEntity.class, "multichest:storage");
	}
}
