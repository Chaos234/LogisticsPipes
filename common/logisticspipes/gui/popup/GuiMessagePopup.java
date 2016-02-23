package logisticspipes.gui.popup;

import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.gui.GuiButton;

public class GuiMessagePopup extends SubGuiScreen {

	private String[] text;
	private int mWidth = 0;

	public GuiMessagePopup(Object... message) {
		super(200, (message.length * 10) + 40, 0, 0);
		text = new String[message.length];
		int i = 0;
		for (Object o : message) {
			if (o instanceof Object[]) {
				for (Object oZwei : (Object[]) o) {
					text[i++] = oZwei.toString();
				}
			} else {
				text[i++] = o.toString();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, xCenter - 25, bottom - 25, 50, 20, "OK"));
	}

	@Override
	protected void renderGuiBackground(int par1, int par2) {
		if (mWidth == 0) {
			int lWidth = 0;
			for (String msg : text) {
				int tWidth = mc.fontRendererObj.getStringWidth(msg);
				if (tWidth > lWidth) {
					lWidth = tWidth;
				}
			}
			xSize = mWidth = Math.max(Math.min(lWidth + 20, 400), 120);
			super.initGui();
		}
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		for (int i = 0; i < 9 && i < text.length; i++) {
			if (text[i] == null) {
				continue;
			}
			String msg = StringUtils.getCuttedString(text[i], mWidth - 10, mc.fontRendererObj);
			int stringWidth = mc.fontRendererObj.getStringWidth(msg);
			mc.fontRendererObj.drawString(msg, xCenter - (stringWidth / 2), guiTop + 10 + (i * 10), 0x404040);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch (guibutton.id) {
			case 0:
				super.exitGui();
				break;
		}
	}
}
