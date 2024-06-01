package twilightforest.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.ibm.icu.impl.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class JigsawUtil {
	// Determines if other Jigsaw orientation can be rotated in order to match the source Jigsaw orientation
	public static boolean canRearrangeForConnection(StructureTemplate.StructureBlockInfo sourceJigsaw, StructureTemplate.StructureBlockInfo otherJigsaw) {
		FrontAndTop sourceOrientation = sourceJigsaw.state().getValue(JigsawBlock.ORIENTATION);
		FrontAndTop otherOrientation = otherJigsaw.state().getValue(JigsawBlock.ORIENTATION);

		boolean frontFacesAlignable = canBeRotatedToAlign(sourceOrientation.front(), otherOrientation.front());
		boolean topFacesAlignable = canBeRotatedToAlign(sourceOrientation.top().getOpposite(), otherOrientation.top());

		return frontFacesAlignable && topFacesAlignable;
	}

	private static boolean canBeRotatedToAlign(Direction source, Direction target) {
		Direction.Plane sourcePlane = source.getAxis().getPlane();
		boolean planesMatch = sourcePlane == target.getAxis().getPlane();

		if (sourcePlane == Direction.Plane.VERTICAL) {
			return planesMatch && source.getOpposite() == target; // Cannot vertically flip templates
		} else {
			return planesMatch; // If both Jigsaws are horizontal, then they can simply be rotated
		}
	}

	public static List<StructureTemplate.StructureBlockInfo> findConnectableJigsaws(String sourceName, String otherName, @Nullable StructureTemplate template, StructurePlaceSettings settings, RandomSource random) {
		if (template == null)
			return List.of();

		List<StructureTemplate.StructureBlockInfo> returnables = new ArrayList<>();

		for (StructureTemplate.StructureBlockInfo jigsawInfo : template.filterBlocks(BlockPos.ZERO, settings, Blocks.JIGSAW)) {
			CompoundTag nbt = jigsawInfo.nbt();
			if (nbt != null && otherName.equals(nbt.getString("target")) && sourceName.equals(nbt.getString("name"))) {
				returnables.add(jigsawInfo);
			}
		}

		Util.shuffle(returnables, random);
		// "Stable" sorting - preserves order of "equal" priorities, as arranged by prior shuffling.
		SinglePoolElement.sortBySelectionPriority(returnables);

		return returnables;
	}

	@Deprecated
	public static Multimap<String, Pair<FrontAndTop, BlockPos>> getJigsaws(boolean getTargets, StructureTemplate template, StructurePlaceSettings settings) {
		String jigsawKey = getTargets ? "target" : "name";
		ImmutableMultimap.Builder<String, Pair<FrontAndTop, BlockPos>> jigsawConnections = ImmutableMultimap.builder();

		for (StructureTemplate.StructureBlockInfo jigsawInfo : template.filterBlocks(BlockPos.ZERO, settings, Blocks.JIGSAW)) {
			CompoundTag nbt = jigsawInfo.nbt();
			if (nbt != null) {
				String label = nbt.getString(jigsawKey);
				FrontAndTop jigsawOrientation = jigsawInfo.state().getValue(JigsawBlock.ORIENTATION);
				BlockPos relative = jigsawInfo.pos().relative(jigsawOrientation.front());

				jigsawConnections.put(label, Pair.of(jigsawOrientation, relative));
			}
		}

		return jigsawConnections.build();
	}

	public static void generateAtJunctions(RandomSource random, TemplateStructurePiece sourcePiece, StructureTemplate otherTemplate, String sourceLabel, String otherLabel, boolean canFlip, BiConsumer<BlockPos, StructurePlaceSettings> placer) {
		List<StructureTemplate.StructureBlockInfo> sourceJigsaws = findConnectableJigsaws(sourceLabel, otherLabel, sourcePiece.template(), sourcePiece.placeSettings(), random);

		if (sourceJigsaws.isEmpty()) return;

		// Adjoinable Template must use "baseline" configuration
		List<StructureTemplate.StructureBlockInfo> connectableJigsaws = findConnectableJigsaws(otherLabel, sourceLabel, otherTemplate, new StructurePlaceSettings(), random);

		if (connectableJigsaws.isEmpty()) return;

		generatePiecesAtJunctions(random, sourceJigsaws, connectableJigsaws, canFlip, sourcePiece.placeSettings().getMirror(), sourcePiece.placeSettings().getRotation(), sourcePiece.templatePosition(), placer);
	}

	public static void generatePiecesAtJunctions(RandomSource random, List<StructureTemplate.StructureBlockInfo> sourceJigsaws, List<StructureTemplate.StructureBlockInfo> connectableJigsaws, boolean canFlip, Mirror sourceMirror, Rotation sourceRotation, BlockPos sourcePos, BiConsumer<BlockPos, StructurePlaceSettings> placer) {
		for (final StructureTemplate.StructureBlockInfo sourceJigsaw : sourceJigsaws) {
			Optional<StructureTemplate.StructureBlockInfo> matchedJigsaw = connectableJigsaws.stream()
				.filter(info -> canRearrangeForConnection(sourceJigsaw, info))
				.findFirst();
			// Filter for Jigsaws that can re-orient for connection to the source jigsaw
			if (matchedJigsaw.isPresent()) {
				StructureTemplate.StructureBlockInfo otherJigsaw = matchedJigsaw.get();
				boolean doMirror = canFlip && random.nextBoolean();

				Direction sourceFront = sourceMirror.mirror(JigsawBlock.getFrontFacing(sourceJigsaw.state()));
				Direction otherFront = JigsawBlock.getFrontFacing(otherJigsaw.state());

				if (sourceMirror != Mirror.NONE && sourceRotation.rotate(Direction.NORTH).getAxis() == Direction.Axis.X)
					sourceFront = sourceFront.getOpposite();

				Mirror mirror = RotationUtil.mirrorOverAxis(doMirror, otherFront.getAxis());
				Rotation relativeRotation = RotationUtil.getRelativeRotation(otherFront.getOpposite(), sourceFront);
				BlockPos otherOffset = otherJigsaw.pos(); //RotationUtil.mirrorOffset(sourceMirror, otherJigsaw.pos());

				BlockPos processedOffset = doMirror ? RotationUtil.mirrorOffset(mirror, otherOffset) : otherOffset;

				StructurePlaceSettings placementSettings = new StructurePlaceSettings();
				placementSettings.setMirror(mirror);
				placementSettings.setRotation(relativeRotation);
				placementSettings.setRotationPivot(processedOffset);
				//RotationUtil.mirrorPlaceSettings(sourceMirror, placementSettings);

				BlockPos placePos = sourcePos.offset(sourceJigsaw.pos()).relative(sourceFront).subtract(processedOffset);
				placer.accept(placePos, placementSettings);

				// Now that we used the  Re-shuffle for next loop iteration
				Util.shuffle(connectableJigsaws, random);
				SinglePoolElement.sortBySelectionPriority(connectableJigsaws);
			}
		}
	}
}
