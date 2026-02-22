package baubles.client.gui;

import baubles.api.BaublesApi;
import baubles.api.cap.BaublesContainer;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.inv.SlotDefinition;
import baubles.client.ClientProxy;
import baubles.common.Baubles;
import baubles.common.container.ContainerPlayerExpanded;
import baubles.common.integration.ModCompatibility;
import com.google.common.collect.Ordering;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.ModConfigs;
import lain.mods.cos.client.GuiCosArmorButton;
import lain.mods.cos.client.GuiCosArmorToggleButton;
import lain.mods.cos.client.PlayerRenderHandler;
import lain.mods.cos.network.packet.PacketOpenCosArmorInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import static baubles.common.integration.ModCompatibility.CA;
import static baubles.common.integration.ModCompatibility.ME$shouldMoveLeft;

public class GuiPlayerExpanded extends InventoryEffectRenderer {

    public static final ResourceLocation background =
            new ResourceLocation(Baubles.MODID, "textures/gui/baubles_inventory.png");
    private static final int BAUBLE_COLUMN_STEP = 18;
        private static final int BAUBLE_LEFT_BORDER_WIDTH = 4;
        private static final int BAUBLE_INTERIOR_WIDTH = 18;
        private static final int BAUBLE_RIGHT_BORDER_WIDTH = 6;

    private static final boolean ENABLE_RECIPE_BOOK = !ModCompatibility.RecipeBook$isDisabled();
    private static final Field REF_OLD_MOUSE_X, REF_OLD_MOUSE_Y; // in GuiInventory to retain mouse positions when you close baubles gui
    private static final Method REF_ACTION_PERFORMED; // in GuiInventory for recipe book

