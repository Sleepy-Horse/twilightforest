package twilightforest.world.components.structures.lichtowerrevamp;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;

public final class TowerRooms {
	// FIXME: Move to DataMap of int -> ResourceLocation[] if possible to obtain RegistryAccess
	private static final ResourceLocation[] thinTowers = new ResourceLocation[] {
		TwilightForestMod.prefix("lich_tower/3x3/fence_thin"),
		TwilightForestMod.prefix("lich_tower/3x3/stair_thin")
	};
	private static final ResourceLocation[] rooms5 = new ResourceLocation[] {
		TwilightForestMod.prefix("lich_tower/5x5/elbow_junction"),
		TwilightForestMod.prefix("lich_tower/5x5/full_junction"),
		TwilightForestMod.prefix("lich_tower/5x5/straight_junction"),
		TwilightForestMod.prefix("lich_tower/5x5/t_junction")
	};
	private static final ResourceLocation[] rooms7 = new ResourceLocation[] {
		TwilightForestMod.prefix("lich_tower/7x7/elbow_junction"),
		TwilightForestMod.prefix("lich_tower/7x7/full_junction"),
		TwilightForestMod.prefix("lich_tower/7x7/straight_junction"),
		TwilightForestMod.prefix("lich_tower/7x7/t_junction")
	};
	private static final ResourceLocation[] rooms9 = new ResourceLocation[] {
		TwilightForestMod.prefix("lich_tower/9x9/elbow_junction"),
		TwilightForestMod.prefix("lich_tower/9x9/full_junction"),
		TwilightForestMod.prefix("lich_tower/9x9/straight_junction"),
		TwilightForestMod.prefix("lich_tower/9x9/t_junction")
	};

	@Nullable
	public static ResourceLocation getARoom(RandomSource randomSource, int size) {
		ResourceLocation[] roomList = switch(Math.min(size, 3)) {
			case 0 -> thinTowers;
			case 1 -> rooms5;
			case 2 -> rooms7;
			case 3 -> rooms9;
			default -> null;
		};

		if (roomList == null || roomList.length == 0) {
			return null;
		} else {
			return Util.getRandom(roomList, randomSource);
		}
	}

	private TowerRooms() {
		throw new IllegalStateException("How did we get here?");
	}
}
