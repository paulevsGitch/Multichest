package paulevs.multichest;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.level.Level;
import net.minecraft.util.maths.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import paulevs.multichest.listener.CommonListener;

import java.util.List;

public class FloodFillSearch {
	private static final IntSet POSITIONS = new IntOpenHashSet();
	private static final IntList[] BUFFERS = new IntList[] {
		new IntArrayList(),
		new IntArrayList()
	};
	
	private static byte bufferIndex;
	private static int centerX;
	private static int centerY;
	private static int centerZ;
	
	public static void getBlocks(Level level, int x, int y, int z, int radius, List<BlockPos> output) {
		centerX = x - 512;
		centerY = y - 512;
		centerZ = z - 512;
		
		IntList startPositions = BUFFERS[bufferIndex];
		startPositions.clear();
		startPositions.add(getIndex(x, y, z));
		
		output.add(new BlockPos(x, y, z));
		POSITIONS.add(getIndex(x, y, z));
		
		while (!startPositions.isEmpty()) {
			bufferIndex = (byte) ((bufferIndex + 1) & 1);
			IntList endPositions = BUFFERS[bufferIndex];
			endPositions.clear();
			
			for (int index : startPositions) {
				int sx = getX(index);
				int sy = getY(index);
				int sz = getZ(index);
				
				for (byte i = 0; i < 27; i++) {
					if (i == 13) continue;
					int px = sx + (i % 3) - 1;
					if (Math.abs(px - x) > radius) continue;
					int py = sy + ((i / 3) % 3) - 1;
					if (Math.abs(py - y) > radius) continue;
					int pz = sz + i / 9 - 1;
					if (Math.abs(pz - z) > radius) continue;
					int leafIndex = getIndex(px, py, pz);
					if (POSITIONS.contains(leafIndex)) continue;
					BlockState state = level.getBlockState(px, py, pz);
					if (!state.isOf(CommonListener.multichest)) continue;
					output.add(new BlockPos(px, py, pz));
					endPositions.add(leafIndex);
					POSITIONS.add(leafIndex);
				}
			}
			
			startPositions = endPositions;
		}
		
		POSITIONS.clear();
	}
	
	private static int getIndex(int x, int y, int z) {
		return ((x - centerX) & 1023) << 20 | ((y - centerY) & 1023) << 10 | (z - centerZ) & 1023;
	}
	
	private static int getX(int index) {
		return (index >> 20) + centerX;
	}
	
	private static int getY(int index) {
		return ((index >> 10) & 1023) + centerY;
	}
	
	private static int getZ(int index) {
		return (index & 1023) + centerZ;
	}
}
