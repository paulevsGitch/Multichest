package paulevs.multichest.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;
import net.minecraft.util.maths.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.gui.screen.container.GuiHelper;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;
import paulevs.multichest.BlobTileHelper;
import paulevs.multichest.FloodFillSearch;
import paulevs.multichest.Multichest;
import paulevs.multichest.gui.MultichestContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultichestBlock extends TemplateBlockWithEntity {
	public static final Identifier GUI_ID = Multichest.NAMESPACE.id("multichest");
	
	public static final int[] TEXTURES = new int[47];
	private static final List<BlockPos> BLOCK_POS = new ArrayList<>();
	public static List<Inventory> currentList;
	
	public MultichestBlock(Identifier id) {
		super(id, Material.WOOD);
		setTranslationKey(id);
		setSounds(WOOD_SOUNDS);
		setHardness(1.0F);
	}
	
	@Override
	public int getTexture(BlockView blockView, int x, int y, int z, int side) {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return getTexture(side);
		if (!(blockView instanceof BlockStateView view)) return getTexture(side);
		return TEXTURES[BlobTileHelper.getTexture(view, x, y, z, this::filter, Direction.byId(side))];
	}
	
	@Override
	public int getTexture(int side) {
		return TEXTURES[15];
	}
	
	@Override
	public boolean canUse(Level level, int x, int y, int z, PlayerEntity player) {
		if (player.isChild()) return false;
		if (level.isRemote) return true;
		
		MultichestBlockEntity entity = (MultichestBlockEntity) level.getBlockEntity(x, y, z);
		if (entity == null) return false;
		
		updateBlockList(level, x, y, z);
		GuiHelper.openGUI(player, GUI_ID, entity, new MultichestContainer(player.inventory));
		
		return true;
	}
	
	@Override
	protected BlockEntity createBlockEntity() {
		return new MultichestBlockEntity();
	}
	
	private boolean filter(BlockState state) {
		return state.isOf(this);
	}
	
	@Override
	public void onBlockRemoved(Level level, int x, int y, int z) {
		MultichestBlockEntity entity = (MultichestBlockEntity) level.getBlockEntity(x, y, z);
		if (entity == null) return;
		for (int i = 0; i < entity.getInventorySize(); i++) {
			ItemStack stack = entity.getItem(i);
			if (stack == null) continue;
			drop(level, x, y, z, stack);
		}
		level.removeBlockEntity(x, y, z);
	}
	
	public static void updateBlockList(Level level, int x, int y, int z) {
		BLOCK_POS.clear();
		FloodFillSearch.getBlocks(level, x, y, z, 64, BLOCK_POS);
		currentList = BLOCK_POS
			.stream()
			.map(pos -> (MultichestBlockEntity) level.getBlockEntity(pos.x, pos.y, pos.z))
			.collect(Collectors.toList());
	}
}
