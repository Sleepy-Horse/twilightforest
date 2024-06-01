package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.world.components.structures.TwilightTemplateStructurePiece;


public final class CentralTowerSegment extends TwilightTemplateStructurePiece {
	public CentralTowerSegment(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.CENTRAL_TOWER.get(), compoundTag, ctx, readSettings(compoundTag));
	}

	public CentralTowerSegment(StructureTemplateManager structureManager, Rotation rotation, BlockPos startPosition) {
		this(structureManager, TwilightForestMod.prefix("lich_tower/tower_slice"), makeSettings(rotation), startPosition);
	}

	private CentralTowerSegment(StructureTemplateManager structureManager, ResourceLocation templateLocation, StructurePlaceSettings placeSettings, BlockPos startPosition) {
		super(TFStructurePieceTypes.CENTRAL_TOWER.get(), 0, structureManager, templateLocation, placeSettings, startPosition);
	}

	@Override
	protected void handleDataMarker(String label, BlockPos pos, ServerLevelAccessor levelAccessor, RandomSource random, BoundingBox boundingBox) {

	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag structureTag) {

	}
}
