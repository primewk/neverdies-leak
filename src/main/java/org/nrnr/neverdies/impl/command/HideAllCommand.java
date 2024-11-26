package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.chat.ChatUtil;

public class HideAllCommand extends Command {

    public HideAllCommand() {
        super("HideAll", "Hides all modules from the arraylist", literal("hideall"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(c -> {
            for (Module module : Managers.MODULE.getModules()) {
                if (module instanceof ToggleModule toggleModule && !toggleModule.isHidden()) {
                    toggleModule.setHidden(true);
                }
            }
            ChatUtil.clientSendMessage("All modules are hidden");
            return 1;
        });
    }
}
