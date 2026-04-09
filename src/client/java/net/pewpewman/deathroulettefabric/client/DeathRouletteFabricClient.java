package net.pewpewman.deathroulettefabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.pewpewman.deathroulettefabric.DeathRouletteFabric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathRouletteFabricClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(DeathRouletteFabric.MOD_ID);

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}