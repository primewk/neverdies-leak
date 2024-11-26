package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.api.macro.Macro;
import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.KeyboardUtil;
import org.nrnr.neverdies.util.chat.ChatUtil;

/**
 * @author chronos
 * @since 1.0
 */
public class PrefixCommand extends Command {

    public PrefixCommand() {
        super("Prefix", "Allows you to change the chat command prefix", literal("prefix"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("prefix", StringArgumentType.string()).executes(c -> {
            final String prefix = StringArgumentType.getString(c, "prefix");
            if (prefix.length() > 1) {
                ChatUtil.error("Prefix can only be one character!");
                return 0;
            }
            int keycode = KeyboardUtil.getKeyCode(prefix);
            Managers.COMMAND.setPrefix(prefix, keycode);
            ChatUtil.clientSendMessage("Command prefix changed to §s" + prefix);
            return 1;
        })).executes(c -> {
            ChatUtil.error("Please provide a new prefix!");
            return 1;
        });
    }
}
