package com.imoonday.personalcloudstorage.item;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.config.ServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PartitionNodeItem extends Item {

    public PartitionNodeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CloudStorage cloudStorage = CloudStorage.of(player);
            int originalPages = cloudStorage.getTotalPages();
            int maxPages = ServerConfig.get().maxPages;
            if (originalPages < maxPages) {
                cloudStorage.addNewPage();
                cloudStorage.syncToClient(serverPlayer);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level.playSound(null, serverPlayer.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS);
                serverPlayer.sendSystemMessage(Component.translatable(this.getDescriptionId() + ".success", cloudStorage.getTotalPages()));
            } else {
                serverPlayer.sendSystemMessage(Component.translatable(this.getDescriptionId() + ".failure"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }
}
