package org.nrnr.neverdies.util;

import net.minecraft.client.MinecraftClient;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TERRIBLE CODING PRACTICE
 *
 * @author chronos
 * @since 1.0
 */
public interface Globals {
    // Minecraft game instance
    MinecraftClient mc = MinecraftClient.getInstance();
    //
    Random RANDOM = ThreadLocalRandom.current();
}
