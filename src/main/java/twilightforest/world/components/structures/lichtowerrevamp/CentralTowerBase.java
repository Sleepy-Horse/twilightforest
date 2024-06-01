package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.world.components.structures.TwilightTemplateStructurePiece;


public final class CentralTowerBase extends TwilightTemplateStructurePiece {

	public CentralTowerBase(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.CENTRAL_TOWER_BASE.get(), compoundTag, ctx, readSettings(compoundTag));
	}

	public CentralTowerBase(StructureTemplateManager structureManager, StructurePlaceSettings placeSettings, BlockPos startPosition) {
		super(TFStructurePieceTypes.CENTRAL_TOWER_BASE.get(), 1, structureManager, TwilightForestMod.prefix("lich_tower/tower_base"), placeSettings, startPosition);
	}

	@Override
	protected void handleDataMarker(String label, BlockPos pos, ServerLevelAccessor levelAccessor, RandomSource random, BoundingBox boundingBox) {
	}
}
