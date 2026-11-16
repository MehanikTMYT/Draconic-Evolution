package com.brandon3055.draconicevolution.client.render.particle;

import com.brandon3055.brandonscore.client.particle.IntParticleType.IntParticleData;

import com.brandon3055.brandonscore.lib.Vec3D;

import com.brandon3055.brandonscore.utils.Utils;

import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.client.particle.*;




public class ParticleEnergy extends SingleQuadParticle {

    public Vec3D targetPos;
    private final SpriteSet spriteSet;

    public ParticleEnergy(ClientLevel world, double xPos, double yPos, double zPos, Vec3D targetPos, SpriteSet spriteSet) {
        super(world, xPos, yPos, zPos, spriteSet.first());
        this.targetPos = targetPos;
        this.spriteSet = spriteSet;
        setSprite(spriteSet.get(world.getRandom()));
        hasPhysics = false;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        setSprite(spriteSet.get(level.getRandom()));

        Vec3D dir = Vec3D.getDirectionVec(new Vec3D(x, y, z), targetPos);
        double speed = 0.5D;
        xd = dir.x * speed;
        yd = dir.y * speed;
        zd = dir.z * speed;
        this.move(this.xd, this.yd, this.zd);

        if (age++ > lifetime || Utils.getDistance(x, y, z, targetPos.x, targetPos.y, targetPos.z) < 0.5) {
            remove();
        }
    }

    public static class Factory implements ParticleProvider<IntParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet p_i50823_1_) {
            this.spriteSet = p_i50823_1_;
        }

        @Override
        public Particle createParticle(IntParticleData data, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource random) {
            ParticleEnergy particleEnergy = new ParticleEnergy(world, x, y, z, new Vec3D(xSpeed, ySpeed, zSpeed), spriteSet);

            int[] list = data.get();
            if (list.length >= 3) {
                particleEnergy.setColor(list[0] / 255F, list[1] / 255F, list[2] / 255F);
            }

            if (list.length >= 4) {
                particleEnergy.scale(list[3] / 100F);
            }

            return particleEnergy;
        }
    }
}
