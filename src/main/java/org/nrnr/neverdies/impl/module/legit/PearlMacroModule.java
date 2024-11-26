package org.nrnr.neverdies.impl.module.legit;


import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.util.chat.ChatUtil;

public class PearlMacroModule extends RotationModule {

    Config<Integer> cooldown = new NumberConfig<>("cooldown-ms", "cooldown time", 0, 200, 1000, () -> false);
    Config<Boolean> throwPearl = new BooleanConfig("Throw", "throw pearl", true);
    Config<Boolean> back = new BooleanConfig("SwitchBack", "switch back", true);
    Config<Boolean> debug = new BooleanConfig("Debug", "send toggle messages", false);


    public PearlMacroModule() {
        super("PearlMacro", "Switches to a enderpearl, throws it, and then switches back based on your setting choice", ModuleCategory.LEGIT);
    }


    @Override
    public void onEnable() {
        super.onEnable();
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            pearlMacro(player);
        }
    }



    public boolean pearlMacro(PlayerEntity player) {
        int originalSlot = player.getInventory().selectedSlot;

        for (int i = 0; i < 9; i++) {
            ItemStack stackInSlot = player.getInventory().getStack(i);
            if (stackInSlot.getItem() == Items.ENDER_PEARL) {
                if (debug.getValue()) {
                    ChatUtil.clientSendMessage("EnderPearl was found!");
                }
                player.getInventory().selectedSlot = i;
                if (throwPearl.getValue()) {
                    ActionResult result = mc.interactionManager.interactItem(mc.player, mc.player.getActiveHand());
                    if (result == ActionResult.SUCCESS) {
                        if (debug.getValue()) {
                            ChatUtil.clientSendMessage("EnderPearl was thrown!");
                        }
                    }
                    if (back.getValue()) {
                        player.getInventory().selectedSlot = originalSlot;
                    }
                    PearlMacroModule.this.disable();
                    return true;
                }
            }
            else {
                if (debug.getValue()) {
                    ChatUtil.clientSendMessage("EnderPearl is not present in hotbar!");
                }
            }
        }
        return false;
    }

}
