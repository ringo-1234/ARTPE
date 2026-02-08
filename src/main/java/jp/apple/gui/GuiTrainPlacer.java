package jp.apple.gui;

import jp.apple.ARTPECore;
import jp.apple.network.PacketFinishEditing;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import java.io.IOException;

public class GuiTrainPlacer extends GuiContainer {
    private final ContainerTrainPlacer container;

    private static final int SLOT_W = 50;
    private static final int SLOT_H = 20;
    private static final int MARGIN = 5;
    private static final int MAX_COLS = 4;

    public GuiTrainPlacer(ContainerTrainPlacer inventorySlotsIn) {
        super(inventorySlotsIn);
        this.container = inventorySlotsIn;
        this.xSize = 240;
        this.ySize = 140;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.refreshButtons();
    }

    private void refreshButtons() {
        this.buttonList.clear();
        int listSize = this.container.tile.trainModels.size();

        for (int i = 0; i < listSize; i++) {
            int row = i / MAX_COLS;
            int col = i % MAX_COLS;
            int x = this.guiLeft + 15 + col * (SLOT_W + MARGIN);
            int y = this.guiTop + 20 + row * (SLOT_H + MARGIN);

            String modelName = this.container.tile.trainModels.get(i);
            if (modelName.isEmpty()) modelName = "未選択";

            
            String dirLabel = (this.container.tile.trainDirs.get(i) == 0) ? "[前] " : "[後] ";
            this.buttonList.add(new GuiButton(i, x, y, SLOT_W, SLOT_H, dirLabel + modelName));
        }

        int nextIndex = listSize;
        int pX = this.guiLeft + 15 + (nextIndex % MAX_COLS) * (SLOT_W + MARGIN);
        int pY = this.guiTop + 20 + (nextIndex / MAX_COLS) * (SLOT_H + MARGIN);
        this.buttonList.add(new GuiButton(100, pX, pY, 20, 20, "+"));
        this.buttonList.add(new GuiButton(200, this.guiLeft + this.xSize - 70, this.guiTop + this.ySize - 30, 60, 20, "出力"));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        
        if (mouseButton == 1) {
            for (GuiButton button : this.buttonList) {
                if (button.mousePressed(this.mc, mouseX, mouseY) && button.id < 100) {
                    
                    int currentDir = this.container.tile.trainDirs.get(button.id);
                    this.container.tile.trainDirs.set(button.id, currentDir == 0 ? 1 : 0);
                    button.playPressSound(this.mc.getSoundHandler());
                    this.refreshButtons();
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 100) {
            this.container.tile.addEmptySlot();
            this.refreshButtons();
        } else if (button.id == 200) {
            
            ARTPECore.network.sendToServer(new PacketFinishEditing(this.container.tile.trainModels, this.container.tile.trainDirs));
            this.mc.displayGuiScreen(null);
        } else if (button.id < 100) {
            this.container.tile.editingIndex = button.id;
            this.mc.displayGuiScreen(new GuiSelectModelFilter(this.mc.world, this.container.tile));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xCC000000);
        this.fontRenderer.drawString("編成エディタ (右クリックで向き変換)", guiLeft + 10, guiTop + 8, 0xFFFFFF);
    }
}