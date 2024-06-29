package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.BoundingBoxUtils;
import twilightforest.util.jigsaw.JigsawPlaceContext;
import twilightforest.util.jigsaw.JigsawRecord;
import twilightforest.world.components.processors.SoftReplaceProcessor;
import twilightforest.world.components.structures.TwilightJigsawPiece;

public class TowerRoof extends TwilightJigsawPiece implements PieceBeardifierModifier {
	public TowerRoof(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.TOWER_ROOF.get(), compoundTag, ctx, readSettings(compoundTag).addProcessor(SoftReplaceProcessor.INSTANCE));
	}

	public TowerRoof(int genDepth, StructureTemplateManager structureManager, ResourceLocation templateLocation, JigsawPlaceContext jigsawContext) {
		super(TFStructurePieceTypes.TOWER_ROOF.get(), genDepth, structureManager, templateLocation, jigsawContext);

		this.placeSettings().addProcessor(SoftReplaceProcessor.INSTANCE);
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

	@Override
	protected void processJigsaw(StructurePiece parent, StructurePieceAccessor pieceAccessor, RandomSource random, JigsawRecord connection) {
	}

	@Override
	protected void handleDataMarker(String pName, BlockPos pPos, ServerLevelAccessor pLevel, RandomSource pRandom, BoundingBox pBox) {
	}

	public BoundingBox generationCollisionBox() {
		if (Math.min(this.boundingBox.getYSpan(), Math.min(this.boundingBox.getXSpan(), this.boundingBox.getZSpan())) < 2) {
			return BoundingBoxUtils.safeRetract(this.boundingBox, Direction.DOWN, 1);
		}

		return BoundingBoxUtils.cloneWithAdjustments(this.boundingBox, 1, 1, 1, -1, 0, -1);
	}
}
