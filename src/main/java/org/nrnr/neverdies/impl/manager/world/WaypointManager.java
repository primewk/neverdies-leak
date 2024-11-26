package org.nrnr.neverdies.impl.manager.world;

import io.netty.util.internal.ConcurrentSet;
import org.nrnr.neverdies.api.waypoint.Waypoint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author chronos
 * @see Waypoint
 * @since 1.0
 */
public class WaypointManager {
    //
    private final Set<Waypoint> waypoints = new ConcurrentSet<>();

    /**
     * @param waypoint
     */
    public void register(Waypoint waypoint) {
        waypoints.add(waypoint);
    }

    /**
     * @param waypoints
     */
    public void register(Waypoint... waypoints) {
        for (Waypoint waypoint : waypoints) {
            register(waypoint);
        }
    }

    /**
     * @param waypoint
     * @return
     */
    public boolean remove(Waypoint waypoint) {
        return waypoints.remove(waypoint);
    }

    /**
     * @param waypoint
     * @return
     */
    public boolean remove(String waypoint) {
        for (Waypoint w : getWaypoints()) {
            if (w.getName().equalsIgnoreCase(waypoint)) {
                return waypoints.remove(w);
            }
        }
        return false;
    }

    /**
     * @return
     */
    public Collection<Waypoint> getWaypoints() {
        return waypoints;
    }

    /**
     * @return
     */
    public Collection<String> getIps() {
        final Set<String> ips = new HashSet<>();
        for (Waypoint waypoint : getWaypoints()) {
            ips.add(waypoint.getIp());
        }
        return ips;
    }
}
