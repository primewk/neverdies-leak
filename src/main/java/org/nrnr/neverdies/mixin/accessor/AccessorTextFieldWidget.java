package org.nrnr.neverdies.mixin.accessor;

import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author chronos
 * @since 1.0
 */
@Mixin(TextFieldWidget.class)
public interface AccessorTextFieldWidget {
    /**
     * @return
     * @see TextFieldWidget#drawsBackground
     */
    @Accessor("drawsBackground")
    boolean isDrawsBackground();
}
