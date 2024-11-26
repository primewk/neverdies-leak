package org.nrnr.neverdies.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.api.command.PlayerArgumentType;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.chat.ChatUtil;

/**
 * @author chronos
 * @since 1.0
 */
public class FriendCommand extends Command {

    /**
     *
     */
    public FriendCommand() {
        super("Friend", "Adds/Removes a friend from the player list", literal("friend"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("add/del", StringArgumentType.string()).suggests(suggest("add", "del", "remove"))
                .then(argument("friend_name", PlayerArgumentType.player()).executes(c -> {
                    String playerName = PlayerArgumentType.getPlayer(c, "friend_name");
                    final String action = StringArgumentType.getString(c, "add/del");
                    if (action.equalsIgnoreCase("add")) {
                        if (Managers.SOCIAL.isFriend(playerName)) {
                            ChatUtil.error("Player is already friended!");
                            return 0;
                        }
                        ChatUtil.clientSendMessage("Added friend with name §s" + playerName);
                        Managers.SOCIAL.addFriend(playerName);
                    } else if (action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("del")) {
                        if (!Managers.SOCIAL.isFriend(playerName)) {
                            ChatUtil.error("Player is not friended!");
                            return 0;
                        }
                        ChatUtil.clientSendMessage("Removed friend with name §c" + playerName);
                        Managers.SOCIAL.remove(playerName);
                    }
                    return 1;
                })).executes(c -> {
                    ChatUtil.error("Must provide player to friend!");
                    return 1;
                })).executes(c -> {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
