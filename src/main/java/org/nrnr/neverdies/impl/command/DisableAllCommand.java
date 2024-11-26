package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.chat.ChatUtil;

/**
 * @author Neverdies
 * @since 1.0
 */
public class DisableAllCommand extends Command {
    /**
     *
     */
    public DisableAllCommand() {
        super("DisableAll", "Disables all enabled modules", literal("disableall"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(c -> {
            for (Module module : Managers.MODULE.getModules()) {
                if (module instanceof ToggleModule toggleModule && toggleModule.isEnabled()) {
                    toggleModule.disable();
                }
            }
            ChatUtil.clientSendMessage("All modules are disabled");
            return 1;
        });
    }
}
