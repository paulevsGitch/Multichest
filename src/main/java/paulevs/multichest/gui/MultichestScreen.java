package paulevs.multichest.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.container.ContainerScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

@Environment(EnvType.CLIENT)
public class MultichestScreen extends ContainerScreen implements MultichestScreenMark {
	private final MultichestContainer container;
	private final int maxRow;
	
	private int backgroundTexture = -1;
	private int currentRow = 0;
	private int dragDelta;
	private boolean dragging;
	private boolean typing;
	private String nameFilter = "";
	
	public MultichestScreen(MultichestContainer container) {
		super(container);
		this.container = container;
		containerHeight = 176;
		maxRow = (container.getInventorySize() >> 3) - 4;
	}
	
	@Override
	protected void renderContainerBackground(float delta) {
		if (backgroundTexture == -1) {
			backgroundTexture = minecraft.textureManager.getTextureId("/assets/multichest/stationapi/textures/gui/multichest.png");
		}
		
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		minecraft.textureManager.bindTexture(backgroundTexture);
		int posX = (width - containerWidth) >> 1;
		int posY = (height - containerHeight) >> 1;
		blit(posX, posY, 0, 0, containerWidth, containerHeight);
		
		scroll();
		float rowDelta = (float) currentRow / maxRow;
		int scrollY = MathHelper.lerp(rowDelta, 17, 72);
		if (scrollY < 17) scrollY = 17;
		blit(posX + 156, posY + scrollY, 242, 1, 12, 15);
		
		int usage = container.getUsage();
		int color = 0xFFBE0000;
		if (usage < 50) color = 0xFF49CB19;
		else if (usage < 75) color = 0xFFF6E322;
		else if (usage < 90) color = 0xFFD46203;
		
		String text = I18n.translate("tile.multichest.gui.filled") + ": ";
		int width = textManager.getTextWidth(text);
		textManager.drawText(text, posX + 8, posY + 6, Color.DARK_GRAY.getRGB());
		textManager.drawText(usage + "%", posX + 8 + width, posY + 6, color);
		
		if (typing) {
			width = textManager.getTextWidth(nameFilter);
			textManager.drawText("_", posX + 81 + width, posY + 5, 0xFFFFFFFF);
		}
		textManager.drawText(nameFilter, posX + 81, posY + 5, 0xFFFFFFFF);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		int posX = (width - containerWidth) >> 1;
		int posY = (height - containerHeight) >> 1;
		
		posX = mouseX - posX;
		posY = mouseY - posY;
		
		if (posX > 7 && posY > 16 && posX < 151 && posY < 88) {
			PlayerInventory inventory = minecraft.player.inventory;
			ItemStack item = inventory.getCursorItem();
			if (item != null) {
				if (button == 1) {
					ItemStack split = new ItemStack(item.getType(), 1, item.getDamage());
					item.count--;
					split = container.addItem(split);
					if (split != null && split.count > 0) {
						item.count += split.count;
					}
					if (item.count == 0) item = null;
				}
				else item = container.addItem(item);
				inventory.setCursorItem(item);
				return;
			}
		}
		
		if (posX > 155 && posX < 168 && posY > 16 && posY < 87) {
			float rowDelta = (float) currentRow / maxRow;
			int scrollY = MathHelper.lerp(rowDelta, 17, 72);
			if (scrollY < 17) scrollY = 17;
			if (posY >= scrollY && posY < scrollY + 16) {
				dragDelta = mouseY - (scrollY - 17);
				dragging = true;
				return;
			}
		}
		
		typing = posX > 80 && posY > 5 && posX < 168 && posY < 13;
		
		super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		if (!dragging) return;
		int posY = mouseY - dragDelta;
		int scrollY = MathHelper.clamp(posY, 0, 55);
		int row = Math.round(scrollY / 55.0F * maxRow);
		if (row == currentRow) return;
		currentRow = row;
		container.setUIOffset(row);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		super.mouseReleased(mouseX, mouseY, button);
		if (button == -1) return;
		dragging = false;
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
				container.setNameFilter(nameFilter);
				if (currentRow != 0) {
					currentRow = 0;
					container.setUIOffset(currentRow);
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
		container.setNameFilter(nameFilter);
		if (currentRow != 0) {
			currentRow = 0;
			container.setUIOffset(currentRow);
		}
	}
	
	private void scroll() {
		int wheel = (int) Math.signum(Mouse.getDWheel());
		int row = MathHelper.clamp(currentRow - wheel, 0, maxRow);
		if (row == currentRow) return;
		currentRow = row;
		container.setUIOffset(row);
	}
	
	@Override
	public int getMinIndex() {
		return 0;
	}
	
	@Override
	public int getMaxIndex() {
		return 31;
	}
}
