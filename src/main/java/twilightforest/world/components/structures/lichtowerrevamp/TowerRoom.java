package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
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
import twilightforest.util.jigsaw.JigsawPlaceContext;
import twilightforest.util.jigsaw.JigsawRecord;
import twilightforest.world.components.structures.TwilightJigsawPiece;

public final class TowerRoom extends TwilightJigsawPiece implements PieceBeardifierModifier {
	private final int roomSize;

	public TowerRoom(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.TOWER_ROOM.get(), compoundTag, ctx, readSettings(compoundTag));

		this.roomSize = compoundTag.getInt("room_size");
	}

	public TowerRoom(StructureTemplateManager structureManager, int genDepth, JigsawPlaceContext jigsawContext, ResourceLocation roomId, int roomSize) {
		super(TFStructurePieceTypes.TOWER_ROOM.get(), genDepth, structureManager, roomId, jigsawContext);

		this.roomSize = roomSize;
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag structureTag) {
		super.addAdditionalSaveData(ctx, structureTag);

		structureTag.putInt("room_size", this.roomSize);
	}

	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor structureStart, RandomSource random) {
		if (this.genDepth > 30 || this.roomSize < 1)
			return;

		super.addChildren(parent, structureStart, random);
	}

	@Override
	protected void processJigsaw(StructurePiece parent, StructurePieceAccessor pieceAccessor, RandomSource random, JigsawRecord record) {
		if ("twilightforest:lich_tower/bridge".equals(record.target())) {
			TowerBridge.putBridge(this, pieceAccessor, random, record.pos(), record.orientation(), this.structureManager, false, this.roomSize - random.nextInt(2) - (random.nextInt(this.genDepth) >> 1), this.getGenDepth() + 1);
		}
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
