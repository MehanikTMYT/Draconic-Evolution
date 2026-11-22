package com.brandon3055.draconicevolution.client;

import com.brandon3055.draconicevolution.client.render.particle.*;
import com.brandon3055.draconicevolution.init.DEParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * port-26.1.2: клиентская часть particle-системы (фабрики + прямой рендер);
 * регистрация типов — в common {@link DEParticles}.
 */
public class DEParticlesClient {

    public static void registerFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(DEParticles.FLAME.get(), CustomFlameParticle.Factory::new);
        event.registerSpriteSet(DEParticles.LINE_INDICATOR.get(), ParticleLineIndicator.Factory::new);
        event.registerSpriteSet(DEParticles.ENERGY.get(), ParticleEnergy.Factory::new);
        event.registerSpriteSet(DEParticles.ENERGY_BASIC.get(), ParticleEnergyBasic.Factory::new);
        event.registerSpriteSet(DEParticles.ENERGY_CORE.get(), ParticleEnergyCoreFX.Factory::new);
        event.registerSpriteSet(DEParticles.GUARDIAN_PROJECTILE.get(), GuardianProjectileParticle.Factory::new);
        event.registerSpriteSet(DEParticles.BLINK.get(), BlinkParticle.Factory::new);
        event.registerSpriteSet(DEParticles.GUARDIAN_CLOUD.get(), GuardianCloudParticle.Factory::new);
        event.registerSpriteSet(DEParticles.GUARDIAN_BEAM.get(), GuardianBeamParticle.Factory::new);
        event.registerSpriteSet(DEParticles.SPARK.get(), SparkParticle.Factory::new);
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
