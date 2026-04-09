package net.pewpewman.deathroulettefabric;

import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

import net.pewpewman.deathroulettefabric.Config;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

public class DeathRouletteFabric implements ModInitializer {
    public static final String MOD_ID = "death-roulette-fabric";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Config CONFIG = Config.createAndLoad();

    private static final Random rand = new Random(System.currentTimeMillis());

    //Sounds
    private static final SoundEvent CLEAR_SOUND = PolymerSoundEvent.registerOverlay(
        Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            Identifier.fromNamespaceAndPath(MOD_ID, "inventory_cleared"),
            SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "inventory_cleared"))
        ));

    private static final SoundEvent SPARE_SOUND = PolymerSoundEvent.registerOverlay(
        Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            Identifier.fromNamespaceAndPath(MOD_ID, "inventory_spared"),
            SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "inventory_spared"))
        ));

    private static final SoundEvent REALLY_REALLY_STUPID_CLEAR_SOUND = PolymerSoundEvent.registerOverlay(
        Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            Identifier.fromNamespaceAndPath(MOD_ID, "really_stupid_clear_sound"),
            SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "really_stupid_clear_sound"))
        ));


    @Override
    public void onInitialize() {
        ServerPlayerEvents.AFTER_RESPAWN.register(this::onDeathEvent);

        //Register assets to polymer
        if (!PolymerResourcePackUtils.addModAssets(MOD_ID)) {
            throw new RuntimeException("MOD_ID somehow not valid :shrug:");
        }
        PolymerResourcePackUtils.markAsRequired();

        //LOGGER.info("SPARE SOUND: " + SPARE_SOUND);
        //LOGGER.info("CLEAR SOUND: " + CLEAR_SOUND);
        //LOGGER.info("STUPID CLEAR SOUND: " + REALLY_REALLY_STUPID_CLEAR_SOUND);

    }

    private void onDeathEvent(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {

        //Ignore stuff like returning from the end dragon fight
        if (alive) {
            return;
        }

        //We only want to do roulette stuff when keep inventory is on
        if (!oldPlayer.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }

        //Roll for death chance

        List<ServerPlayer> soundHearers;
        SoundEvent sound;

        if (rand.nextDouble() <= CONFIG.invWipeChance()) {
            //LOGGER.info("INV WIPE PROCED!!!");

            newPlayer.getInventory().clearContent();

            List<ServerPlayer> players = newPlayer.level().getServer().getPlayerList().getPlayers();

            //Display big text on dead guy's screen
            newPlayer.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("death-roulette-fabric.inventory_cleared_title", oldPlayer.getDisplayName())
                            .withStyle(ChatFormatting.RED)
                            .withStyle(ChatFormatting.BOLD)
            ));
            newPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(20, 40, 20));

            //I've got to warn the others
            for (ServerPlayer player : players) {
                player.sendSystemMessage(
                        Component.translatable(
                                "death-roulette-fabric.inventory_cleared_server_message",
                                newPlayer.getDisplayName()
                        ).withStyle(ChatFormatting.RED)
                );
            }

            //Whole server gets laughing sound
            soundHearers = players;
            sound = !CONFIG.useReallyReallyStupidClearSound()
                    ? CLEAR_SOUND
                    : REALLY_REALLY_STUPID_CLEAR_SOUND;

        } else {
            //Only dead player hears lucky sound
            soundHearers = List.of(oldPlayer);
            sound = SPARE_SOUND;
        }

        //LOGGER.info("SOUND HEARERS: " + soundHearers);
        //Play sound to all who hear them
        for (ServerPlayer player : soundHearers) {
            if (player == newPlayer) {
                player = oldPlayer;
            }

            player.playSound(sound);
        }
    }
}