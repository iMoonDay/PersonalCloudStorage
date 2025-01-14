package com.imoonday.personalcloudstorage.mixin;

import com.imoonday.personalcloudstorage.api.CloudStorageContainer;
import com.imoonday.personalcloudstorage.component.CloudStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements CloudStorageContainer {

    @Unique
    private CloudStorage cloudStorage;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        throw new AssertionError("Mixin constructor called");
    }

    @NotNull
    @Override
    public CloudStorage getCloudStorage() {
        if (cloudStorage == null) {
            cloudStorage = new CloudStorage(this.getUUID(), 36);
        }
        return cloudStorage;
    }

    @Override
    public void setCloudStorage(@NotNull CloudStorage cloudStorage) {
        this.cloudStorage = cloudStorage;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        compound.put("CloudStorage", getCloudStorage().toTag(new CompoundTag()));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("CloudStorage", Tag.TAG_COMPOUND)) {
            CloudStorage storage = CloudStorage.fromTag(compound.getCompound("CloudStorage"));
            if (storage != null) {
                setCloudStorage(storage);
            }
        }
    }
}
