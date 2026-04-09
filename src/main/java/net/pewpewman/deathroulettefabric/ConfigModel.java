package net.pewpewman.deathroulettefabric;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = DeathRouletteFabric.MOD_ID)
@Config(name = "death_roulette_config", wrapperName = "Config")
public class ConfigModel {

    public double invWipeChance = 0.05;
    public boolean useReallyReallyStupidClearSound = false;
}
