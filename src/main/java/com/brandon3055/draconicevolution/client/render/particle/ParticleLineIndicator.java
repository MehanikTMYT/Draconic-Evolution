package com.brandon3055.draconicevolution.client.render.particle;

import com.brandon3055.brandonscore.client.particle.IntParticleType.IntParticleData;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by brandon3055 on 2/5/2016.
 * The particle used to render the beams on the Energy Core
 */
public class ParticleLineIndicator extends SingleQuadParticle {
    public ParticleLineIndicator(Level worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, SpriteSet sprites) {
        super((ClientLevel)worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, sprites.first());
        this.xd = xSpeedIn;
        this.yd = ySpeedIn;
        this.zd = zSpeedIn;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return DEParticleLayers.ADDITIVE;
    }

    @Override
    protected int getLightCoords(float p_107249_) {
        return net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;
    }


    public static class Factory implements ParticleProvider<IntParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet p_i50823_1_) {
            this.spriteSet = p_i50823_1_;
        }

        @Override
        public Particle createParticle(IntParticleData data, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource random) {
            ParticleLineIndicator particle = new ParticleLineIndicator(world, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
            particle.setSprite(spriteSet.get(random));

            int[] list = data.get();
            if (list.length >= 3) {
                particle.setColor(list[0] / 255F, list[1] / 255F, list[2] / 255F);
            }
            if (list.length >= 4) {
                particle.setLifetime(list[3]);
            }

            return particle;
        }
    }
}
