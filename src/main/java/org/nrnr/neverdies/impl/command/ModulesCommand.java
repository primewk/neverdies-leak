package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.chat.ChatUtil;

public class ModulesCommand extends Command {
    public ModulesCommand() {
        super("Modules", "Displays all client modules", literal("modules"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(c -> {
            StringBuilder modulesList = new StringBuilder();
            for (Module module : Managers.MODULE.getModules()) {
                String formatting = module instanceof ToggleModule t && t.isEnabled() ? "§s" : "§f";
                modulesList.append(formatting);
                modulesList.append(module.getName());
                modulesList.append(Formatting.RESET);
                // LOL

            }
            ChatUtil.clientSendMessageRaw(" §7Modules:§f " + modulesList);
            return 1;
        });
    }
}
