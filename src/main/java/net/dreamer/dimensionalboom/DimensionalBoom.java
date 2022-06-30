package net.dreamer.dimensionalboom;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimensionalBoom implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Dimensional Boom");



	public static final GameRules.Key<GameRules.BooleanRule> ISEKAI_ALL_MOBS = GameRuleRegistry.register("dimensionalboom:isekaiAllMobs", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
	public static final GameRules.Key<GameRules.BooleanRule> DEAL_SONIC_BOOM_DAMAGE = GameRuleRegistry.register("dimensionalboom:dealSonicBoomDamage", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));
	public static final GameRules.Key<GameRules.BooleanRule> SHOW_ISEKAI_MESSAGE_ALL_MOBS = GameRuleRegistry.register("dimensionalboom:showIsekaiMessageAllMobs", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));

	@Override
	public void onInitialize() {
		LOGGER.info("Truck-kun is looking proud of sonic boom-kun.");
	}
}
