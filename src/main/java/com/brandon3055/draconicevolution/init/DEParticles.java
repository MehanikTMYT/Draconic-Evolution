package com.brandon3055.draconicevolution.init;

import com.brandon3055.brandonscore.client.particle.IntParticleType;
import com.brandon3055.brandonscore.client.particle.IntParticleType.IntParticleData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;

/**
 * port-26.1.2: вынесено из client.DEParticles в common — регистрация particle-типов
 * нужна на обеих сторонах (клиентские фабрики остались в client.DEParticles).
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
}
