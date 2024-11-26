package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.util.chat.ChatUtil;

/**
 * @author chronos
 * @since 1.0
 */
public class ConfigCommand extends Command {
    /**
     *
     */
    public ConfigCommand() {
        super("Config", "Creates a new configuration preset", literal("config"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("save/load", StringArgumentType.string()).suggests(suggest("save", "load"))
                .then(argument("config_name", StringArgumentType.string()).executes(c -> {
                    String action = StringArgumentType.getString(c, "save/load");
                    String name = StringArgumentType.getString(c, "config_name");
                    if (action.equalsIgnoreCase("save")) {
                        Neverdies.CONFIG.saveModuleConfiguration(name);
                        ChatUtil.clientSendMessage("Saved config: §s" + name);
                    } else if (action.equalsIgnoreCase("load")) {
                        Neverdies.CONFIG.loadModuleConfiguration(name);
                        ChatUtil.clientSendMessage("Loaded config: §s" + name);
                    }
                    return 1;
                })).executes(c -> {
                    ChatUtil.error("Must provide a config to load!");
                    return 1;
                })).executes(c -> {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
