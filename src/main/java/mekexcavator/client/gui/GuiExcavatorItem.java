package mekexcavator.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.button.GuiDisableableButton;
import mekanism.client.gui.element.*;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekexcavator.common.inventory.container.ContainerExcavatorItem;
import mekexcavator.common.tile.TileEntityExcavatorItem;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.Arrays;

@SideOnly(Side.CLIENT)
public class GuiExcavatorItem extends GuiMekanismTile<TileEntityExcavatorItem> {

    private GuiDisableableButton EjectButton;

    public GuiExcavatorItem(InventoryPlayer inventory, TileEntityExcavatorItem tile) {
        super(tile, new ContainerExcavatorItem(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> {
            String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.energyPerTick);
            return Arrays.asList(LangUtils.localize("gui.using") + ": " + multiplier + "/t",
                    LangUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy() - tileEntity.getEnergy()));
        }, this, resource));
        addGuiElement(new GuiSlot(GuiSlot.SlotType.INPUT, this, resource, 106, 43).with(GuiSlot.SlotOverlay.INPUT));
        addGuiElement(new GuiSlot(GuiSlot.SlotType.POWER, this, resource, 151, 6).with(GuiSlot.SlotOverlay.POWER));
        addGuiElement(new GuiInnerScreen(this, resource, 7, 19, 78, 69));
        addGuiElement(new GuiPlayerSlot(this, resource, 7, 159));
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addGuiElement(new GuiSlot(GuiSlot.SlotType.NORMAL, this, resource, 7 + x * 18, 91 + y * 18));
            }
        }
        addGuiElement(new GuiProgress(new GuiProgress.IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getScaledProgress();
            }
        }, GuiProgress.ProgressBar.DOWN, this, resource, 109, 65));
        addGuiElement(new GuiEnergyGauge(() -> tileEntity, GuiEnergyGauge.Type.STANDARD, this, resource, 151, 28) {
            @Override
            public String getTooltipText() {
                return MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy());
            }
        });
        ySize += 76;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(EjectButton = new GuiDisableableButton(0, guiLeft + 88, guiTop + 19, 61, 18, LangUtils.localize("gui.autoEject")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2), 4, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.dimensionId") + ":" + tileEntity.getWorld().provider.getDimension(), 8, 20, 0x33ff99);
        fontRenderer.drawString(LangUtils.localize("gui.dimensionName") + ":", 8, 29, 0x33ff99);
        fontRenderer.drawString(tileEntity.getWorld().provider.getDimensionType().getName(), 8, 38, 0x33ff99);
        fontRenderer.drawString(LangUtils.localize("gui.eject") + ":" + LangUtils.transOnOff(tileEntity.doEject), 8, 47, 0x33ff99);
        if (tileEntity.ContainsOreVeins) {
            fontRenderer.drawString(LangUtils.localize("gui.chunkOre"), 8, 56, 0x33ff99);
            fontRenderer.drawString(LangUtils.localize("gui.chunkOreMined") + ":" + (tileEntity.mineralDepletion == -1 ? LangUtils.localize("gui.infinite") : tileEntity.mineralDepletion), 8, 65, 0x33ff99);
            fontRenderer.drawString(LangUtils.localize("gui.chunkVeinSize") + ":" + (tileEntity.mineralVeinCapacity == -1 ? LangUtils.localize("gui.infinite") : tileEntity.mineralVeinCapacity), 8, 74, 0x33ff99);
        } else {
            fontRenderer.drawString(LangUtils.localize("gui.chunkNoneOre"), 8, 56, 0x33ff99);
        }

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        boolean energy = tileEntity.getEnergy() < tileEntity.energyPerTick || tileEntity.getEnergy() == 0;
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                mc.getTextureManager().bindTexture(MekanismUtils.getResource(MekanismUtils.ResourceType.SLOT, "Slot_Icon.png"));
                if (tileEntity.inventory.get(slotX + slotY * 9).getCount() == tileEntity.inventory.get(slotX + slotY * 9).getMaxStackSize()) {
                    drawTexturedModalRect(guiLeft + 7 + slotX * 18, guiTop + 91 + slotY * 18, 158, 0, 18, 18);
                }
            }
        }
        if (energy) {
            mc.getTextureManager().bindTexture(MekanismUtils.getResource(MekanismUtils.ResourceType.TAB, "Warning_Info.png"));
            drawTexturedModalRect(guiLeft - 26, guiTop + 112, 0, 0, 26, 26);
            addGuiElement(new GuiWarningInfo(this, getGuiLocation(), false));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button == EjectButton) {
            TileNetworkList data = TileNetworkList.withContents(0);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity.TileEntityMessage(tileEntity, data));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }
}
