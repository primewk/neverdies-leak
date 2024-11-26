package org.nrnr.neverdies.impl.manager;

import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.impl.module.client.*;
import org.nrnr.neverdies.impl.module.combat.*;
import org.nrnr.neverdies.impl.module.combat.bedaura.BedAuraModule;
import org.nrnr.neverdies.impl.module.exploit.*;
import org.nrnr.neverdies.impl.module.legit.*;
import org.nrnr.neverdies.impl.module.misc.*;
import org.nrnr.neverdies.impl.module.movement.*;
import org.nrnr.neverdies.impl.module.render.*;
import org.nrnr.neverdies.impl.module.world.*;
import org.nrnr.neverdies.impl.module.combat.AutoSelectMinerModule;

import java.util.*;

/**
 * @author chronos
 * @since 1.0
 */
public class ModuleManager {
    // The client module register. Keeps a list of modules and their ids for
    // easy retrieval by id.
    private final Map<String, Module> modules =
            Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Initializes the module register.
     */
    public ModuleManager() {
        // MAINTAIN ALPHABETICAL ORDER
        register(
                // Client
                new ServerModule(),
                new CapesModule(),
                new ClickGuiModule(),
                new ColorsModule(),
                //new DiscordClientModule(),
                new HUDModule(),
                new IRCModule(),
                new RotationsModule(),
                // Combat
                new AuraModule(),
                new AutoAnchorModule(),
                new AutoArmorModule(),
                new AutoBowReleaseModule(),
                new AutoCrystalModule(),
                new AutoLogModule(),
                new AutoSelectMinerModule(),
                new AutoTotemModule(),
                new AutoTrapModule(),
                new AutoWebModule(),
                new AutoXPModule(),
                // new BacktrackModule(),
                new BedAuraModule(),
                new BurrowModule(),
                new BowAimModule(),
                new CevBreakerModule(),
                new ClickCrystalModule(),
                new LegitTotemModule(),
                new PearlMacroModule(),
                new CriticalsModule(),
                new HandBlockModule(),
                new HoleFillModule(),
                new NoHitDelayModule(),
                new ReplenishModule(),
                new SelfBowModule(),
                new SelfTrapModule(),
                new SurroundModule(),
                new SurroundRewriteModule(),
                new TriggerModule(),
                // Exploit
                new AntiHungerModule(),
             //   new ChorusControlModule(),
              //  new ClientSpoofModule(),
                new CrasherModule(),
                new DisablerModule(),
                new ExtendedFireworkModule(),
              //  new FakeLatencyModule(),
                new FastLatencyModule(),
                new FastProjectileModule(),
                new PacketCancelerModule(),
                new PacketFlyModule(),
                new PhaseModule(),
                new PortalGodModeModule(),
                new ReachModule(),
                // Misc
                new AntiAFKModule(),
                new AntiAimModule(),
                // new AntiBookBanModule(),
              //  new AntiSpamModule(),
              //  new AutoAcceptModule(),
                new AutoEatModule(),
                new AutoEZModule(),
                new AutoFishModule(),
                new AutoReconnectModule(),
                new AutoRespawnModule(),
               // new BeaconSelectorModule(),
                new BetterChatModule(),
                new ChatNotifierModule(),
                new ChestSwapModule(),
                // new ChestStealerModule(),
                new FakePlayerModule(),
              //  new InvCleanerModule(),
                new MiddleClickModule(),
                new NoPacketKickModule(),
             //   new NoSoundLagModule(),
                new PacketLoggerModule(),
                new TimerModule(),
             //   new TrueDurabilityModule(),
                new UnfocusedFPSModule(),
                new XCarryModule(),

                // Movement
           //     new AntiLevitationModule(),
                new AutoWalkModule(),
                new BlinkModule(),
                new ElytraFlyModule(),
                new EntityControlModule(),
                new EntitySpeedModule(),
                new FastFallModule(),
                new FlightModule(),
             //   new IceSpeedModule(),
             //   new JesusModule(),
                new HoleSnapModule(),
                new LongJumpModule(),
                new NoFallModule(),
                new NoJumpDelayModule(),
                new NoSlowModule(),
                new ParkourModule(),
                new SpeedModule(),
                new SprintModule(),
                new StepModule(),
                new TickShiftModule(),
              //  new TridentFlyModule(),
                new VelocityModule(),
             //   new YawModule(),
                // Render
                new BlockHighlightModule(),
                new BreadcrumbsModule(),
                new BreakHighlightModule(),
                new BurrowEspModule(),
                //new ChamsModule(),
                new ChamsRewriteModule(),

                new ESPModule(),
              //  new ExtraTabModule(),
                new FreecamModule(),
                new FullbrightModule(),
                new HoleESPModule(),
                new JumpCircleModule(),
                new NameProtectModule(),
                new NametagsModule(),
                new NoRenderModule(),
                new CrystalOptimizerModule(),
                new NoRotateModule(),
                new NoLootBlowModule(),
              //  new NoWeatherModule(),
                new ParticlesModule(),
                new PhaseESPModule(),
                new SkeletonModule(),
                new SkyboxModule(),
                new SwingModule(),
                new TooltipsModule(),
                new TracersModule(),
             //   new TrueSightModule(),
                new ViewClipModule(),
                new ViewModelModule(),
                // new WaypointsModule(),
                // World
              //  new AntiInteractModule(),
                new AutoMineModule(),
                new AutoToolModule(),
             //   new AvoidModule(),
                new BlockInteractModule(),
                new FastDropModule(),
                new FastPlaceModule(),
                new MultitaskModule(),
           //     new NoGlitchBlocksModule(),
                new ScaffoldModule(),
                new PacketMineModule()
        );

    }

    /**
     *
     */
    public void postInit() {
        // TODO
    }

    /**
     * @param modules
     * @see #register(Module)
     */
    private void register(Module... modules) {
        for (Module module : modules) {
            register(module);
        }
    }

    /**
     * @param module
     */
    private void register(Module module) {
        modules.put(module.getId(), module);
    }

    /**
     * @param id
     * @return
     */
    public Module getModule(String id) {
        return modules.get(id);
    }

    /**
     * @return
     */
    public List<Module> getModules() {
        return new ArrayList<>(modules.values());
    }
}
