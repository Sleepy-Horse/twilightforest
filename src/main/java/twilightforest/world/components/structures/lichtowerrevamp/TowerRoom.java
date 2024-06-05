package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.BoundingBoxUtils;
import twilightforest.util.JigsawUtil;
import twilightforest.world.components.structures.TwilightTemplateStructurePiece;

import java.util.List;

public final class TowerRoom extends TwilightTemplateStructurePiece implements PieceBeardifierModifier {
	public TowerRoom(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.TOWER_ROOM.get(), compoundTag, ctx, readSettings(compoundTag));
	}

	public TowerRoom(StructureTemplateManager structureManager, int genDepth, StructurePlaceSettings placeSettings, BlockPos startPosition, ResourceLocation roomId) {
		super(TFStructurePieceTypes.TOWER_ROOM.get(), genDepth, structureManager, roomId, placeSettings, startPosition);
	}

	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor structureStart, RandomSource random) {
		if (this.genDepth > 6)
			return;

		TowerBridge.generateBridges(random, this.structureManager, structureStart, this, this.genDepth, 3, false);
	}

	public static boolean canExpand(StructurePieceAccessor structureStart, StructureTemplate template, StructurePlaceSettings placeSettings, BlockPos templatePosition) {
		BoundingBox templateBounds = template.getBoundingBox(placeSettings, templatePosition).inflatedBy(-1);

		if (BoundingBoxUtils.isEmpty(templateBounds))
			return false;

		List<StructureTemplate.StructureBlockInfo> sourceJigsaws = JigsawUtil.findConnectableJigsaws("bridge", "target", template, placeSettings, null);

		for (StructureTemplate.StructureBlockInfo info : sourceJigsaws) {
			BoundingBox shifted = BoundingBoxUtils.shiftBoxTowards(templateBounds, JigsawBlock.getFrontFacing(info.state()), 5);
			if (structureStart.findCollisionPiece(shifted) != null) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void handleDataMarker(String label, BlockPos pos, ServerLevelAccessor levelAccessor, RandomSource random, BoundingBox boundingBox) {
	}

	@Override
	public BoundingBox getBeardifierBox() {
		return this.boundingBox;
	}

	@Override
	public TerrainAdjustment getTerrainAdjustment() {
		return TerrainAdjustment.NONE;
	}

	@Override
	public int getGroundLevelDelta() {
		return 0;
	}
}
