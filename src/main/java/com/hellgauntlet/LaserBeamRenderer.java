package com.hellgauntlet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Draws the beam as a long black eight-sided tube from the gauntlet to the front of the laser. */
public class LaserBeamRenderer extends EntityRenderer<LaserBeamEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HellGauntletMod.MODID, "textures/entity/void_sphere.png");
    private static final int SIDES = 8;

    public LaserBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(LaserBeamEntity entity, Frustum frustum, double x, double y, double z) {
        return true; // the beam can be 64 blocks long
    }

    @Override
    public void render(LaserBeamEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        float age = entity.tickCount + partialTick;
        float length = LaserBeamEntity.headLength(entity.getLength(), age);
        if (length <= 0.0F) {
            return;
        }

        float opacity = 1.0F;
        int fadeStart = entity.getFadeStart();
        if (fadeStart >= 0 && age > fadeStart) {
            opacity = 1.0F - (age - fadeStart) / LaserBeamEntity.FADE_TICKS;
        }
        int alpha = (int) (Math.max(0.0F, Math.min(1.0F, opacity)) * 255.0F);
        if (alpha <= 0) {
            return;
        }

        Vec3 dir = entity.getLookAngle();
        Vec3 up = Math.abs(dir.y) > 0.99 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 u = dir.cross(up).normalize();
        Vec3 v = dir.cross(u).normalize();
        float r = LaserBeamEntity.THICKNESS;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();

        for (int i = 0; i < SIDES; i++) {
            double a0 = 2.0 * Math.PI * i / SIDES;
            double a1 = 2.0 * Math.PI * (i + 1) / SIDES;
            Vec3 n0 = u.scale(Math.cos(a0)).add(v.scale(Math.sin(a0)));
            Vec3 n1 = u.scale(Math.cos(a1)).add(v.scale(Math.sin(a1)));
            Vec3 front = dir.scale(length);

            vertex(vc, pose, n0.scale(r), n0, alpha);
            vertex(vc, pose, n0.scale(r).add(front), n0, alpha);
            vertex(vc, pose, n1.scale(r).add(front), n1, alpha);
            vertex(vc, pose, n1.scale(r), n1, alpha);
        }

        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, Vec3 p, Vec3 n, int alpha) {
        vc.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(0, 0, 0, alpha)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, (float) n.x, (float) n.y, (float) n.z);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserBeamEntity entity) {
        return TEXTURE;
    }
}
