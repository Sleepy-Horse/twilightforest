package twilightforest.client;

import com.ibm.icu.text.RuleBasedNumberFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.map.RegisterMapDecorationRenderersEvent;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.*;
import twilightforest.client.model.entity.newmodels.*;
import twilightforest.client.particle.*;
import twilightforest.client.renderer.CursedSpawnerRenderer;
import twilightforest.client.renderer.entity.*;
import twilightforest.client.renderer.entity.newmodels.*;
import twilightforest.client.renderer.map.ConqueredMapIconRenderer;
import twilightforest.client.renderer.map.MagicMapPlayerIconRenderer;
import twilightforest.client.renderer.tileentity.*;
import twilightforest.config.TFConfig;
import twilightforest.entity.TFPart;
import twilightforest.entity.boss.HydraHead;
import twilightforest.entity.boss.HydraNeck;
import twilightforest.entity.boss.NagaSegment;
import twilightforest.entity.boss.SnowQueenIceShield;
import twilightforest.init.*;
import twilightforest.item.*;
import twilightforest.util.woods.TFWoodTypes;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = TwilightForestMod.ID)
public class TFClientSetup {

	public static boolean optifinePresent = false;

	public static void init(IEventBus bus) {
		TFShaders.init(bus);
	}

	@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME, modid = TwilightForestMod.ID)
	public static class ForgeEvents {

		private static boolean firstTitleScreenShown = false;

		@SubscribeEvent
		public static void showOptifineWarning(ScreenEvent.Init.Post event) {
			if (firstTitleScreenShown || !(event.getScreen() instanceof TitleScreen)) return;

			// Registering this resource listener earlier than the main screen will cause a crash
			// Yes, crashing happens if registered to RegisterClientReloadListenersEvent
			if (Minecraft.getInstance().getResourceManager() instanceof ReloadableResourceManager resourceManager) {
				resourceManager.registerReloadListener(ISTER.INSTANCE.get());
				TwilightForestMod.LOGGER.debug("Registered ISTER listener");
			}

			if (optifinePresent && !TFConfig.disableOptifineNagScreen) {
				Minecraft.getInstance().setScreen(new OptifineWarningScreen(event.getScreen()));
			}

			firstTitleScreenShown = true;
		}

		@SubscribeEvent
		public static void customizeSplashes(ScreenEvent.Init.Post event) {
			if (event.getScreen() instanceof TitleScreen title) {
				SplashRenderer renderer = title.splash;
				if (renderer != null) {
					LocalDate date = LocalDate.now();
					if (date.getMonth() == Month.AUGUST && date.getDayOfMonth() == 19) {
						RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.US, RuleBasedNumberFormat.ORDINAL);
						renderer.splash = String.format("Happy %s birthday to the Twilight Forest!", formatter.format(date.getYear() - 2011));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent evt) {
		try {
			Class.forName("net.optifine.Config");
			optifinePresent = true;
		} catch (ClassNotFoundException e) {
			optifinePresent = false;
		}

		evt.enqueueWork(() -> {
			Sheets.addWoodType(TFWoodTypes.TWILIGHT_OAK_WOOD_TYPE);
			Sheets.addWoodType(TFWoodTypes.CANOPY_WOOD_TYPE);
			Sheets.addWoodType(TFWoodTypes.MANGROVE_WOOD_TYPE);
			Sheets.addWoodType(TFWoodTypes.DARK_WOOD_TYPE);
			Sheets.addWoodType(TFWoodTypes.TIME_WOOD_TYPE);
			Sheets.addWoodType(TFWoodTypes.TRANSFORMATION_WOOD_TYPE);
			Sheets.addWoodType(TFWoodTypes.MINING_WOOD_TYPE);
			Sheets.addWoodType(TFWoodTypes.SORTING_WOOD_TYPE);
		});
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(JappaPackReloadListener.INSTANCE);
		MagicPaintingTextureManager.instance = new MagicPaintingTextureManager(Minecraft.getInstance().getTextureManager());
		event.registerReloadListener(MagicPaintingTextureManager.instance);
	}

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(TFMenuTypes.UNCRAFTING.get(), UncraftingScreen::new);
	}

	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		BooleanSupplier jappa = JappaPackReloadListener.INSTANCE.uncachedJappaPackCheck();
		event.registerEntityRenderer(TFEntities.BOAR.get(), m -> !jappa.getAsBoolean() ? new BoarRenderer(m, new BoarModel<>(m.bakeLayer(TFModelLayers.NEW_BOAR))) : new NewBoarRenderer(m, new NewBoarModel<>(m.bakeLayer(TFModelLayers.BOAR))));
		event.registerEntityRenderer(TFEntities.BIGHORN_SHEEP.get(), m -> new BighornRenderer(m, new NewBighornModel<>(m.bakeLayer(!jappa.getAsBoolean() ? TFModelLayers.NEW_BIGHORN_SHEEP : TFModelLayers.BIGHORN_SHEEP)), new BighornFurLayer(m.bakeLayer(TFModelLayers.BIGHORN_SHEEP_FUR)), 0.7F));
		event.registerEntityRenderer(TFEntities.DEER.get(), m -> new TFGenericMobRenderer<>(m, !jappa.getAsBoolean() ? new DeerModel(m.bakeLayer(TFModelLayers.NEW_DEER)) : new NewDeerModel(m.bakeLayer(TFModelLayers.DEER)), 0.7F, "wilddeer.png"));
		event.registerEntityRenderer(TFEntities.REDCAP.get(), m -> new TFBipedRenderer<>(m, !jappa.getAsBoolean() ? new RedcapModel<>(m.bakeLayer(TFModelLayers.NEW_REDCAP)) : new NewRedcapModel<>(m.bakeLayer(TFModelLayers.REDCAP)), new NewRedcapModel<>(m.bakeLayer(TFModelLayers.REDCAP_ARMOR_INNER)), new NewRedcapModel<>(m.bakeLayer(TFModelLayers.REDCAP_ARMOR_OUTER)), 0.4F, "redcap.png"));
		event.registerEntityRenderer(TFEntities.SKELETON_DRUID.get(), m -> new TFBipedRenderer<>(m, new SkeletonDruidModel(m.bakeLayer(TFModelLayers.SKELETON_DRUID)), 0.5F, "skeletondruid.png"));
		event.registerEntityRenderer(TFEntities.HOSTILE_WOLF.get(), HostileWolfRenderer::new);
		event.registerEntityRenderer(TFEntities.WRAITH.get(), m -> new WraithRenderer(m, new WraithModel(m.bakeLayer(TFModelLayers.WRAITH)), 0.5F));
		event.registerEntityRenderer(TFEntities.HYDRA.get(), m -> !jappa.getAsBoolean() ? new HydraRenderer(m, new HydraModel(m.bakeLayer(TFModelLayers.NEW_HYDRA)), 4.0F) : new NewHydraRenderer(m, new NewHydraModel(m.bakeLayer(TFModelLayers.HYDRA)), 4.0F));
		event.registerEntityRenderer(TFEntities.LICH.get(), m -> new LichRenderer(m, new LichModel(m.bakeLayer(TFModelLayers.LICH)), 0.6F));
		event.registerEntityRenderer(TFEntities.PENGUIN.get(), m -> new BirdRenderer<>(m, new PenguinModel(m.bakeLayer(TFModelLayers.PENGUIN)), 0.375F, "penguin.png"));
		event.registerEntityRenderer(TFEntities.LICH_MINION.get(), m -> new TFBipedRenderer<>(m, new LichMinionModel(m.bakeLayer(TFModelLayers.LICH_MINION)), new LichMinionModel(m.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)), new LichMinionModel(m.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)), 0.5F, "textures/entity/zombie/zombie.png"));
		event.registerEntityRenderer(TFEntities.LOYAL_ZOMBIE.get(), m -> new TFBipedRenderer<>(m, new LoyalZombieModel(m.bakeLayer(TFModelLayers.LOYAL_ZOMBIE)), new LoyalZombieModel(m.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)), new LoyalZombieModel(m.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)), 0.5F, "textures/entity/zombie/zombie.png"));
		event.registerEntityRenderer(TFEntities.TINY_BIRD.get(), m -> !jappa.getAsBoolean() ? new TinyBirdRenderer(m, new TinyBirdModel(m.bakeLayer(TFModelLayers.NEW_TINY_BIRD)), 0.3F) : new NewTinyBirdRenderer(m, new NewTinyBirdModel(m.bakeLayer(TFModelLayers.TINY_BIRD)), 0.3F));
		event.registerEntityRenderer(TFEntities.SQUIRREL.get(), m -> new TFGenericMobRenderer<>(m, !jappa.getAsBoolean() ? new SquirrelModel(m.bakeLayer(TFModelLayers.NEW_SQUIRREL)) : new NewSquirrelModel(m.bakeLayer(TFModelLayers.SQUIRREL)), 0.3F, "squirrel2.png"));
		event.registerEntityRenderer(TFEntities.DWARF_RABBIT.get(), m -> new BunnyRenderer(m, new BunnyModel(m.bakeLayer(TFModelLayers.BUNNY)), 0.3F));
		event.registerEntityRenderer(TFEntities.RAVEN.get(), m -> new BirdRenderer<>(m, !jappa.getAsBoolean() ? new RavenModel(m.bakeLayer(TFModelLayers.NEW_RAVEN)) : new NewRavenModel(m.bakeLayer(TFModelLayers.RAVEN)), 0.3F, "raven.png"));
		event.registerEntityRenderer(TFEntities.QUEST_RAM.get(), m -> !jappa.getAsBoolean() ? new QuestRamRenderer(m, new QuestRamModel(m.bakeLayer(TFModelLayers.NEW_QUEST_RAM))) : new NewQuestRamRenderer(m, new NewQuestRamModel(m.bakeLayer(TFModelLayers.QUEST_RAM))));
		event.registerEntityRenderer(TFEntities.KOBOLD.get(), m -> !jappa.getAsBoolean() ? new KoboldRenderer(m, new KoboldModel(m.bakeLayer(TFModelLayers.NEW_KOBOLD)), 0.4F, "kobold.png") : new NewKoboldRenderer(m, new NewKoboldModel(m.bakeLayer(TFModelLayers.KOBOLD)), 0.4F, "kobold.png"));
		//event.registerEntityRenderer(TFEntities.BOGGARD.get(), m -> new RenderTFBiped<>(m, new BipedModel<>(0), 0.625F, "kobold.png"));
		event.registerEntityRenderer(TFEntities.MOSQUITO_SWARM.get(), MosquitoSwarmRenderer::new);
		event.registerEntityRenderer(TFEntities.DEATH_TOME.get(), m -> new TFGenericMobRenderer<>(m, new DeathTomeModel(m.bakeLayer(TFModelLayers.DEATH_TOME)), 0.3F, "textures/entity/enchanting_table_book.png"));
		event.registerEntityRenderer(TFEntities.MINOTAUR.get(), m -> new TFBipedRenderer<>(m, !jappa.getAsBoolean() ? new MinotaurModel(m.bakeLayer(TFModelLayers.NEW_MINOTAUR)) : new NewMinotaurModel(m.bakeLayer(TFModelLayers.MINOTAUR)), 0.625F, "minotaur.png"));
		event.registerEntityRenderer(TFEntities.MINOSHROOM.get(), m -> !jappa.getAsBoolean() ? new MinoshroomRenderer(m, new MinoshroomModel(m.bakeLayer(TFModelLayers.NEW_MINOSHROOM)), 0.625F) : new NewMinoshroomRenderer(m, new NewMinoshroomModel(m.bakeLayer(TFModelLayers.MINOSHROOM)), 0.625F));
		event.registerEntityRenderer(TFEntities.FIRE_BEETLE.get(), m -> new TFGenericMobRenderer<>(m, !jappa.getAsBoolean() ? new FireBeetleModel(m.bakeLayer(TFModelLayers.NEW_FIRE_BEETLE)) : new NewFireBeetleModel(m.bakeLayer(TFModelLayers.FIRE_BEETLE)), 0.8F, "firebeetle.png"));
		event.registerEntityRenderer(TFEntities.SLIME_BEETLE.get(), m -> !jappa.getAsBoolean() ? new SlimeBeetleRenderer(m, new SlimeBeetleModel(m.bakeLayer(TFModelLayers.NEW_SLIME_BEETLE)), 0.6F) : new NewSlimeBeetleRenderer(m, new NewSlimeBeetleModel(m.bakeLayer(TFModelLayers.SLIME_BEETLE)), 0.6F));
		event.registerEntityRenderer(TFEntities.PINCH_BEETLE.get(), m -> new TFGenericMobRenderer<>(m, !jappa.getAsBoolean() ? new PinchBeetleModel(m.bakeLayer(TFModelLayers.NEW_PINCH_BEETLE)) : new NewPinchBeetleModel(m.bakeLayer(TFModelLayers.PINCH_BEETLE)), 0.6F, "pinchbeetle.png"));
		event.registerEntityRenderer(TFEntities.MIST_WOLF.get(), MistWolfRenderer::new);
		event.registerEntityRenderer(TFEntities.CARMINITE_GHASTLING.get(), m -> new TFGhastRenderer<>(m, new TFGhastModel<>(m.bakeLayer(TFModelLayers.CARMINITE_GHASTLING)), 0.625F));
		event.registerEntityRenderer(TFEntities.CARMINITE_GOLEM.get(), m -> new CarminiteGolemRenderer<>(m, new CarminiteGolemModel<>(m.bakeLayer(TFModelLayers.CARMINITE_GOLEM)), 0.75F));
		event.registerEntityRenderer(TFEntities.TOWERWOOD_BORER.get(), m -> new TFGenericMobRenderer<>(m, new SilverfishModel<>(m.bakeLayer(ModelLayers.SILVERFISH)), 0.3F, "towertermite.png"));
		event.registerEntityRenderer(TFEntities.CARMINITE_GHASTGUARD.get(), m -> new CarminiteGhastRenderer<>(m, new TFGhastModel<>(m.bakeLayer(TFModelLayers.CARMINITE_GHASTGUARD)), 3.0F));
		event.registerEntityRenderer(TFEntities.UR_GHAST.get(), m -> !jappa.getAsBoolean() ? new UrGhastRenderer(m, new UrGhastModel(m.bakeLayer(TFModelLayers.NEW_UR_GHAST)), 8.0F, 24F) : new NewUrGhastRenderer(m, new NewUrGhastModel(m.bakeLayer(TFModelLayers.UR_GHAST)), 8.0F, 24F));
		event.registerEntityRenderer(TFEntities.BLOCKCHAIN_GOBLIN.get(), m -> !jappa.getAsBoolean() ? new BlockChainGoblinRenderer<>(m, new BlockChainGoblinModel<>(m.bakeLayer(TFModelLayers.NEW_BLOCKCHAIN_GOBLIN)), 0.4F) : new NewBlockChainGoblinRenderer<>(m, new NewBlockChainGoblinModel<>(m.bakeLayer(TFModelLayers.BLOCKCHAIN_GOBLIN)), 0.4F));
		event.registerEntityRenderer(TFEntities.UPPER_GOBLIN_KNIGHT.get(), m -> !jappa.getAsBoolean() ? new UpperGoblinKnightRenderer(m, new UpperGoblinKnightModel(m.bakeLayer(TFModelLayers.NEW_UPPER_GOBLIN_KNIGHT)), 0.625F) : new NewUpperGoblinKnightRenderer(m, new NewUpperGoblinKnightModel(m.bakeLayer(TFModelLayers.UPPER_GOBLIN_KNIGHT)), 0.625F));
		event.registerEntityRenderer(TFEntities.LOWER_GOBLIN_KNIGHT.get(), m -> new TFBipedRenderer<>(m, !jappa.getAsBoolean() ? new LowerGoblinKnightModel(m.bakeLayer(TFModelLayers.NEW_LOWER_GOBLIN_KNIGHT)) : new NewLowerGoblinKnightModel(m.bakeLayer(TFModelLayers.LOWER_GOBLIN_KNIGHT)), 0.625F, "doublegoblin.png"));
		event.registerEntityRenderer(TFEntities.HELMET_CRAB.get(), m -> new TFGenericMobRenderer<>(m, !jappa.getAsBoolean() ? new HelmetCrabModel(m.bakeLayer(TFModelLayers.NEW_HELMET_CRAB)) : new NewHelmetCrabModel(m.bakeLayer(TFModelLayers.HELMET_CRAB)), 0.625F, "helmetcrab.png"));
		event.registerEntityRenderer(TFEntities.KNIGHT_PHANTOM.get(), m -> new KnightPhantomRenderer(m, new KnightPhantomModel(m.bakeLayer(TFModelLayers.KNIGHT_PHANTOM)), 0.625F));
		event.registerEntityRenderer(TFEntities.NAGA.get(), m -> !jappa.getAsBoolean() ? new NagaRenderer<>(m, new NagaModel<>(m.bakeLayer(TFModelLayers.NEW_NAGA)), 1.45F) : new NewNagaRenderer<>(m, new NewNagaModel<>(m.bakeLayer(TFModelLayers.NAGA)), 1.45F));
		event.registerEntityRenderer(TFEntities.SWARM_SPIDER.get(), SwarmSpiderRenderer::new);
		event.registerEntityRenderer(TFEntities.KING_SPIDER.get(), KingSpiderRenderer::new);
		event.registerEntityRenderer(TFEntities.CARMINITE_BROODLING.get(), CarminiteBroodlingRenderer::new);
		event.registerEntityRenderer(TFEntities.HEDGE_SPIDER.get(), HedgeSpiderRenderer::new);
		event.registerEntityRenderer(TFEntities.REDCAP_SAPPER.get(), m -> new TFBipedRenderer<>(m, !jappa.getAsBoolean() ? new RedcapModel<>(m.bakeLayer(TFModelLayers.NEW_REDCAP)) : new NewRedcapModel<>(m.bakeLayer(TFModelLayers.REDCAP)), new NewRedcapModel<>(m.bakeLayer(TFModelLayers.REDCAP_ARMOR_INNER)), new NewRedcapModel<>(m.bakeLayer(TFModelLayers.REDCAP_ARMOR_OUTER)), 0.4F, "redcapsapper.png"));
		event.registerEntityRenderer(TFEntities.MAZE_SLIME.get(), m -> new MazeSlimeRenderer(m, 0.625F));
		event.registerEntityRenderer(TFEntities.YETI.get(), m -> new TFBipedRenderer<>(m, new YetiModel<>(m.bakeLayer(TFModelLayers.YETI)), 0.625F, "yeti2.png"));
		event.registerEntityRenderer(TFEntities.PROTECTION_BOX.get(), ProtectionBoxRenderer::new);
		event.registerEntityRenderer(TFEntities.MAGIC_PAINTING.get(), MagicPaintingRenderer::new);
		event.registerEntityRenderer(TFEntities.ALPHA_YETI.get(), m -> new TFBipedRenderer<>(m, new AlphaYetiModel(m.bakeLayer(TFModelLayers.ALPHA_YETI)), 1.75F, "yetialpha.png"));
		event.registerEntityRenderer(TFEntities.WINTER_WOLF.get(), WinterWolfRenderer::new);
		event.registerEntityRenderer(TFEntities.SNOW_GUARDIAN.get(), m -> new SnowGuardianRenderer(m, new NoopModel<>(m.bakeLayer(TFModelLayers.NOOP))));
		event.registerEntityRenderer(TFEntities.STABLE_ICE_CORE.get(), m -> new StableIceCoreRenderer(m, new StableIceCoreModel(m.bakeLayer(TFModelLayers.STABLE_ICE_CORE))));
		event.registerEntityRenderer(TFEntities.UNSTABLE_ICE_CORE.get(), m -> new UnstableIceCoreRenderer<>(m, new UnstableIceCoreModel<>(m.bakeLayer(TFModelLayers.UNSTABLE_ICE_CORE))));
		event.registerEntityRenderer(TFEntities.SNOW_QUEEN.get(), m -> !jappa.getAsBoolean() ? new SnowQueenRenderer(m, new SnowQueenModel(m.bakeLayer(TFModelLayers.NEW_SNOW_QUEEN))) : new NewSnowQueenRenderer(m, new NewSnowQueenModel(m.bakeLayer(TFModelLayers.SNOW_QUEEN))));
		event.registerEntityRenderer(TFEntities.TROLL.get(), m -> new TFBipedRenderer<>(m, !jappa.getAsBoolean() ? new TrollModel(m.bakeLayer(TFModelLayers.NEW_TROLL)) : new NewTrollModel(m.bakeLayer(TFModelLayers.TROLL)), 0.625F, "troll.png"));
		event.registerEntityRenderer(TFEntities.GIANT_MINER.get(), TFGiantRenderer::new);
		event.registerEntityRenderer(TFEntities.ARMORED_GIANT.get(), TFGiantRenderer::new);
		event.registerEntityRenderer(TFEntities.ICE_CRYSTAL.get(), IceCrystalRenderer::new);
		event.registerEntityRenderer(TFEntities.CHAIN_BLOCK.get(), BlockChainRenderer::new);
		event.registerEntityRenderer(TFEntities.CUBE_OF_ANNIHILATION.get(), CubeOfAnnihilationRenderer::new);
		event.registerEntityRenderer(TFEntities.HARBINGER_CUBE.get(), HarbingerCubeRenderer::new);
		event.registerEntityRenderer(TFEntities.ADHERENT.get(), AdherentRenderer::new);
		event.registerEntityRenderer(TFEntities.ROVING_CUBE.get(), RovingCubeRenderer::new);
		event.registerEntityRenderer(TFEntities.RISING_ZOMBIE.get(), m -> new TFBipedRenderer<>(m, new RisingZombieModel(m.bakeLayer(TFModelLayers.RISING_ZOMBIE)), new RisingZombieModel(m.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)), new RisingZombieModel(m.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)), 0.5F, "textures/entity/zombie/zombie.png"));
		event.registerEntityRenderer(TFEntities.PLATEAU_BOSS.get(), NoopRenderer::new);

		// projectiles
		event.registerEntityRenderer(TFEntities.NATURE_BOLT.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(TFEntities.LICH_BOLT.get(), c -> new CustomProjectileTextureRenderer(c, TwilightForestMod.prefix("textures/item/twilight_orb.png")));
		event.registerEntityRenderer(TFEntities.WAND_BOLT.get(), c -> new CustomProjectileTextureRenderer(c, TwilightForestMod.prefix("textures/item/twilight_orb.png")));
		event.registerEntityRenderer(TFEntities.TOME_BOLT.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(TFEntities.HYDRA_MORTAR.get(), HydraMortarRenderer::new);
		event.registerEntityRenderer(TFEntities.SLIME_BLOB.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(TFEntities.MOONWORM_SHOT.get(), MoonwormShotRenderer::new);
		event.registerEntityRenderer(TFEntities.CHARM_EFFECT.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(TFEntities.LICH_BOMB.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(TFEntities.THROWN_WEP.get(), ThrownWepRenderer::new);
		event.registerEntityRenderer(TFEntities.FALLING_ICE.get(), FallingIceRenderer::new);
		event.registerEntityRenderer(TFEntities.THROWN_ICE.get(), ThrownIceRenderer::new);
		event.registerEntityRenderer(TFEntities.THROWN_BLOCK.get(), ThrownBlockRenderer::new);
		event.registerEntityRenderer(TFEntities.ICE_SNOWBALL.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(TFEntities.SLIDER.get(), SlideBlockRenderer::new);
		event.registerEntityRenderer(TFEntities.SEEKER_ARROW.get(), DefaultArrowRenderer::new);
		event.registerEntityRenderer(TFEntities.ICE_ARROW.get(), DefaultArrowRenderer::new);

		// Block Entities
		event.registerBlockEntityRenderer(TFBlockEntities.FIREFLY.get(), FireflyTileEntityRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.CICADA.get(), CicadaTileEntityRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.MOONWORM.get(), MoonwormTileEntityRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.TROPHY.get(), TrophyTileEntityRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.TF_CHEST.get(), TFChestTileEntityRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.TF_TRAPPED_CHEST.get(), TFChestTileEntityRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.KEEPSAKE_CASKET.get(), CasketTileEntityRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.SKULL_CANDLE.get(), SkullCandleTileEntityRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.RED_THREAD.get(), RedThreadRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.CANDELABRA.get(), CandelabraTileEntityRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.JAR.get(), JarRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.MASON_JAR.get(), JarRenderer.MasonJarRenderer::new);
		event.registerBlockEntityRenderer(TFBlockEntities.CURSED_SPAWNER.get(), CursedSpawnerRenderer::new);
	}

	@SuppressWarnings("deprecation")
	public static class BakedMultiPartRenderers {

		private static final Map<ResourceLocation, LazyLoadedValue<EntityRenderer<?>>> renderers = new HashMap<>();

		public static void bakeMultiPartRenderers(EntityRendererProvider.Context context) {
			BooleanSupplier jappa = JappaPackReloadListener.INSTANCE.uncachedJappaPackCheck();
			renderers.put(TFPart.RENDERER, new LazyLoadedValue<>(() -> new NoopRenderer<>(context)));
			renderers.put(HydraHead.RENDERER, new LazyLoadedValue<>(() -> !jappa.getAsBoolean() ? new HydraHeadRenderer(context) : new NewHydraHeadRenderer(context)));
			renderers.put(HydraNeck.RENDERER, new LazyLoadedValue<>(() -> !jappa.getAsBoolean() ? new HydraNeckRenderer(context) : new NewHydraNeckRenderer(context)));
			renderers.put(SnowQueenIceShield.RENDERER, new LazyLoadedValue<>(() -> new SnowQueenIceShieldLayer<>(context)));
			renderers.put(NagaSegment.RENDERER, new LazyLoadedValue<>(() -> !jappa.getAsBoolean() ? new NagaSegmentRenderer<>(context) : new NewNagaSegmentRenderer<>(context)));
		}

		public static EntityRenderer<?> lookup(ResourceLocation location) {
			return renderers.get(location).get();
		}
	}

	@SubscribeEvent
	public static void attachRenderLayers(EntityRenderersEvent.AddLayers event) {
		TFClientSetup.BakedMultiPartRenderers.bakeMultiPartRenderers(event.getContext());
		for (EntityType<?> type : event.getEntityTypes()) {
			var renderer = event.getRenderer(type);
			if (renderer instanceof LivingEntityRenderer<?, ?> living) {
				attachRenderLayers(living);
			}
		}

		event.getSkins().forEach(renderer -> {
			LivingEntityRenderer<Player, EntityModel<Player>> skin = event.getSkin(renderer);
			attachRenderLayers(Objects.requireNonNull(skin));
		});
	}

	private static <T extends LivingEntity, M extends EntityModel<T>> void attachRenderLayers(LivingEntityRenderer<T, M> renderer) {
		renderer.addLayer(new ShieldLayer<>(renderer));
		renderer.addLayer(new IceLayer<>(renderer));
	}

	@SubscribeEvent
	public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(TFParticleType.LARGE_FLAME.get(), LargeFlameParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.LEAF_RUNE.get(), LeafRuneParticle.Factory::new);
		event.registerSpecial(TFParticleType.BOSS_TEAR.get(), new GhastTearParticle.Factory());
		event.registerSpriteSet(TFParticleType.GHAST_TRAP.get(), GhastTrapParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.PROTECTION.get(), ProtectionParticle.Factory::new); //probably not a good idea, but worth a shot
		event.registerSpriteSet(TFParticleType.SNOW.get(), SnowParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.SNOW_GUARDIAN.get(), SnowGuardianParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.SNOW_WARNING.get(), SnowWarningParticle.SimpleFactory::new);
		event.registerSpriteSet(TFParticleType.EXTENDED_SNOW_WARNING.get(), SnowWarningParticle.ExtendedFactory::new);
		event.registerSpriteSet(TFParticleType.ICE_BEAM.get(), IceBeamParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.ANNIHILATE.get(), AnnihilateParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.HUGE_SMOKE.get(), SmokeScaleParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.FIREFLY.get(), FireflyParticle.StationaryProvider::new);
		event.registerSpriteSet(TFParticleType.WANDERING_FIREFLY.get(), FireflyParticle.WanderingProvider::new);
		event.registerSpriteSet(TFParticleType.PARTICLE_SPAWNER_FIREFLY.get(), FireflyParticle.ParticleSpawnerProvider::new);
		event.registerSpriteSet(TFParticleType.FALLEN_LEAF.get(), LeafParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.DIM_FLAME.get(), FlameParticle.SmallFlameProvider::new);
		event.registerSpriteSet(TFParticleType.OMINOUS_FLAME.get(), FlameParticle.SmallFlameProvider::new);
		event.registerSpriteSet(TFParticleType.SORTING_PARTICLE.get(), SortingParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.TRANSFORMATION_PARTICLE.get(), TransformationParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.LOG_CORE_PARTICLE.get(), LogCoreParticle.Factory::new);
		event.registerSpriteSet(TFParticleType.CLOUD_PUFF.get(), CloudPuffParticle.Factory::new);
	}

	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerBlock(new IClientBlockExtensions() {
			@Override
			public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
				if (level.random.nextBoolean() && target instanceof BlockHitResult hitResult) { // No clue why the parameter isn't blockHitResult, this should be always true, but we check just in case
					BlockPos pos = hitResult.getBlockPos();
					BlockState blockstate = level.getBlockState(pos);
					if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
						Direction side = hitResult.getDirection();

						int posX = pos.getX();
						int posY = pos.getY();
						int posZ = pos.getZ();

						AABB aabb = blockstate.getShape(level, pos).bounds();
						double x = (double) posX + level.random.nextDouble() * (aabb.maxX - aabb.minX - (double) 0.2F) + (double) 0.1F + aabb.minX;
						double y = (double) posY + level.random.nextDouble() * (aabb.maxY - aabb.minY - (double) 0.2F) + (double) 0.1F + aabb.minY;
						double z = (double) posZ + level.random.nextDouble() * (aabb.maxZ - aabb.minZ - (double) 0.2F) + (double) 0.1F + aabb.minZ;

						if (side == Direction.DOWN) y = (double) posY + aabb.minY - (double) 0.1F;
						if (side == Direction.UP) y = (double) posY + aabb.maxY + (double) 0.1F;

						if (side == Direction.NORTH) z = (double) posZ + aabb.minZ - (double) 0.1F;
						if (side == Direction.SOUTH) z = (double) posZ + aabb.maxZ + (double) 0.1F;

						if (side == Direction.WEST) x = (double) posX + aabb.minX - (double) 0.1F;
						if (side == Direction.EAST) x = (double) posX + aabb.maxX + (double) 0.1F;

						Particle particle = Minecraft.getInstance().particleEngine.createParticle(TFParticleType.CLOUD_PUFF.get(), x, y, z, (double) side.getStepX() * 0.01D, (double) side.getStepY() * 0.01D, (double) side.getStepZ() * 0.01D);
						if (particle == null) return true;
						manager.add(particle);
					}
				}
				return true;
			}

			@Override
			public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
				state.getShape(level, pos).forAllBoxes((boxX, boxY, boxZ, boxX1, boxY1, boxZ1) -> {
					double xSize = Math.min(1.0D, boxX1 - boxX);
					double ySize = Math.min(1.0D, boxY1 - boxY);
					double zSize = Math.min(1.0D, boxZ1 - boxZ);

					int xMax = Math.max(2, Mth.ceil(xSize / 0.25D));
					int yMax = Math.max(2, Mth.ceil(ySize / 0.25D));
					int zMax = Math.max(2, Mth.ceil(zSize / 0.25D));

					for (int xSlice = 0; xSlice < xMax; ++xSlice) {
						if (level.random.nextInt(3) == 1) continue;
						for (int ySlice = 0; ySlice < yMax; ++ySlice) {
							if (level.random.nextInt(3) == 1) continue;
							for (int zSlice = 0; zSlice < zMax; ++zSlice) {
								if (level.random.nextInt(3) == 1) continue;

								double speedX = ((double) xSlice + 0.5D) / (double) xMax;
								double speedY = ((double) ySlice + 0.5D) / (double) yMax;
								double speedZ = ((double) zSlice + 0.5D) / (double) zMax;

								double x = speedX * xSize + boxX;
								double y = speedY * ySize + boxY;
								double z = speedZ * zSize + boxZ;

								speedX = (speedX - 0.5D) * 0.05D;
								speedY = (speedY - 0.5D) * 0.05D;
								speedZ = (speedZ - 0.5D) * 0.05D;

								Particle particle = Minecraft.getInstance().particleEngine.createParticle(TFParticleType.CLOUD_PUFF.get(), (double) pos.getX() + x, (double) pos.getY() + y, (double) pos.getZ() + z, speedX, speedY, speedZ);
								if (particle == null) return;
								manager.add(particle);
							}
						}
					}
				});
				return true;
			}
		}, TFBlocks.WISPY_CLOUD.get(), TFBlocks.RAINY_CLOUD.get(), TFBlocks.SNOWY_CLOUD.get(), TFBlocks.FLUFFY_CLOUD.get());

		event.registerItem(ISTER.CLIENT_ITEM_EXTENSION,
			TFBlocks.CICADA.asItem(), TFBlocks.FIREFLY.asItem(), TFBlocks.MOONWORM.asItem(), TFBlocks.KEEPSAKE_CASKET.asItem(), TFBlocks.CANDELABRA.asItem(),
			TFItems.CICADA_JAR.get(), TFItems.FIREFLY_JAR.get(), TFItems.MASON_JAR.get(), TFItems.KNIGHTMETAL_SHIELD.get(),
			TFBlocks.TWILIGHT_OAK_CHEST.asItem(), TFBlocks.CANOPY_CHEST.asItem(), TFBlocks.MANGROVE_CHEST.asItem(), TFBlocks.DARK_CHEST.asItem(), TFBlocks.TIME_CHEST.asItem(), TFBlocks.TRANSFORMATION_CHEST.asItem(), TFBlocks.MINING_CHEST.asItem(), TFBlocks.SORTING_CHEST.asItem(),
			TFBlocks.TWILIGHT_OAK_TRAPPED_CHEST.asItem(), TFBlocks.CANOPY_TRAPPED_CHEST.asItem(), TFBlocks.MANGROVE_TRAPPED_CHEST.asItem(), TFBlocks.DARK_TRAPPED_CHEST.asItem(), TFBlocks.TIME_TRAPPED_CHEST.asItem(), TFBlocks.TRANSFORMATION_TRAPPED_CHEST.asItem(), TFBlocks.MINING_TRAPPED_CHEST.asItem(), TFBlocks.SORTING_TRAPPED_CHEST.asItem(),
			TFItems.NAGA_TROPHY.get(), TFItems.LICH_TROPHY.get(), TFItems.MINOSHROOM_TROPHY.get(), TFItems.HYDRA_TROPHY.get(), TFItems.KNIGHT_PHANTOM_TROPHY.get(), TFItems.UR_GHAST_TROPHY.get(), TFItems.ALPHA_YETI_TROPHY.get(), TFItems.SNOW_QUEEN_TROPHY.get(), TFItems.QUEST_RAM_TROPHY.get(),
			TFItems.CREEPER_SKULL_CANDLE.get(), TFItems.PIGLIN_SKULL_CANDLE.get(), TFItems.PLAYER_SKULL_CANDLE.get(), TFItems.SKELETON_SKULL_CANDLE.get(), TFItems.WITHER_SKELETON_SKULL_CANDLE.get(), TFItems.ZOMBIE_SKULL_CANDLE.get());

		event.registerItem(ArcticArmorItem.ArmorRender.INSTANCE, TFItems.ARCTIC_HELMET.get(), TFItems.ARCTIC_CHESTPLATE.get(), TFItems.ARCTIC_LEGGINGS.get(), TFItems.ARCTIC_BOOTS.get());
		event.registerItem(FieryArmorItem.ArmorRender.INSTANCE, TFItems.FIERY_HELMET.get(), TFItems.FIERY_CHESTPLATE.get(), TFItems.FIERY_LEGGINGS.get(), TFItems.FIERY_BOOTS.get());
		event.registerItem(KnightmetalArmorItem.ArmorRender.INSTANCE, TFItems.KNIGHTMETAL_HELMET.get(), TFItems.KNIGHTMETAL_CHESTPLATE.get(), TFItems.KNIGHTMETAL_LEGGINGS.get(), TFItems.KNIGHTMETAL_BOOTS.get());
		event.registerItem(PhantomArmorItem.ArmorRender.INSTANCE, TFItems.PHANTOM_HELMET.get(), TFItems.PHANTOM_CHESTPLATE.get());
		event.registerItem(YetiArmorItem.ArmorRender.INSTANCE, TFItems.YETI_HELMET.get(), TFItems.YETI_CHESTPLATE.get(), TFItems.YETI_LEGGINGS.get(), TFItems.YETI_BOOTS.get());
	}

	@SubscribeEvent
	public static void registerMapDecorators(RegisterMapDecorationRenderersEvent event) {
		event.register(MapDecorationTypes.PLAYER.value(), new MagicMapPlayerIconRenderer());
		event.register(TFMapDecorations.QUEST_GROVE.get(), new ConqueredMapIconRenderer());
		event.register(TFMapDecorations.NAGA_COURTYARD.get(), new ConqueredMapIconRenderer());
		event.register(TFMapDecorations.LICH_TOWER.get(), new ConqueredMapIconRenderer());
		event.register(TFMapDecorations.LABYRINTH.get(), new ConqueredMapIconRenderer());
		event.register(TFMapDecorations.HYDRA_LAIR.get(), new ConqueredMapIconRenderer());
		event.register(TFMapDecorations.KNIGHT_STRONGHOLD.get(), new ConqueredMapIconRenderer());
		event.register(TFMapDecorations.DARK_TOWER.get(), new ConqueredMapIconRenderer());
		event.register(TFMapDecorations.YETI_LAIR.get(), new ConqueredMapIconRenderer());
		event.register(TFMapDecorations.AURORA_PALACE.get(), new ConqueredMapIconRenderer());
		event.register(TFMapDecorations.FINAL_CASTLE.get(), new ConqueredMapIconRenderer());
	}
}
