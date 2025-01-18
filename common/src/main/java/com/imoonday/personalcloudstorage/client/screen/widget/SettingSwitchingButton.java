package com.imoonday.personalcloudstorage.client.screen.widget;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingSwitchingButton extends StateSwitchingButton {

    public static final ResourceLocation TEXTURE = PersonalCloudStorage.id("textures/gui/cloud_storage.png");
    protected final Supplier<Boolean> stateGetter;
    protected final Consumer<Boolean> stateSetter;
    @Nullable
    private Runnable saveAction;

    public SettingSwitchingButton(int x, int y, int width, int height, int u, int v, int uOffset, int vOffset, Supplier<Boolean> stateGetter, Consumer<Boolean> stateSetter) {
        super(x, y, width, height, stateGetter.get());
        this.stateGetter = stateGetter;
        this.stateSetter = stateSetter;
        this.initTextureValues(u, v, uOffset, vOffset, TEXTURE);
    }

    public void setSaveAction(@Nullable Runnable saveAction) {
        this.saveAction = saveAction;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.setStateTriggered(!isStateTriggered());
    }

    public void save() {
        if (saveAction != null) {
            saveAction.run();
        }
    }

    @Override
    public boolean isStateTriggered() {
        return stateGetter.get();
    }

    @Override
    public void setStateTriggered(boolean triggered) {
        super.setStateTriggered(triggered);
        stateSetter.accept(triggered);
        this.save();
    }
}
