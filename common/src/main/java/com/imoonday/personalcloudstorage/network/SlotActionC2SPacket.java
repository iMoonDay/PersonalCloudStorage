package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.api.SlotAction;
import com.imoonday.personalcloudstorage.api.SlotActionListener;
import com.imoonday.personalcloudstorage.component.PagedSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record SlotActionC2SPacket(SlotAction slotAction, boolean hasShift, int page, int slot) implements NetworkPacket {

    public SlotActionC2SPacket(SlotAction slotAction, boolean hasShift, PagedSlot slot) {
        this(slotAction, hasShift, slot.getPage(), slot.getSlot());
    }

    public SlotActionC2SPacket(FriendlyByteBuf buf) {
        this(buf.readEnum(SlotAction.class), buf.readBoolean(), buf.readInt(), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(slotAction);
        buf.writeBoolean(hasShift);
        buf.writeInt(page);
        buf.writeInt(slot);
    }

    @Override
    public void handle(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof SlotActionListener listener) {
            listener.onSlotClicked(slotAction, hasShift, page, slot);
        }
    }
}
