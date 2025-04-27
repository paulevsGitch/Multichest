package paulevs.multichest.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.container.ContainerScreen;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.container.slot.Slot;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import paulevs.multichest.block.CombinedInventory;
import paulevs.multichest.block.MultichestBlock;
import paulevs.multichest.mixin.ContainerScreenAccessor;

public class MultichestWrapperScreen extends ContainerScreen implements MultichestScreenMark {
	private static final int WIDTH = 104;
	private static final int HEIGHT = 166;
	
	private final CombinedInventory inventory;
	private final ContainerScreen content;
	private final int startIndex;
	private final int maxRow;
	
	private int backgroundTexture = -1;
	private int currentRow = 0;
	private boolean isInBounds;
	private int dragDelta;
	private boolean dragging;
	private boolean typing;
	private String nameFilter = "";
	
	@SuppressWarnings("unchecked")
	public MultichestWrapperScreen(ContainerScreen content) {
		super(content.container);
		this.content = content;
		
		ContainerScreenAccessor accessor = (ContainerScreenAccessor) content;
		int offsetX = 10 + (accessor.multichest_containerWidth() >> 1);
		
		inventory = new CombinedInventory(MultichestBlock.currentList);
		containerWidth = accessor.multichest_containerWidth();
		containerHeight = accessor.multichest_containerHeight();
		startIndex = content.container.slots.size();
		
		for (byte i = 0; i < 32; i++) {
			int x = (i & 3) * 18 - offsetX;
			int y = (i >> 2) * 18 + 17;
			Slot slot = new Slot(inventory, i, x, y);
			slot.id = content.container.slots.size();
			content.container.slots.add(slot);
			content.container.items.add(null);
		}
		
		maxRow = (inventory.getInventorySize() >> 2) - 8;
	}
	
	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		
		ContainerScreenAccessor accessor = (ContainerScreenAccessor) content;
		
		int posX = (width - containerWidth) >> 1;
		int posY = (height - containerHeight) >> 1;
		
