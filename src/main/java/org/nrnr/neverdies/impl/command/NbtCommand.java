package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.util.chat.ChatUtil;

public class NbtCommand extends Command {
    /**
     *
     */
    public NbtCommand() {
        super("Nbt", "Displays all nbt tags on the held item", literal("nbt"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ItemStack mainhand = mc.player.getMainHandStack();
            if (!mainhand.hasNbt() || mainhand.getNbt() == null) {
                ChatUtil.error("No Nbt tags on this item!");
                return 0;
            }
            ChatUtil.clientSendMessage(mainhand.getNbt().toString());
            return 1;
        });
    }
}
