package org.nrnr.neverdies.impl.module.legit;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.module.client.RotationsModule;

public class LegitTotemModule extends RotationModule {
    Config<Boolean> totemHover = new BooleanConfig("TotemHover", "WIP", true);
    private PlayerEntity playerToTrack;
    private boolean totemUsed;

    public LegitTotemModule() {

        super("LegitTotem", "Legit Totem by NRNR", ModuleCategory.LEGIT);
    }



    @EventListener
    public void onTick() {
        if (totemHover.getValue()) {
            PlayerEntity player = MinecraftClient.getInstance().player;

            if (player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
                playerToTrack = player;
                totemUsed = false;
            } else if (playerToTrack == player && !totemUsed && MinecraftClient.getInstance().currentScreen instanceof InventoryScreen) {
                PlayerScreenHandler screenHandler = player.playerScreenHandler;
                for (Slot slot : screenHandler.slots) {
                    if (slot.getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                        Vector2i slotCenter = getSlotScreenPosition(MinecraftClient.getInstance(), slot);
                        GLFW.glfwSetCursorPos(MinecraftClient.getInstance().getWindow().getHandle(), slotCenter.x, slotCenter.y);
                        totemUsed = true;
                        break;
                    }
                }
            }        }
    }

    private void legitTotem(MinecraftClient client) {

    }

    private Vector2i getSlotScreenPosition(MinecraftClient client, Slot slot) {
        HandledScreen<?> handledScreen = (HandledScreen<?>) client.currentScreen;
        int guiLeft = 0; // was (handledScreen.width - handledScreen.width) / 2
        int guiTop = 0; // was (handledScreen.height - handledScreen.height) / 2

        int slotSize = 16;
        int slotSpacingX = 2;
        int slotSpacingY = 4;
        int leftOffset = guiLeft + 8;
        int topOffset = guiTop + 17;
        int column = slot.id % 9;
        int row = slot.id / 9;
        int x = leftOffset + (slotSize + slotSpacingX) * column + slotSize / 2;
        int y = topOffset + (slotSize + slotSpacingY) * row + slotSize / 2;

        return new Vector2i(x, y);
    }

}
