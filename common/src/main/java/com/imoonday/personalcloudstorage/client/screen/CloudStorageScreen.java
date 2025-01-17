package com.imoonday.personalcloudstorage.client.screen;

import com.imoonday.personalcloudstorage.api.CloudStorageListener;
import com.imoonday.personalcloudstorage.client.ClientConfig;
import com.imoonday.personalcloudstorage.client.ModKeys;
import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import com.imoonday.personalcloudstorage.client.screen.widget.CloudStorageSettingsComponent;
import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.component.CloudStorageSettings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class CloudStorageScreen extends AbstractContainerScreen<CloudStorageMenu> implements CloudStorageListener {

    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int MODIFICATION_BUTTON_SIZE = 10;
    private static final MutableComponent ADD_PAGE_TEXT = Component.translatable("message.personalcloudstorage.add_page");
    private static final MutableComponent REMOVE_PAGE_TEXT = Component.translatable("message.personalcloudstorage.remove_page");
    private static final MutableComponent CANNOT_DELETE_TEXT = Component.translatable("message.personalcloudstorage.at_least_one");
    private final Player player;
    private final int containerRows;
    private PageButton prevPageButton;
    private PageButton nextPageButton;
    private Button removeButton;

    public CloudStorageScreen(CloudStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.player = inventory.player;
        this.containerRows = menu.getContainerRows();
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
        this.menu.setOnDataChange(this::onUpdate);
    }

    @Override
    protected void init() {
        super.init();
        boolean multiPages = this.menu.getCloudStorage().getTotalPages() > 1;

        ClientConfig config = ClientConfig.get();
        if (!config.hidePageTurnButton) {
            this.prevPageButton = new PageButton(this.leftPos - 23 - 5, (this.height - 13) / 2, false, button -> {
                pressButton(CloudStorageMenu.PREVIOUS_PAGE_BUTTON_ID);
            }, true);
            this.addRenderableWidget(this.prevPageButton);

            this.nextPageButton = new PageButton(this.leftPos + this.imageWidth + 5, (this.height - 13) / 2, true, button -> {
                pressButton(CloudStorageMenu.NEXT_PAGE_BUTTON_ID);
            }, true);
            this.addRenderableWidget(this.nextPageButton);
        }

        if (!this.menu.disallowModification(this.player)) {
            int size = MODIFICATION_BUTTON_SIZE;
            Button addButton = Button.builder(Component.literal("+"), button -> pressButton(CloudStorageMenu.ADD_PAGE_BUTTON_ID))
                                     .bounds(this.leftPos + this.imageWidth - 7 - size + config.pageModificationButtonOffsetX, this.topPos + 5 + config.pageModificationButtonOffsetY, size, size)
                                     .tooltip(Tooltip.create(ADD_PAGE_TEXT))
                                     .build();
            this.addRenderableWidget(addButton);

            this.removeButton = Button.builder(Component.literal("-"), button -> pressButton(hasShiftDown() ? CloudStorageMenu.REMOVE_PAGE_FORCED_BUTTON_ID : CloudStorageMenu.REMOVE_PAGE_BUTTON_ID))
                                      .bounds(addButton.getX() - 2 - size, addButton.getY(), size, size)
                                      .tooltip(Tooltip.create(multiPages ? REMOVE_PAGE_TEXT : CANNOT_DELETE_TEXT))
                                      .build();
            this.addRenderableWidget(this.removeButton);
        }

        this.onUpdate();

        CloudStorageSettingsComponent component = new CloudStorageSettingsComponent(this.minecraft, this.leftPos + this.imageWidth, this.topPos + 1);
        component.addUpdateAction(this::onUpdate);
        this.addRenderableWidget(component);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderPageTurnKeys(guiGraphics);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    protected void renderPageTurnKeys(GuiGraphics guiGraphics) {
        if (ClientConfig.get().hidePageTurnButton || ClientConfig.get().hidePageTurnKeyName) return;

        if (this.prevPageButton != null && this.prevPageButton.visible) {
            Component message = getMessageWithKey(ModKeys.PREVIOUS_PAGE.getKeyMapping());
            int centerX = this.prevPageButton.getX() + this.prevPageButton.getWidth() / 2;
            int y = this.prevPageButton.getY() - MODIFICATION_BUTTON_SIZE;
            guiGraphics.drawCenteredString(font, message, centerX, y, 0xFFFFFF);
        }
        if (this.nextPageButton != null && this.nextPageButton.visible) {
            Component message = getMessageWithKey(ModKeys.NEXT_PAGE.getKeyMapping());
            int centerX = this.nextPageButton.getX() + this.nextPageButton.getWidth() / 2;
            int y = this.nextPageButton.getY() - MODIFICATION_BUTTON_SIZE;
            guiGraphics.drawCenteredString(font, message, centerX, y, 0xFFFFFF);
        }
    }

    public static MutableComponent getMessageWithKey(KeyMapping keyMapping) {
        return Component.literal("[").append(keyMapping.getTranslatedKeyMessage()).append("]");
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        guiGraphics.blit(CONTAINER_BACKGROUND, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
        int page = this.menu.getCurrentPage();
        int totalPages = this.menu.getCloudStorage().getTotalPages();
        Component text = Component.literal((page + 1) + "/" + totalPages);
        guiGraphics.drawString(font, text, (this.width - font.width(text)) / 2, this.topPos + this.inventoryLabelY, 4210752, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeys.PREVIOUS_PAGE.matches(keyCode, scanCode)) {
            pressButton(CloudStorageMenu.PREVIOUS_PAGE_BUTTON_ID);
            if (this.prevPageButton != null && this.prevPageButton.visible) {
                playPageTurnSound();
            }
            return true;
        } else if (ModKeys.NEXT_PAGE.matches(keyCode, scanCode)) {
            pressButton(CloudStorageMenu.NEXT_PAGE_BUTTON_ID);
            if (this.nextPageButton != null && this.nextPageButton.visible) {
                playPageTurnSound();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ModKeys.PREVIOUS_PAGE.matchesMouse(button)) {
            pressButton(CloudStorageMenu.PREVIOUS_PAGE_BUTTON_ID);
            playPageTurnSound();
            return true;
        } else if (ModKeys.NEXT_PAGE.matchesMouse(button)) {
            pressButton(CloudStorageMenu.NEXT_PAGE_BUTTON_ID);
            playPageTurnSound();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void pressButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    private void playPageTurnSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    @Override
    public void onUpdate() {
        CloudStorage cloudStorage = this.menu.getCloudStorage();
        int totalPages = cloudStorage.getTotalPages();
        CloudStorageSettings settings = cloudStorage.getSettings();
        boolean multiPages = totalPages > 1;
        int currentPage = this.menu.getCurrentPage();
        if (this.prevPageButton != null) {
            this.prevPageButton.visible = multiPages && (settings.cycleThroughPages || currentPage > 0);
        }
        if (this.nextPageButton != null) {
            this.nextPageButton.visible = multiPages && (settings.cycleThroughPages || currentPage < totalPages - 1);
        }
        if (this.removeButton != null) {
            this.removeButton.active = multiPages;
            this.removeButton.setTooltip(Tooltip.create(multiPages ? REMOVE_PAGE_TEXT : CANNOT_DELETE_TEXT));
        }
    }
}
