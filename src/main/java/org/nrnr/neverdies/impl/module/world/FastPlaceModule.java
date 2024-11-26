package org.nrnr.neverdies.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.ItemListConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.mixin.accessor.AccessorMinecraftClient;
import org.nrnr.neverdies.util.math.timer.CacheTimer;
import org.nrnr.neverdies.util.world.SneakBlocks;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chronos
 * @since 1.0
 */
public class FastPlaceModule extends ToggleModule {

    //
    Config<Selection> selectionConfig = new EnumConfig<>("Selection", "The selection of items to apply fast placements", Selection.WHITELIST, Selection.values());
    Config<Integer> delayConfig = new NumberConfig<>("Delay", "Fast place click delay", 0, 1, 4);
    Config<Float> startDelayConfig = new NumberConfig<>("StartDelay", "Fast place start delay", 0.0f, 0.0f, 1.0f);
    Config<Boolean> ghostFixConfig = new BooleanConfig("GhostFix", "Fixes item ghosting issue on some servers", false);
    Config<List<Item>> whitelistConfig = new ItemListConfig<>("Whitelist", "Valid item whitelist", Items.EXPERIENCE_BOTTLE, Items.SNOWBALL, Items.EGG);
    Config<List<Item>> blacklistConfig = new ItemListConfig<>("Blacklist", "Valid item blacklist", Items.ENDER_PEARL, Items.ENDER_EYE);
    //
    private final CacheTimer startTimer = new CacheTimer();

    public FastPlaceModule() {
        super("FastPlace", "Place items and blocks faster", ModuleCategory.WORLD);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (!mc.options.useKey.isPressed()) {
            startTimer.reset();
        } else if (startTimer.passed(startDelayConfig.getValue(), TimeUnit.SECONDS)
                && ((AccessorMinecraftClient) mc).hookGetItemUseCooldown() > delayConfig.getValue()
                && placeCheck(mc.player.getMainHandStack())) {
            if (ghostFixConfig.getValue()) {
                Managers.NETWORK.sendSequencedPacket(id ->
                        new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), id));
            }
            ((AccessorMinecraftClient) mc).hookSetItemUseCooldown(delayConfig.getValue());
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet
                && ghostFixConfig.getValue() && !event.isClientPacket()
                && placeCheck(mc.player.getStackInHand(packet.getHand()))) {
            BlockState state = mc.world.getBlockState(packet.getBlockHitResult().getBlockPos());
            if (!SneakBlocks.isSneakBlock(state)) {
                event.cancel();
            }
        }
    }

    private boolean placeCheck(ItemStack held) {
        return switch (selectionConfig.getValue()) {
            case WHITELIST -> ((ItemListConfig<?>) whitelistConfig)
                    .contains(held.getItem());
            case BLACKLIST -> !((ItemListConfig<?>) blacklistConfig)
                    .contains(held.getItem());
            case ALL -> true;
        };
    }

    public enum Selection {
        WHITELIST,
        BLACKLIST,
        ALL
    }
}
