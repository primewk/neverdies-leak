package org.nrnr.neverdies.impl.module.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.keyboard.KeyboardInputEvent;
import org.nrnr.neverdies.init.Modules;

/**
 * @author chronos
 * @since 1.0
 */
public class SearchModule extends ToggleModule {

    public SearchModule() {
        super("Search", "Highlights specified blocks in the world", ModuleCategory.RENDER);
    }
    private String inputText = "";
    private int boxWidth = 200;
    private int boxHeight = 20;
    private int x, y; // Position of the box


}
