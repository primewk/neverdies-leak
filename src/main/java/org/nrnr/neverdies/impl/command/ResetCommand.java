package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.api.command.ModuleArgumentType;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.util.chat.ChatUtil;

public class ResetCommand extends Command {

    public ResetCommand() {
        super("Reset", "Resets the values of modules", literal("reset"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("module", ModuleArgumentType.module()).executes(context -> {
            Module module = ModuleArgumentType.getModule(context, "module");
            if (module == null) {
                ChatUtil.error("Invalid module!");
                return 0;
            }
            for (Config<?> config : module.getConfigs()) {
                if (config.getName().equalsIgnoreCase("Enabled") || config.getName().equalsIgnoreCase("Keybind")
                        || config.getName().equalsIgnoreCase("Hidden")) {
                    continue;
                }
                config.resetValue();
            }
            ChatUtil.clientSendMessage("§7" + module.getName() + "§f settings were reset to default values");
            return 1;
        })).executes(context -> {
            ChatUtil.error("Must provide module to reset!");
            return 1;
        });
    }
}
