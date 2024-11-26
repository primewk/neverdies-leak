package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.api.command.ModuleArgumentType;
import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.util.chat.ChatUtil;

/**
 * @author chronos
 * @since 1.0
 */
public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("Toggle", "Enables/Disables a module", literal("toggle"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("module", ModuleArgumentType.module()).executes(c -> {
            Module module = ModuleArgumentType.getModule(c, "module");
            if (module instanceof ToggleModule t) {
                t.toggle();
                ChatUtil.clientSendMessage("%s is now %s", "§7" + t.getName() + "§f", t.isEnabled() ? "§senabled§f" : "§cdisabled§f");
            }
            return 1;
        })).executes(c -> {
            ChatUtil.error("Must provide module to toggle!");
            return 1;
        });
    }
}
