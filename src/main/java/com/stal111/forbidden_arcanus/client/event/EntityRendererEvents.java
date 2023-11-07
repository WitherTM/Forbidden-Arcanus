package com.stal111.forbidden_arcanus.client.event;

import com.stal111.forbidden_arcanus.ForbiddenArcanus;
import com.stal111.forbidden_arcanus.client.model.*;
import com.stal111.forbidden_arcanus.client.renderer.block.*;
import com.stal111.forbidden_arcanus.client.renderer.entity.*;
import com.stal111.forbidden_arcanus.common.block.skull.ObsidianSkullType;
import com.stal111.forbidden_arcanus.common.entity.ModBoat;
import com.stal111.forbidden_arcanus.core.init.ModBlockEntities;
import com.stal111.forbidden_arcanus.core.init.ModEntities;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Entity Renderer Events <br>
 * Forbidden Arcanus - com.stal111.forbidden_arcanus.client.event.EntityRendererEvents
 *
 * @author stal111
 * @version 1.19 - 2.1.0
 * @since 2021-11-28
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRendererEvents {

    public static final ModelLayerLocation OBSIDIAN_SKULL_LAYER = new ModelLayerLocation(new ResourceLocation(ForbiddenArcanus.MOD_ID, "obsidian_skull"), "main");
    public static final ModelLayerLocation DETAILED_OBSIDIAN_SKULL_LAYER = new ModelLayerLocation(new ResourceLocation(ForbiddenArcanus.MOD_ID, "detailed_obsidian_skull"), "main");

    @SubscribeEvent
    public static void onRegisterRenders(EntityRenderersEvent.RegisterRenderers event) {
        // Block Entities
        event.registerBlockEntityRenderer(ModBlockEntities.NIPA.get(), NipaRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BLACK_HOLE.get(), BlackHoleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.UTREM_JAR.get(), UtremJarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OBSIDIAN_SKULL.get(), SkullBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEPHAESTUS_FORGE.get(), HephaestusForgeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESEARCH_DESK.get(), ResearchDeskRenderer::new);

        // Entities
        event.registerEntityRenderer(ModEntities.BOOM_ARROW.get(), BoomArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.DRACO_ARCANUS_ARROW.get(), DracoArcanusArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.ENERGY_BALL.get(), EnergyBallRenderer::new);
        event.registerEntityRenderer(ModEntities.CRIMSON_LIGHTNING_BOLT.get(), CrimsonLightningBoltRenderer::new);
        event.registerEntityRenderer(ModEntities.BOAT.get(), context -> new ModBoatRenderer(context, false));
        event.registerEntityRenderer(ModEntities.CHEST_BOAT.get(), context -> new ModBoatRenderer(context, true));
        event.registerEntityRenderer(ModEntities.LOST_SOUL.get(), LostSoulRenderer::new);
        event.registerEntityRenderer(ModEntities.AUREAL_BOTTLE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.DARK_TRADER.get(), DarkTraderRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BlackHoleRenderer.BLACK_HOLE_LAYER, BlackHoleRenderer::createHoleLayer);
        event.registerLayerDefinition(BlackHoleRenderer.BLACK_HOLE_AURA_LAYER, BlackHoleRenderer::createAuraLayer);
        event.registerLayerDefinition(MagicCircleModel.OUTER_RING_LAYER, MagicCircleModel::createLayer);
        event.registerLayerDefinition(MagicCircleModel.INNER_RING_LAYER, MagicCircleModel::createLayer);
        event.registerLayerDefinition(MagicCircleModel.VALID_RITUAL_INDICATOR, MagicCircleModel::createLayer);

        //event.registerLayerDefinition(DracoAurumWingsModel.LAYER_LOCATION, DracoAurumWingsModel::createBodyLayer);
        //event.registerLayerDefinition(DracoAurumHeadModel.LAYER_LOCATION, DracoAurumHeadModel::createBodyLayer);

        event.registerLayerDefinition(LostSoulModel.LAYER_LOCATION, LostSoulModel::createBodyLayer);
        event.registerLayerDefinition(DarkTraderModel.LAYER_LOCATION, DarkTraderModel::createBodyLayer);
        event.registerLayerDefinition(QuantumLightDoorModel.LAYER_LOCATION, QuantumLightDoorModel::createLayer);

        event.registerLayerDefinition(OBSIDIAN_SKULL_LAYER, ObsidianSkullRenderer::createObsidianSkullLayer);
        event.registerLayerDefinition(DETAILED_OBSIDIAN_SKULL_LAYER, ObsidianSkullRenderer::createDetailedObsidianSkullLayer);
        event.registerLayerDefinition(AbstractForbiddenomiconModel.LAYER_LOCATION, AbstractForbiddenomiconModel::createBodyLayer);

        for (ModBoat.Type type : ModBoat.Type.values()) {
            event.registerLayerDefinition(new ModelLayerLocation(new ResourceLocation(ForbiddenArcanus.MOD_ID, type.getModelLocation()), "main"), BoatModel::createBodyModel);
            event.registerLayerDefinition(new ModelLayerLocation(new ResourceLocation(ForbiddenArcanus.MOD_ID, type.getChestModelLocation()), "main"), ChestBoatModel::createBodyModel);
        }
    }

    @SubscribeEvent
    public static void onCreateSkullModels(EntityRenderersEvent.CreateSkullModels event) {
        EntityModelSet modelSet = event.getEntityModelSet();

        event.registerSkullModel(ObsidianSkullType.DEFAULT, new SkullModel(modelSet.bakeLayer(OBSIDIAN_SKULL_LAYER)));
        event.registerSkullModel(ObsidianSkullType.CRACKED, new SkullModel(modelSet.bakeLayer(OBSIDIAN_SKULL_LAYER)));
        event.registerSkullModel(ObsidianSkullType.FRAGMENTED, new SkullModel(modelSet.bakeLayer(OBSIDIAN_SKULL_LAYER)));
        event.registerSkullModel(ObsidianSkullType.FADING, new SkullModel(modelSet.bakeLayer(OBSIDIAN_SKULL_LAYER)));
        event.registerSkullModel(ObsidianSkullType.AUREALIC, new SkullModel(modelSet.bakeLayer(DETAILED_OBSIDIAN_SKULL_LAYER)));
        event.registerSkullModel(ObsidianSkullType.ETERNAL, new SkullModel(modelSet.bakeLayer(DETAILED_OBSIDIAN_SKULL_LAYER)));
    }
}
