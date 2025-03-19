package com.imoonday.personalcloudstorage.client.screen.widget;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.client.ClientCloudStorage;
import com.imoonday.personalcloudstorage.client.ModConfigScreenFactory;
import com.imoonday.personalcloudstorage.client.PersonalCloudStorageClient;
import com.imoonday.personalcloudstorage.core.CloudStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CloudStorageSettingsComponent implements Renderable, GuiEventListener, LayoutElement, NarratableEntry {

    public static final ResourceLocation WIDGET_TEXTURE = PersonalCloudStorage.id("textures/gui/cloud_storage.png");
    private static final int SWITCH_BUTTON_SIZE = 11;
    public static boolean visible = true;
    private final CloudStorage.Settings settings = ClientCloudStorage.get().getSettings();
    private final Minecraft minecraft;
    private final Font font;
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final List<SettingSwitchingButton> switchingButtons = new ArrayList<>();
    private final List<Runnable> updateActions = new ArrayList<>();
    private int x;
    private int y;
    private int maxColumns;
    private int maxRows;
    private int width;
    private int height;

    public CloudStorageSettingsComponent(Minecraft minecraft, int x, int y, boolean isOwner) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
        this.x = x;
        this.y = y;
        this.init(isOwner);
    }

    private void init(boolean isOwner) {
        this.widgets.clear();
        this.maxRows = 0;
        this.maxColumns = 0;

        ToggleVisibilityButton switchButton = new ToggleVisibilityButton(this.x + 1, this.y, SWITCH_BUTTON_SIZE, SWITCH_BUTTON_SIZE, visible);
        switchButton.setTooltip(Tooltip.create(Component.translatable("widget.personalcloudstorage.settings_button.tooltip")));
        this.widgets.add(switchButton);

        if (isOwner) {
            this.addButton(0, 0, 0, 39, () -> settings.autoDownload, value -> settings.autoDownload = value, Component.translatable("settings.personalcloudstorage.autoDownload"));
            this.addButton(0, 1, 51, 39, () -> settings.autoUpload, value -> settings.autoUpload = value, Component.translatable("settings.personalcloudstorage.autoUpload"));
            this.addButton(0, 2, 102, 39, () -> settings.cycleThroughPages, value -> settings.cycleThroughPages = value, Component.translatable("settings.personalcloudstorage.cycleThroughPages"));
        } else {
            this.addButton(0, 0, 102, 39, () -> settings.cycleThroughPages, value -> settings.cycleThroughPages = value, Component.translatable("settings.personalcloudstorage.cycleThroughPages"));
        }

        this.width = 10 + 26 * this.maxColumns + 8;
        this.height = 20 + 26 * this.maxRows + 8;
    }

    private SettingSwitchingButton addButton(int row, int column, int u, int v, Supplier<Boolean> stateGetter, Consumer<Boolean> stateSetter, Component tooltip) {
        SettingSwitchingButton button = new SettingSwitchingButton(x + 10 + column * 26, getBgY() + 20 + row * 26, 24, 24, u, v, 26, 26, stateGetter, stateSetter);
        button.setSaveAction(() -> {
            update();
            ClientCloudStorage.get().syncSettings();
        });
        button.setTooltip(Tooltip.create(tooltip));
        button.visible = visible;
        this.widgets.add(button);
        this.switchingButtons.add(button);
        this.maxColumns = Math.max(this.maxColumns, column + 1);
        this.maxRows = Math.max(this.maxRows, row + 1);
        return button;
    }

    public void update() {
        for (Runnable action : updateActions) {
            action.run();
        }
    }

    public int getBgY() {
        return this.y + SWITCH_BUTTON_SIZE + 1;
    }

    public void addUpdateAction(Runnable action) {
        this.updateActions.add(action);
    }

    public void removeUpdateAction(Runnable action) {
        this.updateActions.remove(action);
    }

    public void toggleVisibility() {
        visible = !visible;
        for (SettingSwitchingButton button : switchingButtons) {
            button.visible = visible;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableDepthTest();
        if (visible) {
            renderBackground(guiGraphics);
            renderTitle(guiGraphics);
        }
        for (AbstractWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        RenderSystem.disableDepthTest();
    }

    private void renderBackground(GuiGraphics guiGraphics) {
        guiGraphics.blitNineSliced(WIDGET_TEXTURE, this.x, getBgY(), this.width, this.height, 8, 24, 24, 0, 103);
    }

    private void renderTitle(GuiGraphics guiGraphics) {
        Component title = Component.translatable("settings.personalcloudstorage.title");
        int titleWidth = this.font.width(title);
        int titleX = this.x + (this.width - titleWidth) / 2;
        if (titleWidth > 26 * this.maxColumns + 2) {
            title = Component.translatable("settings.personalcloudstorage.title.narrow");
            titleWidth = this.font.width(title);
            titleX = this.x + (this.width - titleWidth) / 2;
        }
        guiGraphics.drawString(this.font, title, titleX, getBgY() + 10, 4210752, false);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (AbstractWidget widget : widgets) {
            widget.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AbstractWidget widget : widgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (AbstractWidget widget : widgets) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (AbstractWidget widget : widgets) {
            if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (AbstractWidget widget : widgets) {
            if (widget.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : widgets) {
            if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : widgets) {
            if (widget.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (AbstractWidget widget : widgets) {
            if (widget.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return GuiEventListener.super.nextFocusPath(event);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (AbstractWidget widget : widgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void setFocused(boolean focused) {

    }


    @Override
    public boolean isFocused() {
        return false;
    }


    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        return GuiEventListener.super.getCurrentFocusPath();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(x, y, width, height);
    }


    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return visible ? width : 11;
    }

    @Override
    public int getHeight() {
        return visible ? height + 12 : 11;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        for (AbstractWidget widget : widgets) {
            consumer.accept(widget);
        }
    }

    class ToggleVisibilityButton extends StateSwitchingButton {

        public ToggleVisibilityButton(int x, int y, int width, int height, boolean initialState) {
            super(x, y, width, height, initialState);
            this.initTextureValues(0, 91, 12, 0, WIDGET_TEXTURE);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            super.onClick(mouseX, mouseY);
            toggleVisibility();
            this.setStateTriggered(CloudStorageSettingsComponent.visible);
            CloudStorageSettingsComponent.this.update();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (PersonalCloudStorageClient.clothConfig && this.active && this.visible && button == 1 && this.clicked(mouseX, mouseY)) {
                this.playDownSound(minecraft.getSoundManager());
                minecraft.setScreen(ModConfigScreenFactory.create(minecraft.screen));
                return true;
            }
            return false;
        }
    }
}
