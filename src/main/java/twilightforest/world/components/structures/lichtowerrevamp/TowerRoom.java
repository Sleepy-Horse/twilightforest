package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
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
import twilightforest.util.jigsaw.JigsawUtil;
import twilightforest.world.components.structures.TwilightJigsawPiece;

public final class TowerRoom extends TwilightJigsawPiece implements PieceBeardifierModifier {
	private final int roomSize;
	private final boolean generateGround;

	public TowerRoom(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.TOWER_ROOM.get(), compoundTag, ctx, readSettings(compoundTag));

		this.roomSize = compoundTag.getInt("room_size");
		this.generateGround = compoundTag.getBoolean("gen_ground");
	}

	public TowerRoom(StructureTemplateManager structureManager, int genDepth, JigsawPlaceContext jigsawContext, ResourceLocation roomId, int roomSize, boolean generateGround) {
		super(TFStructurePieceTypes.TOWER_ROOM.get(), genDepth, structureManager, roomId, jigsawContext);

		this.roomSize = roomSize;
		this.generateGround = generateGround;
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag structureTag) {
		super.addAdditionalSaveData(ctx, structureTag);

		structureTag.putInt("room_size", this.roomSize);
		structureTag.putBoolean("gen_ground", this.generateGround);
	}

	@Override
	protected void processJigsaw(StructurePiece parent, StructurePieceAccessor pieceAccessor, RandomSource random, JigsawRecord connection) {
		switch (connection.target()) {
			case "twilightforest:lich_tower/bridge" -> {
				if (this.genDepth > 30 || this.roomSize < 1)
					break;

				TowerBridge.putBridge(this, pieceAccessor, random, connection.pos(), connection.orientation(), this.structureManager, false, this.roomSize - random.nextInt(2) - (random.nextInt(this.genDepth) >> 1), this.getGenDepth() + 1, this.generateGround);
			}
			case "twilightforest:lich_tower/roof" -> {
				JigsawRecord sourceJigsaw = this.getSourceJigsaw();
				Direction sourceDirection = JigsawUtil.getAbsoluteHorizontal(sourceJigsaw != null ? sourceJigsaw.orientation() : connection.orientation());

				FrontAndTop orientationToMatch = FrontAndTop.fromFrontAndTop(Direction.UP, sourceDirection.getOpposite());
				BoundingBox roofExtension = BoundingBoxUtils.extrusionFrom(this.boundingBox.minX(), this.boundingBox.maxY() + 1, this.boundingBox.minZ(), this.boundingBox.maxX(), this.boundingBox.maxY() + 1, this.boundingBox.maxZ(), sourceDirection, 1);
				StructurePiece collidingPiece = pieceAccessor.findCollisionPiece(roofExtension);
				boolean sideRoofsOnly = collidingPiece != null;

				for (ResourceLocation roofLocation : TowerPieces.roofs(random, this.roomSize, sideRoofsOnly)) {
					JigsawPlaceContext placeableJunction = JigsawPlaceContext.pickPlaceableJunction(this, connection.pos(), orientationToMatch, this.structureManager, roofLocation, "twilightforest:lich_tower/roof", random);

					if (placeableJunction != null) {
						TowerRoof roofPiece = new TowerRoof(this.genDepth + 1, this.structureManager, roofLocation, placeableJunction);

						if (pieceAccessor.findCollisionPiece(roofPiece.generationCollisionBox()) == null) {
							pieceAccessor.addPiece(roofPiece);
							roofPiece.addChildren(this, pieceAccessor, random);

							break;
						}
					}
				}
			}
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
		return this.generateGround ? TerrainAdjustment.BEARD_THIN : TerrainAdjustment.NONE;
	}

	@Override
	public int getGroundLevelDelta() {
		return 0;
	}
}
