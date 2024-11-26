package org.nrnr.neverdies.impl.event.block;

import net.minecraft.block.Block;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
@Cancelable
public class BlockSlipperinessEvent extends Event {
    //
    private final Block block;
    private float slipperiness;

    /**
     * @param block
     * @param slipperiness
     */
    public BlockSlipperinessEvent(Block block, float slipperiness) {
        this.block = block;
        this.slipperiness = slipperiness;
    }

    /**
     * @return
     */
    public Block getBlock() {
        return block;
    }

    /**
     * @return
     */
    public float getSlipperiness() {
        return slipperiness;
    }

    /**
     * @param slipperiness
     */
    public void setSlipperiness(float slipperiness) {
        this.slipperiness = slipperiness;
    }
}
