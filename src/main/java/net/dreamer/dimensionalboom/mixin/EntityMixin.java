package net.dreamer.dimensionalboom.mixin;

import net.dreamer.dimensionalboom.DimensionalBoomAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements DimensionalBoomAccessor {
    @Shadow public abstract double getX();
    @Shadow public abstract double getZ();
    @Shadow public abstract Vec3d getVelocity();
    @Shadow public abstract float getYaw();
    @Shadow public abstract float getPitch();

    boolean wasIsekaid = false;

    public void setIsekaid() {
        this.wasIsekaid = true;
    }

    private void createIsekaiSpawnPlatform(ServerWorld world) {
        BlockPos blockPos = new BlockPos(this.getX(), 50, this.getZ());
        int i = blockPos.getX();
        int j = blockPos.getY() - 2;
        int k = blockPos.getZ();
        BlockPos.iterate(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach((pos) -> world.setBlockState(pos, Blocks.WHITE_STAINED_GLASS.getDefaultState()));
        BlockPos.iterate(i - 1, j, k - 1, i + 1, j + 2, k + 1).forEach((pos) -> world.setBlockState(pos, Blocks.AIR.getDefaultState()));
        BlockPos.iterate(i - 2, j, k - 2, i + 2, j, k + 2).forEach((pos) -> world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState()));
    }

    @Inject(at = @At("HEAD"), method = "getTeleportTarget", cancellable = true)
    public void getTeleportTargetInject(ServerWorld destination,CallbackInfoReturnable<TeleportTarget> cir) {
        if(this.wasIsekaid) {
            if(destination.getRegistryKey() == World.NETHER || destination.getRegistryKey() == World.OVERWORLD) {
                BlockPos blockPos = new BlockPos(this.getX(), 50, this.getZ());
                this.createIsekaiSpawnPlatform(destination);

                this.wasIsekaid = false;

                cir.setReturnValue(new TeleportTarget(new Vec3d((double)blockPos.getX() + 0.5D,blockPos.getY(), (double)blockPos.getZ() + 0.5D), this.getVelocity(), this.getYaw(), this.getPitch()));
            }

            this.wasIsekaid = false;
        }
    }
}