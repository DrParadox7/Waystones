package net.blay09.mods.waystones.client.gui;

import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.network.NetworkHandler;
import net.blay09.mods.waystones.network.message.MessageWarpStone;
import net.blay09.mods.waystones.util.WaystoneEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

public class GuiWarpStone extends GuiScreen {

	private static final int BUTTON_WIDTH = 95;
	private static final int BUTTON_HEIGHT = 22;
	private static int buttonsPerPage;
	private final WaystoneEntry[] entries;
	private GuiButton btnPrevPage;
	private GuiButton btnNextPage;
	private int pageOffset;
	private boolean isFree;

	public GuiWarpStone(WaystoneEntry[] entries, boolean isFree) {
		this.entries = entries;
		this.isFree = isFree;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		buttonsPerPage = WaystoneConfig.warpListButtonsPerPage;

		// TODO: recalculate positions based on resolution and buttons per page.
		int centerX = width / 2;
		int centerY = height / 2;
		int prevX = centerX - 100;
		int prevY = centerY + 7 + 11 * buttonsPerPage;
		btnPrevPage = new GuiButton(0, prevX, prevY, BUTTON_WIDTH, BUTTON_HEIGHT,
				I18n.format("gui.waystones:warpStone.previousPage"));
		buttonList.add(btnPrevPage);

		int nextX = centerX + 5;
		int nextY = prevY;
		btnNextPage = new GuiButton(1, nextX, nextY, BUTTON_WIDTH, BUTTON_HEIGHT,
				I18n.format("gui.waystones:warpStone.nextPage"));
		buttonList.add(btnNextPage);

		updateList();
	}

	@SuppressWarnings("unchecked")
	public void updateList() {
		btnPrevPage.enabled = pageOffset > 0;
		btnNextPage.enabled = pageOffset < (entries.length - 1) / buttonsPerPage;

		Iterator it = buttonList.iterator();
		while (it.hasNext()) {
			if (it.next() instanceof GuiButtonWaystone) {
				it.remove();
			}
		}

		int centerX = width / 2;
		int centerY = height / 2;
		int btnInitialX = centerX - 100;
		/**
		 * | per page | title | initialY | pagingY |
		 * | .....? 1 | . -52 | .... -27 | ... +07 |
		 * | .....? 2 | . -63 | .... -38 | ... +18 |
		 * | .....= 3 | . -74 | .... -49 | ... +29 |
		 * | ...... 4 | . -85 | .... -60 | ... +40 |
		 * | .....= 5 | . -96 | .... -71 | ... +51 |
		 */
		int btnInitialY = centerY - 27 - 11 * buttonsPerPage;

		int y = 0;
		int currDimensionId = Minecraft.getMinecraft().theWorld.provider.dimensionId;
		for (int i = 0; i < buttonsPerPage; i++) {
			int entryIndex = pageOffset * buttonsPerPage + i;
			if (entryIndex < 0 || entryIndex >= entries.length)
				continue;

			WaystoneEntry entry = entries[entryIndex];
			GuiButtonWaystone btnWaystone = new GuiButtonWaystone(2 + i, btnInitialX, btnInitialY + y, entry);
			if (entry.getDimensionId() != currDimensionId) {
				btnWaystone.enabled = WaystoneManager.isDimensionWarpAllowed(entry);
			}
			buttonList.add(btnWaystone);
			y += BUTTON_HEIGHT + 2;
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == btnNextPage) {
			pageOffset++;
			updateList();
		} else if (button == btnPrevPage) {
			pageOffset--;
			updateList();
		} else if (button instanceof GuiButtonWaystone) {
			NetworkHandler.channel
					.sendToServer(new MessageWarpStone(((GuiButtonWaystone) button).getWaystone(), isFree));
			mc.displayGuiScreen(null);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawWorldBackground(0);
		super.drawScreen(mouseX, mouseY, partialTicks);
		GL11.glColor4f(1f, 1f, 1f, 1f);

		int centerX = width / 2;
		int centerY = height / 2;
		int rectSize = 100;
		int rectSize_2 = rectSize / 2;

		drawRect(centerX - rectSize_2, centerY - rectSize_2, centerX + rectSize_2, centerY + rectSize_2, 0xFFFFFF);

		int titleY = centerY - 52 - 11 * buttonsPerPage;
		drawCenteredString(
				fontRendererObj, I18n.format("gui.waystones:warpStone.selectDestination"),
				centerX, titleY, 0xFFFFFF);
	}

}
