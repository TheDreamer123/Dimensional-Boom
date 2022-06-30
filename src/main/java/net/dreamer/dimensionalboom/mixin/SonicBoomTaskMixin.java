package net.dreamer.dimensionalboom.mixin;

import net.dreamer.dimensionalboom.DimensionalBoom;
import net.dreamer.dimensionalboom.DimensionalBoomAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SonicBoomTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(SonicBoomTask.class)
public class SonicBoomTaskMixin extends Task<WardenEntity> {
    @Shadow @Final private static int RUN_TIME;
    @Shadow @Final private static int SOUND_DELAY;

    public SonicBoomTaskMixin(Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState) {
        super(requiredMemoryState);
    }

    @Inject(at = @At("HEAD"), method = "keepRunning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/WardenEntity;J)V", cancellable = true)
    public void keepRunningInject(ServerWorld serverWorld,WardenEntity wardenEntity,long l,CallbackInfo info) {
        wardenEntity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((target) -> wardenEntity.getLookControl().lookAt(target.getPos()));
        if (!wardenEntity.getBrain().hasMemoryModule(MemoryModuleType.SONIC_BOOM_SOUND_DELAY) && !wardenEntity.getBrain().hasMemoryModule(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) {
            wardenEntity.getBrain().remember(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN,Unit.INSTANCE,RUN_TIME - SOUND_DELAY);
            Optional<LivingEntity> var10000 = wardenEntity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET);
            Objects.requireNonNull(wardenEntity);
            var10000.filter(wardenEntity::isValidTarget).filter((target) -> wardenEntity.isInRange(target,15.0D,20.0D)).ifPresent((target) -> {
                Vec3d vec3d = wardenEntity.getPos().add(0.0D,1.600000023841858D,0.0D);
                Vec3d vec3d2 = target.getEyePos().subtract(vec3d);
                Vec3d vec3d3 = vec3d2.normalize();

                for (int i = 1; i < MathHelper.floor(vec3d2.length()) + 7; ++i) {
                    Vec3d vec3d4 = vec3d.add(vec3d3.multiply(i));
                    serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,vec3d4.x,vec3d4.y,vec3d4.z,1,0.0D,0.0D,0.0D,0.0D);
                }

                wardenEntity.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM,3.0F,1.0F);

                if(serverWorld.getGameRules().getBoolean(DimensionalBoom.ISEKAI_ALL_MOBS) || target instanceof PlayerEntity) {
                    if (serverWorld.getGameRules().getBoolean(DimensionalBoom.SHOW_ISEKAI_MESSAGE_ALL_MOBS) || target instanceof PlayerEntity) {
                        List<ServerPlayerEntity> list = serverWorld.getPlayers();
                        list.forEach((player) -> player.sendMessage(Text.translatable("Sonic boom-kun isekai'd %1$s",target.getDisplayName())));
                    }

                    if(serverWorld.getGameRules().getBoolean(DimensionalBoom.DEAL_SONIC_BOOM_DAMAGE)) target.damage(DamageSource.sonicBoom(wardenEntity), 10.0F);

                    ((DimensionalBoomAccessor) target).setIsekaid();
                    ServerWorld world = serverWorld.getServer().getWorld(World.OVERWORLD);
                    if (serverWorld.getRegistryKey() == World.OVERWORLD)
                        world = (new Random().nextInt(2) == 0 ? serverWorld.getServer().getWorld(World.NETHER) : serverWorld.getServer().getWorld(World.END));
                    if (serverWorld.getRegistryKey() == World.NETHER)
                        world = (new Random().nextInt(2) == 0 ? serverWorld.getServer().getWorld(World.OVERWORLD) : serverWorld.getServer().getWorld(World.END));
                    if (serverWorld.getRegistryKey() == World.END)
                        world = (new Random().nextInt(2) == 0 ? serverWorld.getServer().getWorld(World.NETHER) : serverWorld.getServer().getWorld(World.OVERWORLD));
                    target.moveToWorld(world);
                } else {
                    target.damage(DamageSource.sonicBoom(wardenEntity), 10.0F);
                    double d = 0.5D * (1.0D - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                    double e = 2.5D * (1.0D - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                    target.addVelocity(vec3d3.getX() * e, vec3d3.getY() * d, vec3d3.getZ() * e);
                }
            });
        }
        info.cancel();
    }
}
