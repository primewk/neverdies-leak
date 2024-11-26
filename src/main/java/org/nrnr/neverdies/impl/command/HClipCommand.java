package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.chat.ChatUtil;

/**
 * @author chronos
 * @since 1.0
 */
public class HClipCommand extends Command {

    /**
     *
     */
    public HClipCommand() {
        super("HClip", "Horizontally clips the player", literal("hclip"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("distance", DoubleArgumentType.doubleArg()).executes(c -> {
            double dist = DoubleArgumentType.getDouble(c, "distance");
            double rad = Math.toRadians(mc.player.getYaw() + 90);
            double x = Math.cos(rad) * dist;
            double z = Math.sin(rad) * dist;
            Managers.POSITION.setPositionXZ(x, z);
            ChatUtil.clientSendMessage("Horizontally clipped §s" + dist + "§f blocks");
            return 1;
        })).executes(c -> {
            ChatUtil.error("Must provide distance!");
            return 1;
        });
    }
}
