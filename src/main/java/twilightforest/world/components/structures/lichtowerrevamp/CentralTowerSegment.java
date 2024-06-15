package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.jigsaw.JigsawPlaceContext;
import twilightforest.util.jigsaw.JigsawRecord;
import twilightforest.world.components.structures.TwilightJigsawPiece;

public final class CentralTowerSegment extends TwilightJigsawPiece implements PieceBeardifierModifier {
	public CentralTowerSegment(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.CENTRAL_TOWER.get(), compoundTag, ctx, readSettings(compoundTag));
	}

	public CentralTowerSegment(StructureTemplateManager structureManager, int genDepth, JigsawPlaceContext jigsawContext) {
		super(TFStructurePieceTypes.CENTRAL_TOWER.get(), genDepth, structureManager, TwilightForestMod.prefix("lich_tower/tower_slice"), jigsawContext);
	}

	public static void putTowerSegment(StructurePieceAccessor pieceAccessor, RandomSource random, BlockPos sourceJigsawPos, FrontAndTop sourceOrientation, TwilightJigsawPiece parent, StructureTemplateManager structureManager) {
		JigsawPlaceContext placeableJunction = JigsawPlaceContext.pickPlaceableJunction(parent, sourceJigsawPos, sourceOrientation, structureManager, TwilightForestMod.prefix("lich_tower/tower_slice"), "twilightforest:lich_tower/tower_below", random);

		if (placeableJunction == null) return;

		StructurePiece towerBase = new CentralTowerSegment(structureManager, parent.getGenDepth() + 1, placeableJunction);
		pieceAccessor.addPiece(towerBase);
		towerBase.addChildren(parent, pieceAccessor, random);
	}

	@Override
	protected void processJigsaw(StructurePiece parent, StructurePieceAccessor pieceAccessor, RandomSource random, JigsawRecord record) {
		if ("twilightforest:lich_tower/tower_below".equals(record.target())) {
			if (this.genDepth < random.nextInt(4) + 6) {
				CentralTowerSegment.putTowerSegment(pieceAccessor, random, record.pos(), record.orientation(), this, this.structureManager);
			} else {
				JigsawPlaceContext placeableJunction = JigsawPlaceContext.pickPlaceableJunction(this, record.pos(), record.orientation(), this.structureManager, TwilightForestMod.prefix("lich_tower/tower_boss_room"), "twilightforest:lich_tower/tower_below", random);

				if (placeableJunction == null) {
					return;
				}

				StructurePiece bossRoom = new BossRoom(this.structureManager, placeableJunction);
				pieceAccessor.addPiece(bossRoom);
				bossRoom.addChildren(this, pieceAccessor, random);
			}
		} else if ("twilightforest:lich_tower/bridge".equals(record.target()) && random.nextInt(this.genDepth * 2) == 0) {
			TowerBridge.putBridge(this, pieceAccessor, random, record.pos(), record.orientation(), this.structureManager, true, 4 - random.nextInt(2) - (random.nextInt(this.genDepth) >> 1) - (this.genDepth >> 2), this.getGenDepth() + 1);
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
