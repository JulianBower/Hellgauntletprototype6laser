package com.hellgauntlet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the orb as a spinning, pulsing 3D black cube (like a shulker bullet) from the void_orb item model. */
public class VoidOrbRenderer extends EntityRenderer<VoidOrbEntity> {
    private final ItemRenderer itemRenderer;
    private final ItemStack stack;

    public VoidOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.stack = new ItemStack(ModItems.VOID_ORB.get());
    }

    @Override
    public void render(VoidOrbEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        float t = entity.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.0F, entity.getBbHeight() / 2.0F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(t * 24.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(t * 15.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(t * 9.0F));
        float pulse = 1.1F + 0.12F * (float) Math.sin(t * 0.6F);
        poseStack.scale(pulse, pulse, pulse);
        this.itemRenderer.renderStatic(this.stack, ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidOrbEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
