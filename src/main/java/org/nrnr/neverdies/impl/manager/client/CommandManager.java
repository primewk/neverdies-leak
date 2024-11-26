package org.nrnr.neverdies.impl.manager.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.api.command.Command;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.impl.command.*;
import org.nrnr.neverdies.impl.event.gui.chat.ChatMessageEvent;
import org.nrnr.neverdies.impl.event.keyboard.KeyboardInputEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.Globals;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author chronos
 * @see Command
 * @since 1.0
 */
public class CommandManager implements Globals {
    //
    private final List<Command> commands = new ArrayList<>();
    // Command prefix, used to identify a command in the chat
    private String prefix = ".";
    private int prefixKey = GLFW.GLFW_KEY_PERIOD;
    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandSource source = new ClientCommandSource(null, mc);

    /**
     * Registers commands to the CommandManager
     */
    public CommandManager() {
        Neverdies.EVENT_HANDLER.subscribe(this);
        register(
                new BindCommand(),
                new ConfigCommand(),
                new DisableAllCommand(),
                new DrawnCommand(),
                new FriendCommand(),
                new HClipCommand(),
                new HelpCommand(),
                new HideAllCommand(),
                new ModulesCommand(),
                new NbtCommand(),
                new OpenFolderCommand(),
                new PrefixCommand(),
                new ResetCommand(),
                new ReloadSoundCommand(),
                new ToggleCommand(),
                new VanishCommand(),
                new VClipCommand()
        );
        //
        for (Module module : Managers.MODULE.getModules()) {
            register(new ModuleCommand(module));
        }
        Neverdies.info("Updating Loader");
        Neverdies.info("Launching Client");

        for (Command command : commands) {
            command.buildCommand(command.getCommandBuilder());
            dispatcher.register(command.getCommandBuilder());
        }
    }

    @EventListener
    public void onChatMessage(ChatMessageEvent.Client event) {
        final String text = event.getMessage().trim();
        if (text.startsWith(prefix)) {
            String literal = text.substring(1);
            event.cancel();
            mc.inGameHud.getChatHud().addToMessageHistory(text);
            try {
                dispatcher.execute(dispatcher.parse(literal, source));
            } catch (Exception exception) {
                // exception.printStackTrace();
            }
        }
    }

    @EventListener
    public void onKeyboardInput(KeyboardInputEvent event) {
        if (event.getAction() == 1 && event.getKeycode() == prefixKey && mc.currentScreen == null) {
            event.cancel();
            mc.setScreen(new ChatScreen(""));
        }
    }


    @SuppressWarnings("unchecked")
    private LiteralArgumentBuilder<Object> redirectBuilder(String alias, LiteralCommandNode<?> destination) {
        LiteralArgumentBuilder<Object> literalArgumentBuilder = LiteralArgumentBuilder.literal(alias.toLowerCase()).requires((Predicate<Object>) destination.getRequirement())
                .forward((CommandNode<Object>) destination.getRedirect(), (RedirectModifier<Object>) destination.getRedirectModifier(), destination.isFork())
                .executes((com.mojang.brigadier.Command<Object>) destination.getCommand());
        for (CommandNode<?> child : destination.getChildren()) {
            literalArgumentBuilder.then((CommandNode<Object>) child);
        }
        return literalArgumentBuilder;
    }

    /**
     * @param commands
     */
    private void register(Command... commands) {
        for (Command command : commands) {
            register(command);
        }
    }

    /**
     * @param command
     */
    private void register(Command command) {
        commands.add(command);
    }

    /**
     * @return
     */
    public List<Command> getCommands() {
        return commands;
    }

    public Command getCommand(String name) {
        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(name)) {
                return command;
            }
        }
        return null;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix
     * @param prefixKey
     */
    public void setPrefix(String prefix, int prefixKey) {
        this.prefix = prefix;
        this.prefixKey = prefixKey;
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return dispatcher;
    }

    public CommandSource getSource() {
        return source;
    }
}
