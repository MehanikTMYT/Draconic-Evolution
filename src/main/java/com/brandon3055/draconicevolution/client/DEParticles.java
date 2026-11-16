package com.brandon3055.draconicevolution.client;

import com.brandon3055.brandonscore.client.particle.IntParticleType;
import com.brandon3055.brandonscore.client.particle.IntParticleType.IntParticleData;
import com.brandon3055.draconicevolution.client.render.particle.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;

/**
 * Created by brandon3055 on 23/4/2016.
 * A list of all of DE's particles
 */
public class DEParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MODID);

    public static void init(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }

    //@formatter:off
    public static final DeferredHolder<ParticleType<?>, ParticleType<IntParticleData>>          FLAME                   = PARTICLE_TYPES.register("flame",                  () -> new IntParticleType(false));
    public static final DeferredHolder<ParticleType<?>, ParticleType<IntParticleData>>          LINE_INDICATOR          = PARTICLE_TYPES.register("line_indicator",         () -> new IntParticleType(false));
    public static final DeferredHolder<ParticleType<?>, ParticleType<IntParticleData>>          ENERGY                  = PARTICLE_TYPES.register("energy",                 () -> new IntParticleType(false));
    public static final DeferredHolder<ParticleType<?>, ParticleType<IntParticleData>>          ENERGY_BASIC            = PARTICLE_TYPES.register("energy_basic",           () -> new IntParticleType(false));
    public static final DeferredHolder<ParticleType<?>, ParticleType<IntParticleData>>          ENERGY_CORE             = PARTICLE_TYPES.register("energy_core",            () -> new IntParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType>                     GUARDIAN_PROJECTILE     = PARTICLE_TYPES.register("guardian_projectile",    () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType>                     BLINK                   = PARTICLE_TYPES.register("blink",                  () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType>                     GUARDIAN_CLOUD          = PARTICLE_TYPES.register("guardian_cloud",         () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType>                     GUARDIAN_BEAM           = PARTICLE_TYPES.register("guardian_beam",          () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, ParticleType<IntParticleData>>          SPARK                   = PARTICLE_TYPES.register("spark",                  () -> new IntParticleType(false));
    //@formatter:on

    public static void registerFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FLAME.get(), CustomFlameParticle.Factory::new);
        event.registerSpriteSet(LINE_INDICATOR.get(), ParticleLineIndicator.Factory::new);
        event.registerSpriteSet(ENERGY.get(), ParticleEnergy.Factory::new);
        event.registerSpriteSet(ENERGY_BASIC.get(), ParticleEnergyBasic.Factory::new);
        event.registerSpriteSet(ENERGY_CORE.get(), ParticleEnergyCoreFX.Factory::new);
        event.registerSpriteSet(GUARDIAN_PROJECTILE.get(), GuardianProjectileParticle.Factory::new);
        event.registerSpriteSet(BLINK.get(), BlinkParticle.Factory::new);
        event.registerSpriteSet(GUARDIAN_CLOUD.get(), GuardianCloudParticle.Factory::new);
        event.registerSpriteSet(GUARDIAN_BEAM.get(), GuardianBeamParticle.Factory::new);
        event.registerSpriteSet(SPARK.get(), SparkParticle.Factory::new);
    }


    public static Particle addParticleDirect(Level world, Particle particle) {
        if (world instanceof ClientLevel && particle instanceof com.brandon3055.draconicevolution.client.render.effect.CrystalFXBase<?> crystal) {
            com.brandon3055.draconicevolution.client.render.effect.CrystalFXRenderer.add(crystal);
            return crystal;
        }
        if (world instanceof ClientLevel) {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return null;
            Camera activerenderinfo = mc.gameRenderer.getMainCamera();
            if (activerenderinfo.isInitialized() && mc.particleEngine != null) {
                mc.particleEngine.add(particle);
                return particle;
            }
        }
        return null;
    }

}
