package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.JigsawUtil;
import twilightforest.world.components.structures.TwilightTemplateStructurePiece;


public final class CentralTowerSegment extends TwilightTemplateStructurePiece implements PieceBeardifierModifier {
	public CentralTowerSegment(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.CENTRAL_TOWER.get(), compoundTag, ctx, readSettings(compoundTag));
	}

	public CentralTowerSegment(StructureTemplateManager structureManager, int genDepth, StructurePlaceSettings placeSettings, BlockPos startPosition) {
		super(TFStructurePieceTypes.CENTRAL_TOWER.get(), genDepth, structureManager, TwilightForestMod.prefix("lich_tower/tower_slice"), placeSettings, startPosition);
	}

	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor structureStart, RandomSource random) {
		if (this.genDepth < random.nextInt(4) + 6) {
			JigsawUtil.generateAtJunctions(
				random,
				this,
				this.structureManager.getOrCreate(TwilightForestMod.prefix("lich_tower/tower_slice")),
				"twilightforest:lich_tower/tower_above",
				"twilightforest:lich_tower/tower_below",
				false,
				(pos, config) -> {
					StructurePiece segment = new CentralTowerSegment(this.structureManager, this.genDepth + 1, config, pos);
					structureStart.addPiece(segment);
					segment.addChildren(this, structureStart, random);
				}
			);
		} else {
			JigsawUtil.generateAtJunctions(
				random,
				this,
				this.structureManager.getOrCreate(TwilightForestMod.prefix("lich_tower/tower_boss_room")),
				"twilightforest:lich_tower/tower_above",
				"twilightforest:lich_tower/tower_below",
				false,
				(pos, config) -> {
					StructurePiece bossRoom = new BossRoom(this.structureManager, config, pos);
					structureStart.addPiece(bossRoom);
					bossRoom.addChildren(this, structureStart, random);
				}
			);
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
