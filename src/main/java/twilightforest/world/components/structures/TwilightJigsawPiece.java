package twilightforest.world.components.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;
import twilightforest.util.jigsaw.JigsawPlaceContext;
import twilightforest.util.jigsaw.JigsawRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TwilightJigsawPiece extends TwilightTemplateStructurePiece {
	@Nullable
	private final JigsawRecord sourceJigsaw;
	private final List<JigsawRecord> spareJigsaws;

	public TwilightJigsawPiece(StructurePieceType structurePieceType, CompoundTag compoundTag, StructurePieceSerializationContext ctx, StructurePlaceSettings rl2SettingsFunction) {
		super(structurePieceType, compoundTag, ctx, rl2SettingsFunction);

		this.sourceJigsaw = readSourceFromNBT(compoundTag);
		this.spareJigsaws = readConnectionsFromNBT(compoundTag);
	}

	public TwilightJigsawPiece(StructurePieceType type, int genDepth, StructureTemplateManager structureManager, ResourceLocation templateLocation, StructurePlaceSettings placeSettings, BlockPos startPosition) {
		this(type, genDepth, structureManager, templateLocation, placeSettings, startPosition, null, JigsawRecord.allFromTemplate(structureManager, templateLocation, placeSettings));
	}

	public TwilightJigsawPiece(StructurePieceType type, int genDepth, StructureTemplateManager structureManager, ResourceLocation templateLocation, JigsawPlaceContext jigsawContext) {
		this(type, genDepth, structureManager, templateLocation, jigsawContext.placementSettings(), jigsawContext.templatePos(), jigsawContext.seedJigsaw(), jigsawContext.spareJigsaws());
	}

	private TwilightJigsawPiece(StructurePieceType type, int genDepth, StructureTemplateManager structureManager, ResourceLocation templateLocation, StructurePlaceSettings placeSettings, BlockPos startPosition, @Nullable JigsawRecord sourceJigsaw, List<JigsawRecord> spareJigsaws) {
		super(type, genDepth, structureManager, templateLocation, placeSettings, startPosition);

		this.sourceJigsaw = sourceJigsaw;
		this.spareJigsaws = Collections.unmodifiableList(spareJigsaws);
	}

	@Nullable
	protected static JigsawRecord readSourceFromNBT(CompoundTag structureTag) {
		if (!structureTag.contains("source", Tag.TAG_COMPOUND)) {
			return null;
		}

		return JigsawRecord.fromTag(structureTag.getCompound("source"));
	}

	protected static List<JigsawRecord> readConnectionsFromNBT(CompoundTag structureTag) {
		ListTag connections = structureTag.getList("connections", Tag.TAG_COMPOUND);

		List<JigsawRecord> connectionsList = new ArrayList<>();

		for (Tag tagEntry : connections) {
			if (tagEntry instanceof CompoundTag tag) {
				connectionsList.add(JigsawRecord.fromTag(tag));
			}
		}

		return Collections.unmodifiableList(connectionsList);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag structureTag) {
		super.addAdditionalSaveData(ctx, structureTag);

		if (this.sourceJigsaw != null) {
			structureTag.put("source", this.sourceJigsaw.toTag());
		}

		ListTag tags = new ListTag();

		for (JigsawRecord record : this.spareJigsaws) {
			tags.add(record.toTag());
		}

		structureTag.put("connections", tags);
	}

	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor pieceAccessor, RandomSource random) {
		super.addChildren(parent, pieceAccessor, random);

		for (JigsawRecord record : this.spareJigsaws) {
			this.processJigsaw(parent, pieceAccessor, random, record);
		}
	}

	protected abstract void processJigsaw(StructurePiece parent, StructurePieceAccessor pieceAccessor, RandomSource random, JigsawRecord connection);

	@Nullable
	public JigsawRecord getSourceJigsaw() {
		return this.sourceJigsaw;
	}

	public List<JigsawRecord> getSpareJigsaws() {
		return this.spareJigsaws;
	}
}
