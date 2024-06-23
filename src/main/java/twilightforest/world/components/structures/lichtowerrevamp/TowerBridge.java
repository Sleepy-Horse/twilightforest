package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.jigsaw.JigsawPlaceContext;
import twilightforest.util.jigsaw.JigsawRecord;
import twilightforest.world.components.structures.TwilightJigsawPiece;

public final class TowerBridge extends TwilightJigsawPiece implements PieceBeardifierModifier {
	public TowerBridge(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.TOWER_BRIDGE.get(), compoundTag, ctx, readSettings(compoundTag));
	}

	public TowerBridge(StructureTemplateManager structureManager, int genDepth, JigsawPlaceContext jigsawContext, ResourceLocation templateLocation) {
		super(TFStructurePieceTypes.TOWER_BRIDGE.get(), genDepth, structureManager, templateLocation, jigsawContext);
	}

	@Override
	protected void processJigsaw(StructurePiece parent, StructurePieceAccessor pieceAccessor, RandomSource random, JigsawRecord record) {
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

	public static void putBridge(TwilightJigsawPiece parent, StructurePieceAccessor pieceAccessor, RandomSource random, BlockPos sourceJigsawPos, FrontAndTop sourceOrientation, StructureTemplateManager structureManager, boolean fromCentralTower, int roomMaxSize, int depth, boolean canPutGround) {
		for (ResourceLocation bridgeId : TowerPieces.bridges(fromCentralTower, random)) {
			JigsawPlaceContext placeableJunction = JigsawPlaceContext.pickPlaceableJunction(parent, sourceJigsawPos, sourceOrientation, structureManager, bridgeId, fromCentralTower ? "twilightforest:lich_tower/bridge_center" : "twilightforest:lich_tower/bridge", random);

			if (placeableJunction != null) {
				TowerBridge bridge = new TowerBridge(structureManager, depth + 1, placeableJunction, bridgeId);

				if (bridge.tryGenerateRoom(random, pieceAccessor, roomMaxSize, canPutGround)) {
					pieceAccessor.addPiece(bridge);
					bridge.addChildren(parent, pieceAccessor, random);
					return;
				}
			}
		}
	}

	public boolean tryGenerateRoom(final RandomSource random, final StructurePieceAccessor structureStart, final int roomMaxSize, boolean canPutGround) {
		for (JigsawRecord generatingPoint : this.getSpareJigsaws()) {
			for (int roomSize = Math.max(0, roomMaxSize - 1); roomSize >= 0; roomSize--) {
				boolean result = this.tryPlaceRoom(random, structureStart, TowerPieces.getARoom(random, roomSize), generatingPoint, roomSize, canPutGround);

				if (result) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean tryPlaceRoom(RandomSource random, StructurePieceAccessor pieceAccessor, @Nullable ResourceLocation roomId, JigsawRecord connection, int roomSize, boolean canPutGround) {
		if (roomId != null) {
			JigsawPlaceContext placeableJunction = JigsawPlaceContext.pickPlaceableJunction(this, connection.pos(), connection.orientation(), this.structureManager, roomId, "twilightforest:lich_tower/room", random);

			if (placeableJunction == null) {
				return false;
			}

			boolean generateGround = canPutGround && roomSize > 0 && placeableJunction.templatePos().subtract(connection.pos()).getY() < 7;

			StructurePiece room = new TowerRoom(this.structureManager, this.genDepth + 1, placeableJunction, roomId, roomSize, generateGround);

			if (pieceAccessor.findCollisionPiece(room.getBoundingBox()) != null) {
				return false;
			}

			pieceAccessor.addPiece(room);
			room.addChildren(this, pieceAccessor, random);

			return true;
		}

		return false;
	}
}
