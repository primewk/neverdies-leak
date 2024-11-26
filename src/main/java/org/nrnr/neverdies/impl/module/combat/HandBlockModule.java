package org.nrnr.neverdies.impl.module.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.event.world.AddEntityEvent;
import org.nrnr.neverdies.impl.event.world.RemoveEntityEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.player.FindItemResult;
import org.nrnr.neverdies.util.player.InventoryUtil;
import org.nrnr.neverdies.util.player.RotationUtil;
import org.nrnr.neverdies.util.world.BlockUtil;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class HandBlockModule extends RotationModule {

    Config<Boolean> autoSwap = new BooleanConfig("AutoSwap", "AutoSwap to Obsidian", true);
    Config<Boolean> swapBackConfig = new BooleanConfig("SwapBack", "Swaps Back after autoswap", false);
    Config<Boolean> swingConfig = new BooleanConfig("Swing", "Swings your mainhand", false);
    Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Simple strict direction placements", false);
    Config<Boolean> grimConfig = new BooleanConfig("Grim", "For GrimAC", false);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotate before breaking", false);


    public HandBlockModule() {
        super("HandBlock", "Mainly for flat, places block at crossair location", ModuleCategory.LEGIT);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            final BlockState state = packet.getState();
            final BlockPos targetPos = packet.getPos();
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event){
        FindItemResult obsidianItemResult;
        if (autoSwap.getValue()) {
            obsidianItemResult = InventoryUtil.findInHotbar(Items.OBSIDIAN);
        }
        else {
            assert mc.player != null;
            obsidianItemResult = new FindItemResult(mc.player.getInventory().selectedSlot, 1);
        }

        Vec3d crossairPosVec = mc.crosshairTarget.getPos();
        MinecraftClient client1 = MinecraftClient.getInstance();
        assert !(client1 == null);
        BlockHitResult hitResult = (BlockHitResult) client1.crosshairTarget;

        assert hitResult != null;
        Managers.INTERACT.placeBlock(hitResult.getBlockPos(), obsidianItemResult.slot(), grimConfig.getValue(), strictDirectionConfig.getValue(), swingConfig.getValue(), (state, angles) ->
        {
                    if (rotateConfig.getValue()) {
                        if (state) {
                            Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                        } else {
                            Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                        }
                    }
        });

    }




}
