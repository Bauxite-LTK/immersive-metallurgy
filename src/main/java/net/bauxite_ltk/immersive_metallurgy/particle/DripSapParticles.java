package net.bauxite_ltk.immersive_metallurgy.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DripSapParticles extends TextureSheetParticle {

    private final SpriteSet sprites;
    private int phase;            // 0=hang, 1=fall, 2=land
    private int hangTicksLeft;

    private DripSapParticles(ClientLevel level, double x, double y, double z,
                               double xSpeed, double ySpeed, double zSpeed,
                               SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;

        // ---- START ----
        this.phase = 0;
        this.hangTicksLeft = 10 + level.random.nextInt(20); // hang phase 10~30 tick
        this.gravity = 0;
        this.friction = 1.0F;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.lifetime = 120;
        this.quadSize = 0.1F;
        this.alpha = 0.92F;


        this.rCol = 0.45F;
        this.gCol = 0.30F;
        this.bCol = 0.12F;


        this.setSprite(sprites.get(0, 2));
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }


    @Override
    public void tick() {
        this.oRoll = this.roll;

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.phase == 0) {
            // ── HANG ──
            this.hangTicksLeft--;
            if (this.hangTicksLeft <= 0) {
                startFalling();
            }

        } else if (this.phase == 1) {
            // ── FALL──
            this.yd -= 0.06F;
            this.move(this.xd, this.yd, this.zd);

            if (this.onGround) {
                // FALL -> LAND
                startLanding();
            }

        } else {
            // ── LAND ──
            this.yd = 0;
            this.alpha *= 0.7F;
            if (this.alpha < 0.01F) {
                this.remove();
            }
        }

        this.age++;
        if (this.age >= this.lifetime) {
            this.remove();
        }
    }

    private void startFalling() {
        this.phase = 1;
        this.gravity = 0.06F;
        this.friction = 0.98F;
        this.yd = -0.03F; // 初始下落速度
        this.quadSize = 0.09F;

        this.setSprite(this.sprites.get(1, 2));

    }

    private void startLanding() {
        this.phase = 2;
        this.onGround = false;
        this.yd = 0;
        this.xd = 0;
        this.zd = 0;
        this.gravity = 0;
        this.quadSize = 0.18F;
        this.alpha = 0.85F;
        this.lifetime = this.age + 8;

        float f = Mth.randomBetween(this.random, 0.6F, 1.0F);
        this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER, SoundSource.BLOCKS, f, 1.0F, false);

        this.setSprite(this.sprites.get(2, 2));

    }


    @OnlyIn(Dist.CLIENT)
    public static class ParticleProvider implements net.minecraft.client.particle.ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public ParticleProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new DripSapParticles(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
