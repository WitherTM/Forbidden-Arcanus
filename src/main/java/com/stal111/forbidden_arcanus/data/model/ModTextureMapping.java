package com.stal111.forbidden_arcanus.data.model;

import com.stal111.forbidden_arcanus.ForbiddenArcanus;
import com.stal111.forbidden_arcanus.core.init.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * @author stal111
 * @since 28.10.2023
 */
public class ModTextureMapping {

    private static final String FORBIDDENOMICON = "forbiddenomicon";
    private static final String DESK = "desk";

    public static TextureMapping forbiddenomicon(Block block) {
        return new TextureMapping().put(TextureSlot.FRONT, getBlockTexture(block, FORBIDDENOMICON, "_front")).put(TextureSlot.BACK, getBlockTexture(block, FORBIDDENOMICON, "_back")).put(TextureSlot.INSIDE, getBlockTexture(block, FORBIDDENOMICON, "_inside")).put(TextureSlot.SIDE, getBlockTexture(block, FORBIDDENOMICON, "_side"));
    }

    public static TextureMapping desk(boolean research) {
        Block desk = ModBlocks.DESK.get();
        Block researchDesk = ModBlocks.RESEARCH_DESK.get();

        return new TextureMapping().put(TextureSlot.FRONT, getBlockTexture(desk, DESK, "_front")).put(TextureSlot.BACK, getBlockTexture(research ? researchDesk : desk, DESK, "_back")).put(TextureSlot.INSIDE, getBlockTexture(desk, DESK, "_inside")).put(TextureSlot.SIDE, getBlockTexture(research ? researchDesk : desk, DESK, "_side")).put(TextureSlot.TOP, getBlockTexture(desk, DESK, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(desk, DESK, "_bottom"));
    }

    public static ResourceLocation getBlockTexture(Block block, String folder) {
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
        return resourceLocation.withPrefix("block/" + folder + "/");
    }

    public static ResourceLocation getBlockTexture(Block block, String folder, String suffix) {
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
        return resourceLocation.withPath(s -> "block/" + folder + "/" + s + suffix);
    }

    public static ResourceLocation getBlockTexture(String folder, String texture) {
        return new ResourceLocation(ForbiddenArcanus.MOD_ID, "block/" + folder + "/" + texture);
    }
}
