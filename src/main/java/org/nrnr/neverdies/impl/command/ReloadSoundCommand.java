package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.util.chat.ChatUtil;

public class ReloadSoundCommand extends Command {
    public ReloadSoundCommand() {
        super("ReloadSound", "Reloads the Minecraft sound system", literal("reloadsound"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            mc.getSoundManager().reloadSounds();
            ChatUtil.clientSendMessage("Reloaded the SoundSystem");
            return 1;
        });
    }
}
