package net.deamjava.plotarmor.mixin;

import net.deamjava.plotarmor.PlotArmorData;
import net.deamjava.plotarmor.PlotArmorHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Redirect(
			method = "hurtServer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"
			)
	)
	private void plotarmor$redirectDieAfterTotemFailed(LivingEntity instance, DamageSource source) {
		if (instance instanceof ServerPlayer player
				&& PlotArmorData.INSTANCE.isArmored(player.getUUID())) {
			PlotArmorHandler.saveFromDeath(player);
			return;
		}
		instance.die(source);
	}
}
