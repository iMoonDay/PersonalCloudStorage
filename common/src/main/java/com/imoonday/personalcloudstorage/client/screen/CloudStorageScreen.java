package com.imoonday.personalcloudstorage.client.screen;

import com.imoonday.personalcloudstorage.api.CloudStorageListener;
import com.imoonday.personalcloudstorage.client.ClientConfig;
import com.imoonday.personalcloudstorage.client.ModKeys;
import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import com.imoonday.personalcloudstorage.client.screen.widget.CloudStorageSettingsComponent;
import com.imoonday.personalcloudstorage.core.CloudStorage;
import com.imoonday.personalcloudstorage.core.CloudStorageSettings;
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
    private static final MutableComponent CANNOT_ADD_PAGE_TEXT = Component.translatable("message.personalcloudstorage.cannot_add_page");
    private static final MutableComponent REMOVE_PAGE_TEXT = Component.translatable("message.personalcloudstorage.remove_page");
    private static final MutableComponent CANNOT_DELETE_TEXT = Component.translatable("message.personalcloudstorage.at_least_one");
    private final Player player;
    private final int containerRows;
    private CloudStorageSettingsComponent settingsComponent;
    private PageButton prevPageButton;
    private PageButton nextPageButton;
    private Button addButton;
    private Button removeButton;

    public CloudStorageScreen(CloudStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.player = inventory.player;
        this.containerRows = menu.getContainerRows();
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
        this.menu.setOnUpdate(this::onUpdate);
    }

    @Override
    public void onUpdate() {
        CloudStorage cloudStorage = this.menu.getCloudStorage();
        int totalPages = cloudStorage.getTotalPages();
        CloudStorageSettings settings = cloudStorage.getSettings();
        boolean multiPages = totalPages > 1;
        int currentPage = this.menu.getCurrentPage();

        int buttonY = calculatePageTurnButtonY();

        if (this.prevPageButton != null) {
            this.prevPageButton.setY(buttonY);
            this.prevPageButton.visible = multiPages && (settings.cycleThroughPages || currentPage > 0);
        }
        if (this.nextPageButton != null) {
            this.nextPageButton.setY(buttonY);
            this.nextPageButton.visible = multiPages && (settings.cycleThroughPages || currentPage < totalPages - 1);
        }
        this.updateAddButton();
        if (this.removeButton != null) {
            this.removeButton.active = multiPages;
            this.removeButton.setTooltip(Tooltip.create(multiPages ? REMOVE_PAGE_TEXT : CANNOT_DELETE_TEXT));
        }
    }

    protected int calculatePageTurnButtonY() {
        int buttonY = (this.height - 13) / 2;
        if (this.settingsComponent != null) {
            int componentBottom = this.settingsComponent.getY() + this.settingsComponent.getHeight();
            if (buttonY - 10 < componentBottom) {
                buttonY = componentBottom + 10;
            }
        }
        return buttonY;
    }

    private void updateAddButton() {
        if (this.addButton != null) {
            boolean active = this.menu.checkAddAvailable(this.player, true);
            this.addButton.active = active;
            this.addButton.setTooltip(Tooltip.create(active ? ADD_PAGE_TEXT : CANNOT_ADD_PAGE_TEXT));
        }
    }

    @Override
    protected void init() {
        super.init();
        boolean multiPages = this.menu.getCloudStorage().getTotalPages() > 1;

        ClientConfig config = ClientConfig.get();

        this.settingsComponent = new CloudStorageSettingsComponent(this.minecraft, this.leftPos + this.imageWidth + config.settingsComponentOffsetX, this.topPos + 1 + config.settingsComponentOffsetY, this.menu.isOwnCloudStorage());
        this.settingsComponent.addUpdateAction(this::onUpdate);
        this.addRenderableWidget(this.settingsComponent);

        if (!config.hidePageTurnButton) {
            int buttonY = calculatePageTurnButtonY();

            this.prevPageButton = new PageButton(this.leftPos - 23 - 5, buttonY, false, button -> {
                pressButton(CloudStorageMenu.PREVIOUS_PAGE_BUTTON_ID);
            }, true);
            this.addRenderableWidget(this.prevPageButton);

            this.nextPageButton = new PageButton(this.leftPos + this.imageWidth + 5, buttonY, true, button -> {
                pressButton(CloudStorageMenu.NEXT_PAGE_BUTTON_ID);
            }, true);
            this.addRenderableWidget(this.nextPageButton);
        }

        if (!this.menu.disallowModification(this.player)) {
            int size = MODIFICATION_BUTTON_SIZE;
            this.addButton = Button.builder(Component.literal("+"), button -> pressButton(CloudStorageMenu.ADD_PAGE_BUTTON_ID))
                                   .bounds(this.leftPos + this.imageWidth - 7 - size + config.pageModificationButtonOffsetX, this.topPos + 5 + config.pageModificationButtonOffsetY, size, size)
                                   .tooltip(Tooltip.create(this.menu.checkAddAvailable(this.player, true) ? ADD_PAGE_TEXT : CANNOT_ADD_PAGE_TEXT))
                                   .build();
            this.addRenderableWidget(this.addButton);

            this.removeButton = Button.builder(Component.literal("-"), button -> pressButton(hasShiftDown() ? CloudStorageMenu.REMOVE_PAGE_FORCED_BUTTON_ID : CloudStorageMenu.REMOVE_PAGE_BUTTON_ID))
                                      .bounds(addButton.getX() - 2 - size, addButton.getY(), size, size)
                                      .tooltip(Tooltip.create(multiPages ? REMOVE_PAGE_TEXT : CANNOT_DELETE_TEXT))
                                      .build();
            this.addRenderableWidget(this.removeButton);
        }

        this.onUpdate();
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
            int y = this.prevPageButton.getY() - 10;
            guiGraphics.drawCenteredString(font, message, centerX, y, 0xFFFFFF);
        }
        if (this.nextPageButton != null && this.nextPageButton.visible) {
            Component message = getMessageWithKey(ModKeys.NEXT_PAGE.getKeyMapping());
            int centerX = this.nextPageButton.getX() + this.nextPageButton.getWidth() / 2;
            int y = this.nextPageButton.getY() - 10;
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
    protected void containerTick() {
        super.containerTick();
        this.updateAddButton();
    }

    private void playPageTurnSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    private void pressButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }
}
