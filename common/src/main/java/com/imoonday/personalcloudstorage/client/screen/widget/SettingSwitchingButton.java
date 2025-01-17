package com.imoonday.personalcloudstorage.client.screen.widget;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingSwitchingButton extends StateSwitchingButton {

    public static final ResourceLocation TEXTURE = PersonalCloudStorage.id("textures/gui/cloud_storage.png");
    protected StateData stateData;
    @Nullable
    private Runnable saveAction;

    public SettingSwitchingButton(int x, int y, int width, int height, int u, int v, int uOffset, int vOffset, StateData stateData) {
        super(x, y, width, height, stateData.get());
        this.stateData = stateData;
        this.initTextureValues(u, v, uOffset, vOffset, TEXTURE);
    }

    public void setSaveAction(@Nullable Runnable saveAction) {
        this.saveAction = saveAction;
    }

    @Override
    public boolean isStateTriggered() {
        return stateData.get();
    }

    @Override
    public void setStateTriggered(boolean triggered) {
        super.setStateTriggered(triggered);
        stateData.set(triggered);
        save();
    }

    public boolean toggleState() {
        boolean toggle = stateData.toggle();
        super.setStateTriggered(toggle);
        save();
        return toggle;
    }

    private void save() {
        if (saveAction != null) {
            saveAction.run();
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        toggleState();
    }

    public static StateData createStateData(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return new StateData() {

            @Override
            public boolean get() {
                return getter.get();
            }

            @Override
            public void set(boolean state) {
                setter.accept(state);
            }
        };
    }

    public interface StateData {

        boolean get();

        void set(boolean state);

        default boolean toggle() {
            boolean state = !get();
            set(state);
            return state;
        }
    }
}