    static {
        boolean deobfEnv = FMLLaunchHandler.isDeobfuscatedEnvironment();

        try {
            REF_OLD_MOUSE_X = GuiInventory.class.getDeclaredField(deobfEnv ? "oldMouseX" : "field_147048_u");
            REF_OLD_MOUSE_Y = GuiInventory.class.getDeclaredField(deobfEnv ? "oldMouseY" : "field_147047_v");

            REF_OLD_MOUSE_X.setAccessible(true);
            REF_OLD_MOUSE_Y.setAccessible(true);

            if (ENABLE_RECIPE_BOOK) {
                REF_ACTION_PERFORMED = GuiInventory.class.getDeclaredMethod(deobfEnv ? "actionPerformed" : "func_146284_a", GuiButton.class);
                REF_ACTION_PERFORMED.setAccessible(true);
            }
            else REF_ACTION_PERFORMED = null;

        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final EntityPlayer player;
    private final IBaublesItemHandler baublesHandler = ((ContainerPlayerExpanded) this.inventorySlots).baubles;
    protected GuiButtonImage recipeBook;
    protected GuiButton cosButton, cosToggleButton;
    private float oldMouseX, oldMouseY;

    public GuiPlayerExpanded(EntityPlayer player) {
        super(new ContainerPlayerExpanded(player.inventory, player));
        this.allowUserInput = true;
        this.player = player;
    }

    private void resetGuiLeft() {
        int centeredGuiLeft = (this.width - this.xSize) / 2;
        int columns = this.getBaubleColumnCount();
        int leftWidth = columns > 0 ? 28 + ((columns - 1) * BAUBLE_COLUMN_STEP) : 0;
        int leftMostBaublePanelX = centeredGuiLeft - leftWidth;
        int minLeftPadding = 4;
        int shiftRight = Math.max(0, minLeftPadding - leftMostBaublePanelX);

        this.guiLeft = centeredGuiLeft + shiftRight;
        this.updateExtraButtonPositions();
    }

    private int getBaubleColumnCount() {
        int maxSlots = this.getRealBaubleSlots();
        if (maxSlots <= 0) return 0;
        int maxRowsPerColumn = this.getActualMaxBaubleSlots();
        return (maxSlots + maxRowsPerColumn - 1) / maxRowsPerColumn;
    }

    private void updateExtraButtonPositions() {
        if (this.recipeBook != null) {
            this.recipeBook.x = this.guiLeft + 104;
            this.recipeBook.y = this.height / 2 - 22;
        }

        if (this.cosButton != null) {
            this.cosButton.x = this.guiLeft + ModConfigs.CosArmorGuiButton_Left;
            this.cosButton.y = this.guiTop + ModConfigs.CosArmorGuiButton_Top;
        }

        if (this.cosToggleButton != null) {
            this.cosToggleButton.x = this.guiLeft + ModConfigs.CosArmorToggleButton_Left;
            this.cosToggleButton.y = this.guiTop + ModConfigs.CosArmorToggleButton_Top;
        }
    }

    @Override
    public void updateScreen() {
        updateActivePotionEffects();
        resetGuiLeft();
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        super.initGui();

        if (ENABLE_RECIPE_BOOK) {
            this.initRecipeBook();
            this.buttonList.add(this.recipeBook);
        }

        if (Loader.isModLoaded(CA)) {
            this.initCosButtons();
            this.buttonList.add(this.cosButton);
            this.buttonList.add(this.cosToggleButton);
        }

        resetGuiLeft();
    }

    private void initRecipeBook() {
        this.recipeBook = new GuiButtonImage(10, this.guiLeft + 104, this.height / 2 - 22, 20, 18, 178, 0, 19, INVENTORY_BACKGROUND);
    }

    @Optional.Method(modid = CA)
    private void initCosButtons() {
        if (!ModConfigs.CosArmorGuiButton_Hidden) {
            this.cosButton = new GuiCosArmorButton(58, this.guiLeft + ModConfigs.CosArmorGuiButton_Left, this.guiTop + ModConfigs.CosArmorGuiButton_Top, 10, 10, "cos.gui.buttoncos") {
                @Override
                public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
                    boolean pressed = super.mousePressed(mc, mouseX, mouseY);
                    if (pressed) {
                        CosmeticArmorReworked.network.sendToServer(new PacketOpenCosArmorInventory());
                    }
                    return pressed;
                }
            };
        }
        if (!ModConfigs.CosArmorToggleButton_Hidden) {
            GuiCosArmorToggleButton toggleButton = new GuiCosArmorToggleButton(59, this.guiLeft + ModConfigs.CosArmorToggleButton_Left, this.guiTop + ModConfigs.CosArmorToggleButton_Top, 5, 5, "") {
                @Override
                public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
                    boolean pressed = super.mousePressed(mc, mouseX, mouseY);
                    if (pressed) {
                        PlayerRenderHandler.HideCosArmor = !PlayerRenderHandler.HideCosArmor;
                        this.state = PlayerRenderHandler.HideCosArmor ? 1 : 0;
                    }
                    return pressed;
                }
            };
            toggleButton.state = PlayerRenderHandler.HideCosArmor ? 1 : 0;
            this.cosToggleButton = toggleButton;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.crafting"), 97, 8, 4210752);
        int slotIndex = this.getHoveredBaubleSlotIndex(mouseX, mouseY);
        if (slotIndex >= 0) {
            BaublesContainer container = ((BaublesContainer) baublesHandler);

            ItemStack stack = container.getStackInSlot(slotIndex);
            if (!stack.isEmpty()) return;

            SlotDefinition definition = container.getSlot(slotIndex);

            if (definition != null) {

                FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0, 200);
                String str = I18n.format(definition.getTranslationKey(slotIndex));
                GuiUtils.drawHoveringText(Collections.singletonList(str), mouseX - this.guiLeft, mouseY - this.guiTop, width, height, 300, renderer);
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        resetGuiLeft();
        this.drawDefaultBackground();
        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);

        int k = this.guiLeft;
        int l = this.guiTop;

        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        int maxSlots = this.getRealBaubleSlots();

        if (maxSlots > 0) {
            int maxRowsPerColumn = this.getActualMaxBaubleSlots();
            int columns = (maxSlots + maxRowsPerColumn - 1) / maxRowsPerColumn;
            int[] columnRows = new int[columns];

            for (int column = 0; column < columns; column++) {
                columnRows[column] = Math.min(maxRowsPerColumn, maxSlots - (column * maxRowsPerColumn));
            }

            for (int column = 0; column < columns; column++) {
                int x = k - 28 - (column * BAUBLE_COLUMN_STEP);
                int rows = columnRows[column];

                if (rows == 1) {
                    boolean leftNeighborHasRow = column + 1 < columns && 0 < columnRows[column + 1];
                    boolean rightNeighborHasRow = column - 1 >= 0 && 0 < columnRows[column - 1];
                    this.drawBaubleColumnPart(x, l, 34, 28, !leftNeighborHasRow, !rightNeighborHasRow);
                    continue;
                }

                for (int i = 0; i < rows; i++) {
                    int textureY = 39;
                    int height = 20;
                    int y = l + (i * 18);

                    if (i == 0) {
                        textureY = 34;
                        height += 4;
                    }
                    else y += 5;
                    if (i == rows - 1) height += 4;

                    boolean leftNeighborHasRow = column + 1 < columns && i < columnRows[column + 1];
                    boolean rightNeighborHasRow = column - 1 >= 0 && i < columnRows[column - 1];

                    this.drawBaubleColumnPart(x, y, textureY, height, !leftNeighborHasRow, !rightNeighborHasRow);
                }
            }
        }

        GuiInventory.drawEntityOnScreen(k + 51, l + 75, 30, (float) (k + 51) - this.oldMouseX, (float) (l + 75 - 50) - this.oldMouseY, this.mc.player);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1: // Stats button
                this.mc.displayGuiScreen(new GuiStats(this, this.mc.player.getStatFileWriter()));
                break;
            case 10: // Recipe Book Button
                this.openInventoryWithRecipeBook(new GuiInventory(this.player));
                break;
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException {
        if (par2 == ClientProxy.KEY_BAUBLES.getKeyCode()) {
            this.mc.player.closeScreen();
        } else
            super.keyTyped(par1, par2);
    }

    @Override
    protected void updateActivePotionEffects() {
        boolean hasVisibleEffect = false;
        for (PotionEffect potioneffect : this.mc.player.getActivePotionEffects()) {
            Potion potion = potioneffect.getPotion();
            if (potion.shouldRender(potioneffect)) {
                hasVisibleEffect = true;
                break;
            }
        }
        if (this.mc.player.getActivePotionEffects().isEmpty() || !hasVisibleEffect) {
            this.guiLeft = (this.width - this.xSize) / 2;
            this.hasActivePotionEffects = false;
        } else {
            this.hasActivePotionEffects = true;
        }
    }


    @Override
    protected void drawActivePotionEffects() {
        boolean moveLeft = ME$shouldMoveLeft(this);
        int i = this.guiLeft;
        if (moveLeft) guiLeft -= 27;
        super.drawActivePotionEffects();
        guiLeft = i;
    }

    public void displayNormalInventory() {
        GuiInventory gui = new GuiInventory(this.mc.player);

        try {
            REF_OLD_MOUSE_Y.set(gui, this.oldMouseX);
            REF_OLD_MOUSE_Y.set(gui, this.oldMouseY);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        this.mc.displayGuiScreen(gui);
    }

    private void openInventoryWithRecipeBook(GuiInventory inventory) {
        this.mc.displayGuiScreen(inventory);
        if (!inventory.func_194310_f().isVisible()) {
            try {
                assert REF_ACTION_PERFORMED != null;
                REF_ACTION_PERFORMED.invoke(inventory, recipeBook);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int getMaxY() {
        return 18 * Math.min(this.getRealBaubleSlots(), this.getActualMaxBaubleSlots());
    }

    private int getHoveredBaubleSlotIndex(int mouseX, int mouseY) {
        int maxSlots = this.getRealBaubleSlots();
        int maxRowsPerColumn = this.getActualMaxBaubleSlots();

        for (int slotIndex = 0; slotIndex < maxSlots; slotIndex++) {
            int column = slotIndex / maxRowsPerColumn;
            int row = slotIndex % maxRowsPerColumn;

            int xLoc = this.guiLeft - 24 - (column * BAUBLE_COLUMN_STEP);
            int yLoc = this.guiTop + 5 + (row * 18);

            if (mouseX > xLoc && mouseX < xLoc + 19 && mouseY >= yLoc && mouseY < yLoc + 18) {
                return slotIndex;
            }
        }

        return -1;
    }

    private void drawBaubleColumnPart(int x, int y, int textureY, int height, boolean drawLeftBorder, boolean drawRightBorder) {
        this.drawTexturedModalRect(x + BAUBLE_LEFT_BORDER_WIDTH, y, 180, textureY, BAUBLE_INTERIOR_WIDTH, height);

        if (drawLeftBorder) {
            this.drawTexturedModalRect(x, y, 176, textureY, BAUBLE_LEFT_BORDER_WIDTH, height);
        }

        if (drawRightBorder) {
            this.drawTexturedModalRect(x + BAUBLE_LEFT_BORDER_WIDTH + BAUBLE_INTERIOR_WIDTH, y, 198, textureY, BAUBLE_RIGHT_BORDER_WIDTH, height);
        }
    }

    public int getBaubleSlots() {
//        int slotNum = 0;
//        for (int i = 0; i < baublesHandler.getSlots(); i++) {
//            if (baublesHandler.getSlot(i) != null) {
//                slotNum += 1;
//            }
//        }
//        return slotNum;
        return this.baublesHandler.getSlots();
    }

    public int getRealBaubleSlots() {
        int slotNum = 0;
        for (int i = 0; i < baublesHandler.getSlots(); i++) {
            if (baublesHandler.getRealSlot(i) != null) {
                slotNum += 1;
            }
        }
        return slotNum;
    }

    public int getMaxBaubleSlots() {
        return Math.min(baublesHandler.getSlots(), this.getActualMaxBaubleSlots());
    }

    public int getActualMaxBaubleSlots() {
        return 8;
    }
}
