package com.imoonday.personalcloudstorage.item;

import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.core.CloudStorage;
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
        CloudStorage cloudStorage = CloudStorage.of(player);
        int originalPages = cloudStorage.getTotalPages();
        int maxPages = ServerConfig.get().maxPages;
        if (originalPages < maxPages) {
            if (player instanceof ServerPlayer serverPlayer) {
                cloudStorage.addNewPage();
                cloudStorage.syncToClient(serverPlayer);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level.playSound(null, serverPlayer.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS);
                serverPlayer.sendSystemMessage(Component.translatable(this.getDescriptionId() + ".success", cloudStorage.getTotalPages()));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.personalcloudstorage.reach_upper_limit"), true);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }
}
