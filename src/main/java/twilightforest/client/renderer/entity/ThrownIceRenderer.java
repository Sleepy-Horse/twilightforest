package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.entity.projectile.IceBomb;

import java.util.Random;

/**
 * [VanillaCopy] of {@link net.minecraft.client.renderer.entity.FallingBlockRenderer} because of generic type restrictions
  */
public class ThrownIceRenderer extends EntityRenderer<IceBomb> {

	public ThrownIceRenderer(EntityRendererProvider.Context manager) {
		super(manager);
		this.shadowRadius = 0.5F;
	}

	@Override
	public void render(IceBomb entity, float yaw, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light) {
		BlockState blockstate = entity.getBlockState();
		if (blockstate.getRenderShape() == RenderShape.MODEL) {
			Level world = entity.getCommandSenderWorld();
			if (blockstate != world.getBlockState(entity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
				ms.pushPose();
				BlockPos blockpos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
				ms.translate(-0.5D, 0.0D, -0.5D);
				BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
				for (net.minecraft.client.renderer.RenderType type : net.minecraft.client.renderer.RenderType.chunkBufferLayers()) {
					if (ItemBlockRenderTypes.canRenderInLayer(blockstate, type)) {
						net.minecraftforge.client.ForgeHooksClient.setRenderType(type);
						blockrendererdispatcher.getModelRenderer().tesselateBlock(world, blockrendererdispatcher.getBlockModel(blockstate), blockstate, blockpos, ms, buffers.getBuffer(type), false, new Random(), blockstate.getSeed(BlockPos.ZERO), OverlayTexture.NO_OVERLAY);
					}
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderType(null);
				ms.popPose();
				super.render(entity, yaw, partialTicks, ms, buffers, light);
			}
		}
	}

	@Override
	public ResourceLocation getTextureLocation(IceBomb entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
