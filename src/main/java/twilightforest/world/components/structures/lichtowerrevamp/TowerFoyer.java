package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.JigsawUtil;
import twilightforest.world.components.structures.TwilightTemplateStructurePiece;

public final class TowerFoyer extends TwilightTemplateStructurePiece {
	public TowerFoyer(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.TOWER_FOYER.get(), compoundTag, ctx, readSettings(compoundTag));
	}

	public TowerFoyer(StructureTemplateManager structureManager, BlockPos startPosition, Mirror mirror, Rotation rotation) {
		super(TFStructurePieceTypes.TOWER_FOYER.get(), 0, structureManager, TwilightForestMod.prefix("lich_tower/tower_foyer"), makeSettings(rotation, mirror), startPosition);
	}

	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor structureStart, RandomSource random) {
		JigsawUtil.generateAtJunctions(
			random,
			this,
			this.structureManager.getOrCreate(TwilightForestMod.prefix("lich_tower/tower_base")),
			"twilightforest:lich_tower/tower_base", // Jigsaw name label to search within the current template, target label within the other template
			// Jigsaw target label to search within the current template, name label within the other template
			false, // Whether the new piece can be mirrored
			1, // Max attempts
			(pos, config, direction) -> {
				StructurePiece towerBase = new CentralTowerBase(this.structureManager, config, pos);
				structureStart.addPiece(towerBase);
				towerBase.addChildren(this, structureStart, random);
				return true;
			}
		);
	}

	@Override
	protected void handleDataMarker(String label, BlockPos pos, ServerLevelAccessor levelAccessor, RandomSource random, BoundingBox boundingBox) {
	}
}
