package paulevs.multichest.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.container.ContainerScreen;
import net.minecraft.level.Level;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.modificationstation.stationapi.api.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.multichest.MultichestClient;
import paulevs.multichest.block.MultichestBlock;
import paulevs.multichest.gui.MultichestWrapperScreen;
import paulevs.multichest.listener.CommonListener;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow public HitResult hitResult;
	@Shadow public Level level;
	
	@Inject(method = "openScreen", at = @At("HEAD"))
	private void multichest_openScreen(Screen screen, CallbackInfo info, @Local(argsOnly = true) LocalRef<Screen> localScreen) {
		if (screen instanceof ContainerScreen containerScreen && MultichestClient.isSupportedScreen(containerScreen)) {
			if (hitResult == null || hitResult.type != HitType.BLOCK) return;
			for (byte i = 0; i < 6; i++) {
				Direction side = Direction.byId(i);
				
				int x = hitResult.x + side.getOffsetX();
				int y = hitResult.y + side.getOffsetY();
				int z = hitResult.z + side.getOffsetZ();
				
				if (!level.getBlockState(x, y, z).isOf(CommonListener.multichest)) continue;
				
				MultichestBlock.updateBlockList(level, x, y, z);
				localScreen.set(new MultichestWrapperScreen(containerScreen));
				break;
			}
		}
	}
}
