package org.nrnr.neverdies.api.waypoint;

import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.ConfigContainer;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.util.math.timer.CacheTimer;
import org.nrnr.neverdies.util.math.timer.Timer;

/**
 * @author chronos
 * @since 1.0
 */
public class Waypoint extends ConfigContainer implements Position {
    //
    private final String ip;
    //
    private final Config<Double> xConfig = new NumberConfig<>("X", "X " +
            "position of waypoint.", 0.0D, 0.0D, Double.MAX_VALUE);
    private final Config<Double> yConfig = new NumberConfig<>("Y", "Y " +
            "position of waypoint.", 0.0D, 0.0D, Double.MAX_VALUE);
    private final Config<Double> zConfig = new NumberConfig<>("Z", "Z " +
            "position of waypoint.", 0.0D, 0.0D, Double.MAX_VALUE);
    private final Timer timer;

    /**
     * @param name
     * @param ip
     * @param x
     * @param y
     * @param z
     */
    public Waypoint(String name, String ip, double x, double y, double z) {
        super(name);
        this.ip = ip;
        xConfig.setValue(x);
        yConfig.setValue(y);
        zConfig.setValue(z);
        this.timer = new CacheTimer();
    }

    /**
     * @param time
     * @return
     */
    private boolean passedTime(long time) {
        return timer.passed(time);
    }

    public String getIp() {
        return ip;
    }

    @Override
    public double getX() {
        return xConfig.getValue();
    }

    @Override
    public double getY() {
        return yConfig.getValue();
    }

    @Override
    public double getZ() {
        return zConfig.getValue();
    }

    /**
     * @return
     */
    public Vec3d getPos() {
        return new Vec3d(getX(), getY(), getZ());
    }
}
