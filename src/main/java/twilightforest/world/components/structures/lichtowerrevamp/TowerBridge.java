package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
import org.jetbrains.annotations.NotNull;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.BoundingBoxUtils;
import twilightforest.util.JigsawUtil;
import twilightforest.world.components.structures.TwilightTemplateStructurePiece;

import java.util.List;

public final class TowerBridge extends TwilightTemplateStructurePiece implements PieceBeardifierModifier {
	public TowerBridge(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.TOWER_BRIDGE.get(), compoundTag, ctx, readSettings(compoundTag));
	}

	public TowerBridge(StructureTemplateManager structureManager, int genDepth, StructurePlaceSettings placeSettings, BlockPos startPosition, String name) {
		super(TFStructurePieceTypes.TOWER_BRIDGE.get(), genDepth, structureManager, TwilightForestMod.prefix("lich_tower/" + name), placeSettings, startPosition);
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

	@Override
	public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
		super.postProcess(pLevel, pStructureManager, pGenerator, pRandom, pBox, pChunkPos, pPos);

		pLevel.setBlock(this.templatePosition().above(), Blocks.BEACON.defaultBlockState(), 3);
		pLevel.setBlock(this.templatePosition().above().above(), Blocks.MAGENTA_GLAZED_TERRACOTTA.defaultBlockState().rotate(this.getRotation()), 3);
	}

	public static void generateBridges(final RandomSource random, final StructureTemplateManager structureManager, final StructurePieceAccessor structureStart, final TemplateStructurePiece sourcePiece, final int depth, int attempts, boolean fromCentralTower) {
		if (attempts < 1)
			return;

		final String name = pickBridge(fromCentralTower, random);
		StructureTemplate bridgeTemplate = structureManager.getOrCreate(TwilightForestMod.prefix("lich_tower/" + name));
		JigsawUtil.generateAtJunctions(
			random,
			sourcePiece,
			bridgeTemplate,
			"twilightforest:lich_tower/bridge",
			false,
			attempts,
			(pos, config, direction) -> {
				TowerBridge bridge = new TowerBridge(structureManager, depth + 1, config, pos, name);

				if (!bridge.tryFitRandomRoom(random, structureStart, depth + 1))
					return false;

				structureStart.addPiece(bridge);
				bridge.addChildren(sourcePiece, structureStart, random);

				return true;
			}
		);
	}

	private static @NotNull String pickBridge(boolean fromCentralTower, RandomSource random) {
		if (fromCentralTower)
			return "central_bridge";

		// TODO More bridge designs
		return random.nextBoolean() ? "room_bridge" : "no_bridge";
	}

	public boolean tryFitRandomRoom(final RandomSource random, final StructurePieceAccessor structureStart, final int depth) {
		ResourceLocation roomId = TowerRooms.getARoom(random, 4 - depth);
		return roomId != null && this.tryPlaceRoom(random, structureStart, depth, roomId);
	}

	private boolean tryPlaceRoom(final RandomSource random, final StructurePieceAccessor structureStart, final int depth, final ResourceLocation roomId) {
		StructureTemplate newTemplate = this.structureManager.getOrCreate(roomId);
		int successes = JigsawUtil.generateAtJunctions(
			random,
			this,
			newTemplate,
			"twilightforest:lich_tower/room",
			false,
			1,
			(pos, config, direction) -> {
				if (!canExpand(structureStart, newTemplate, config, pos, 5))
					return false;

				TowerRoom segment = new TowerRoom(this.structureManager, depth, config, pos, roomId);

				if (structureStart.findCollisionPiece(segment.getBoundingBox()) != null) {
					return false;
				}

				structureStart.addPiece(segment);
				segment.addChildren(this, structureStart, random);
				return true;
			}
		);
		return successes > 0;
	}

	public static boolean canExpand(StructurePieceAccessor structureStart, StructureTemplate template, StructurePlaceSettings placeSettings, BlockPos templatePosition, int bridgeLength) {
		BoundingBox templateBounds = template.getBoundingBox(placeSettings, templatePosition).inflatedBy(-1);

		if (BoundingBoxUtils.isEmpty(templateBounds))
			return false;

		List<StructureTemplate.StructureBlockInfo> sourceJigsaws = JigsawUtil.findConnectableJigsaws("bridge", "target", template, placeSettings, null);

		for (StructureTemplate.StructureBlockInfo info : sourceJigsaws) {
			BoundingBox shifted = BoundingBoxUtils.shiftBoxTowards(templateBounds, JigsawBlock.getFrontFacing(info.state()), bridgeLength);
			if (structureStart.findCollisionPiece(shifted) != null) {
				return false;
			}
		}

		return true;
	}
}
