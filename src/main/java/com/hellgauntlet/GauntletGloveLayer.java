package com.hellgauntlet;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;

/** Draws the gauntlet as a glove wrapped around the hand of whichever arm holds the item. */
public class GauntletGloveLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(HellGauntletMod.MODID, "gauntlet_glove"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HellGauntletMod.MODID, "textures/entity/gauntlet_glove.png");

    private final ModelPart rightGlove;
    private final ModelPart leftGlove;

    public GauntletGloveLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                              EntityModelSet models) {
        super(parent);
        ModelPart root = models.bakeLayer(LAYER);
        this.rightGlove = root.getChild("right_glove");
        this.leftGlove = root.getChild("left_glove");
    }

    /**
     * Arm boxes in the player model run from y = -2 to y = 10 (the hand is at the bottom), so the glove
     * covers y = 5 to 10.5 with a cuff just above and a red knuckle plate at the bottom.
     */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_glove", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, 5.0F, -2.5F, 5.0F, 5.5F, 5.0F)
                        .texOffs(0, 11).addBox(-3.75F, 4.0F, -2.75F, 5.5F, 1.5F, 5.5F)
                        .texOffs(0, 19).addBox(-3.75F, 9.5F, -2.75F, 5.5F, 1.2F, 5.5F),
                PartPose.ZERO);
        root.addOrReplaceChild("left_glove", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.5F, 5.0F, -2.5F, 5.0F, 5.5F, 5.0F)
                        .texOffs(0, 11).addBox(-1.75F, 4.0F, -2.75F, 5.5F, 1.5F, 5.5F)
                        .texOffs(0, 19).addBox(-1.75F, 9.5F, -2.75F, 5.5F, 1.2F, 5.5F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (player.isInvisible()) {
            return;
        }
        boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        if (player.getMainHandItem().getItem() instanceof GauntletItem) {
            draw(poseStack, buffer, light, rightHanded);
        }
        if (player.getOffhandItem().getItem() instanceof GauntletItem) {
            draw(poseStack, buffer, light, !rightHanded);
        }
    }

    private void draw(PoseStack poseStack, MultiBufferSource buffer, int light, boolean rightArm) {
        poseStack.pushPose();
        PlayerModel<AbstractClientPlayer> model = this.getParentModel();
        (rightArm ? model.rightArm : model.leftArm).translateAndRotate(poseStack);
        (rightArm ? this.rightGlove : this.leftGlove).render(poseStack,
                buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
