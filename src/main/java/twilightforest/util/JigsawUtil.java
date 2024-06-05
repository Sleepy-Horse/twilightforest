package twilightforest.util;

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
import net.neoforged.neoforge.common.util.TriPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	public static List<StructureTemplate.StructureBlockInfo> findConnectableJigsaws(String jigsawLabel, String matchInKey, @Nullable StructureTemplate template, StructurePlaceSettings settings, @Nullable RandomSource random) {
		if (template == null)
			return List.of();

		List<StructureTemplate.StructureBlockInfo> returnables = new ArrayList<>();

		for (StructureTemplate.StructureBlockInfo jigsawInfo : template.filterBlocks(BlockPos.ZERO, settings, Blocks.JIGSAW)) {
			CompoundTag nbt = jigsawInfo.nbt();
			if (nbt != null && jigsawLabel.equals(nbt.getString(matchInKey))) {
				returnables.add(jigsawInfo);
			}
		}

		if (random != null) {
			Util.shuffle(returnables, random);
			// "Stable" sorting - preserves order of "equal" priorities, as arranged by prior shuffling.
			SinglePoolElement.sortBySelectionPriority(returnables);
		}

		return returnables;
	}

	// Structure pieces being placed via 'placer' BiConsumer MUST match param otherTemplate in size. Or else you will encounter size discrepancies!
	public static int generateAtJunctions(RandomSource random, TemplateStructurePiece sourcePiece, StructureTemplate otherTemplate, String jigsawLabel, boolean canFlip, int placeAttemptLimit, TriPredicate<BlockPos, StructurePlaceSettings, Direction> placer) {
		List<StructureTemplate.StructureBlockInfo> sourceJigsaws = findConnectableJigsaws(jigsawLabel, "target", sourcePiece.template(), sourcePiece.placeSettings(), random);

		if (sourceJigsaws.isEmpty()) return 0;

		// Adjoinable Template must use "baseline" configuration
		List<StructureTemplate.StructureBlockInfo> connectableJigsaws = findConnectableJigsaws(jigsawLabel, "name", otherTemplate, new StructurePlaceSettings(), random);

		return generatePiecesAtJunctions(random, sourceJigsaws, connectableJigsaws, canFlip, sourcePiece.placeSettings().getMirror(), sourcePiece.placeSettings().getRotation(), sourcePiece.templatePosition(), placer, placeAttemptLimit);
	}

	public static int generatePiecesAtJunctions(RandomSource random, List<StructureTemplate.StructureBlockInfo> sourceJigsaws, List<StructureTemplate.StructureBlockInfo> connectableJigsaws, boolean canFlip, Mirror sourceMirror, Rotation sourceRotation, BlockPos sourceTemplatePos, TriPredicate<BlockPos, StructurePlaceSettings, Direction> placer, int placeAttemptLimit) {
		if (connectableJigsaws.isEmpty()) return 0;

		int tries = 0;
		int successes = 0;

		for (final StructureTemplate.StructureBlockInfo sourceJigsaw : sourceJigsaws) {
			Optional<StructureTemplate.StructureBlockInfo> matchedJigsaw = connectableJigsaws.stream()
				.filter(info -> canRearrangeForConnection(sourceJigsaw, info))
				.findFirst();
			// Filter for Jigsaws that can re-orient for connection to the source jigsaw
			if (matchedJigsaw.isPresent()) {
				boolean isVertical = JigsawBlock.getFrontFacing(sourceJigsaw.state()).getAxis().isVertical();
				boolean success = generateAtJunction(isVertical, random, canFlip, sourceMirror, sourceRotation, sourceTemplatePos, sourceJigsaw, matchedJigsaw.get(), placer);

				if (success) {
					successes++;

					if (++tries >= placeAttemptLimit) {
						return successes;
					}
				}

				// Now that we used the list, re-shuffle for next loop iteration
				Util.shuffle(connectableJigsaws, random);
				SinglePoolElement.sortBySelectionPriority(connectableJigsaws);
			}
		}

		return successes;
	}

	private static boolean generateAtJunction(boolean isVertical, RandomSource random, boolean canFlip, Mirror sourceMirror, Rotation sourceRotation, BlockPos sourceTemplatePos, StructureTemplate.StructureBlockInfo sourceJigsaw, StructureTemplate.StructureBlockInfo otherJigsaw, TriPredicate<BlockPos, StructurePlaceSettings, Direction> placer) {
		boolean doMirror = canFlip && random.nextBoolean();

		Direction sourceFront = sourceMirror.mirror(JigsawBlock.getFrontFacing(sourceJigsaw.state()));
		Direction otherFront = otherJigsaw.state().getValue(JigsawBlock.ORIENTATION).front();
		BlockPos otherOffset = otherJigsaw.pos();

		if (isVertical) {
			Direction sourceTop = sourceMirror.mirror(JigsawBlock.getTopFacing(sourceJigsaw.state()));
			Direction otherTop = JigsawBlock.getTopFacing(otherJigsaw.state());

			return tryPlace(sourceTemplatePos, placer, doMirror, RotationUtil.mirrorOverAxis(doMirror, otherTop.getAxis()), otherOffset, sourceFront, RotationUtil.getRelativeRotation(otherTop, sourceTop), sourceJigsaw.pos());
		} else {
			if (sourceMirror != Mirror.NONE && sourceRotation.rotate(Direction.NORTH).getAxis() == Direction.Axis.X)
				sourceFront = sourceFront.getOpposite();

			return tryPlace(sourceTemplatePos, placer, doMirror, RotationUtil.mirrorOverAxis(doMirror, otherFront.getAxis()), otherOffset, sourceFront, RotationUtil.getRelativeRotation(otherFront.getOpposite(), sourceFront), sourceJigsaw.pos());
		}
	}

	private static boolean tryPlace(BlockPos sourceTemplatePos, TriPredicate<BlockPos, StructurePlaceSettings, Direction> placer, boolean doMirror, Mirror mirror, BlockPos otherOffset, Direction sourceFront, Rotation relativeRotation, BlockPos sourceJigsawPos) {
		BlockPos processedOffset = doMirror ? RotationUtil.mirrorOffset(mirror, otherOffset) : otherOffset;
		BlockPos placePos = sourceTemplatePos.offset(sourceJigsawPos).relative(sourceFront).subtract(processedOffset);

		StructurePlaceSettings placementSettings = new StructurePlaceSettings();
		placementSettings.setMirror(mirror);
		placementSettings.setRotation(relativeRotation);
		placementSettings.setRotationPivot(processedOffset);
		return placer.test(placePos, placementSettings, sourceFront);
	}
}