		RenderHelper.disableLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		
		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 0.0F);
		accessor.multichest_renderForeground();
		GL11.glPopMatrix();
		
		posX = ((width - accessor.multichest_containerWidth()) >> 1) - WIDTH - 2;
		posY = (height - HEIGHT) >> 1;
		posX = mouseX - posX;
		posY = mouseY - posY;
		isInBounds = posX >= 0 && posY >= 0 && posX < WIDTH && posY < HEIGHT;
		
		if (!dragging) return;
		posY = mouseY - dragDelta;
		int scrollY = MathHelper.clamp(posY, 0, 126);
		int row = Math.round(scrollY / 126.0F * maxRow);
		if (row == currentRow) return;
		currentRow = row;
		inventory.setUIOffset(row << 2);
	}
	
	@Override
	protected void renderContainerBackground(float delta) {
		ContainerScreenAccessor accessor = (ContainerScreenAccessor) content;
		accessor.multichest_renderContainerBackground(delta);
		
		if (backgroundTexture == -1) {
			backgroundTexture = minecraft.textureManager.getTextureId("/assets/multichest/stationapi/textures/gui/multichest_side.png");
		}
		
		minecraft.textureManager.bindTexture(backgroundTexture);
		int posX = ((width - accessor.multichest_containerWidth()) >> 1) - WIDTH - 2;
		int posY = (height - HEIGHT) >> 1;
		blit(posX, posY, 0, 0, WIDTH, HEIGHT);
		
		scroll();
		float rowDelta = (float) currentRow / maxRow;
		int scrollY = MathHelper.lerp(rowDelta, 17, 143);
		if (scrollY < 17) scrollY = 17;
		blit(posX + 84, posY + scrollY, 242, 1, 12, 15);
		
		if (typing) {
			int width = textManager.getTextWidth(nameFilter);
			textManager.drawText("_", posX + 9 + width, posY + 5, 0xFFFFFFFF);
		}
		textManager.drawText(nameFilter, posX + 9, posY + 5, 0xFFFFFFFF);
	}
	
	@Override
	public void init(Minecraft minecraft, int width, int height) {
		content.init(minecraft, width, height);
		super.init(minecraft, width, height);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		ContainerScreenAccessor accessor = (ContainerScreenAccessor) content;
		minecraft.textureManager.bindTexture(backgroundTexture);
		
		int posX = ((width - accessor.multichest_containerWidth()) >> 1) - WIDTH - 2;
		int posY = (height - containerHeight) >> 1;
		posX = mouseX - posX;
		posY = mouseY - posY;
		
		if (posX >= 0 && posY >= 0 && posX < WIDTH && posY < HEIGHT) {
			if (button != 0 && button != 1) return;
			
			typing = posX > 8 && posY > 5 && posX < 96 && posY < 13;
			if (typing) return;
			
			if (posX > 84 && posY > 17 && posX < 96 && posY < 158) {
				float rowDelta = (float) currentRow / maxRow;
				int scrollY = MathHelper.lerp(rowDelta, 17, 143);
				if (scrollY < 17) scrollY = 17;
				if (posY >= scrollY && posY < scrollY + 16) {
					dragDelta = mouseY - (scrollY - 17);
					dragging = true;
					return;
				}
			}
			
			PlayerInventory inventory = minecraft.player.inventory;
			ItemStack hand = inventory.getCursorItem();
			Slot slot = accessor.multichest_getSlot(mouseX, mouseY);
			if (slot == null) return;
			if (hand == null) {
				if (slot.hasItem()) {
					ItemStack stored = slot.getItem();
					ItemStack taken = stored;
					if (button == 1 && stored.count > 1) {
						taken = new ItemStack(stored.getType(), stored.count >> 1, stored.getDamage());
						stored.count -= taken.count;
					}
					else slot.setStack(null);
					inventory.setCursorItem(taken);
					slot.markDirty();
				}
			}
			else {
				if (button == 1 && hand.count > 1) {
					ItemStack put = new ItemStack(hand.getType(), 1, hand.getDamage());
					hand.count--;
					put = this.inventory.addItem(put);
					if (put != null) hand.count += put.count;
					if (hand.count == 0) hand = null;
					inventory.setCursorItem(hand);
				}
				else {
					hand = this.inventory.addItem(hand);
					inventory.setCursorItem(hand);
				}
			}
		}
		else super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	protected void keyPressed(char character, int key) {
		if (!typing) {
			super.keyPressed(character, key);
			return;
		}
		else if (key == Keyboard.KEY_ESCAPE) {
			typing = false;
			return;
		}
		
		if (key == Keyboard.KEY_BACK) {
			if (!nameFilter.isEmpty()) {
				nameFilter = nameFilter.substring(0, nameFilter.length() - 1);
				inventory.setNameFilter(nameFilter);
				if (currentRow != 0) {
					currentRow = 0;
					inventory.setUIOffset(currentRow);
				}
			}
			return;
		}
		else if (!Character.isAlphabetic(character) && !Character.isWhitespace(character) && !Character.isDigit(character)) {
			super.keyPressed(character, key);
			return;
		}
		
		if (nameFilter.length() >= 14) return;
		nameFilter += character;
		inventory.setNameFilter(nameFilter);
		if (currentRow != 0) {
			currentRow = 0;
			inventory.setUIOffset(currentRow);
		}
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		super.mouseReleased(mouseX, mouseY, button);
		if (button == -1) return;
		dragging = false;
	}
	
	private void scroll() {
		if (!isInBounds) return;
		int wheel = (int) Math.signum(Mouse.getDWheel());
		int row = MathHelper.clamp(currentRow - wheel, 0, maxRow);
		if (row == currentRow) return;
		currentRow = row;
		inventory.setUIOffset(row << 2);
	}
	
	@Override
	public int getMinIndex() {
		return startIndex;
	}
	
	@Override
	public int getMaxIndex() {
		return startIndex + 31;
	}
}
