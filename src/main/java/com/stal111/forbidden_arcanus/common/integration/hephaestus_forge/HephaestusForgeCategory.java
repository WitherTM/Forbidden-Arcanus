package com.stal111.forbidden_arcanus.common.integration.hephaestus_forge;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.RitualInput;
import com.stal111.forbidden_arcanus.common.item.enhancer.EnhancerDefinition;
import com.stal111.forbidden_arcanus.core.init.ModBlocks;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author stal111
 * @since 2023-06-05
 */
public abstract class HephaestusForgeCategory implements IRecipeCategory<Ritual> {

    private static final List<IntIntPair> INPUT_POSITIONS = ImmutableList.of(
            IntIntPair.of(63, 13),
            IntIntPair.of(82, 16),
            IntIntPair.of(85, 35),
            IntIntPair.of(82, 54),
            IntIntPair.of(63, 57),
            IntIntPair.of(44, 54),
            IntIntPair.of(41, 35),
            IntIntPair.of(44, 16)
    );

    private static final IntIntPair FORGE_ITEM_POSITION = IntIntPair.of(63, 35);
    private static final IntIntPair ENHANCER_POSITION = IntIntPair.of(10, 12);
    private static final int ENHANCER_Y_OFFSET = 21;

    private final String name;

    private final IDrawable background;
    private final IDrawable icon;
    private final List<EssenceInfo> essences;

    public HephaestusForgeCategory(String name, IGuiHelper guiHelper, ResourceLocation texture, int essencesStartX, int essencesStartY) {
        this.name = name;
        this.background = guiHelper.createDrawable(texture, 0, 0, 148, 108);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.HEPHAESTUS_FORGE.get()));
        this.essences = EssenceInfo.create(guiHelper, essencesStartX, essencesStartY);
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.forbidden_arcanus.category." + name);
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull Ritual ritual, @Nonnull IFocusGroup focusGroup) {
        this.addInputs(builder, ritual.inputs(), ritual.mainIngredient());

        if (ritual.requirements() != null && this.displayEnhancers()) {
            this.addEnhancers(builder, ritual.requirements().enhancers());
        }

        this.buildRecipe(builder, ritual);
    }

    protected abstract void buildRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull Ritual ritual);

    protected boolean displayEnhancers() {
        return true;
    }

    private void addInputs(@Nonnull IRecipeLayoutBuilder builder, List<RitualInput> inputs, Ingredient mainIngredient) {
        builder.addSlot(RecipeIngredientRole.INPUT, FORGE_ITEM_POSITION.firstInt(), FORGE_ITEM_POSITION.secondInt())
                .addIngredients(mainIngredient);

        int index = 0;

        for (RitualInput input : inputs) {
            for (int j = 0; j < input.amount(); j++) {
                builder.addSlot(RecipeIngredientRole.INPUT, INPUT_POSITIONS.get(index).firstInt(), INPUT_POSITIONS.get(index).secondInt())
                        .addIngredients(input.ingredient());

                index++;
            }
        }
    }

    private void addEnhancers(@Nonnull IRecipeLayoutBuilder builder, List<Holder<EnhancerDefinition>> enhancers) {
        for (int i = 0; i < enhancers.size(); i++) {
            Holder<EnhancerDefinition> enhancer = enhancers.get(i);

            builder.addSlot(RecipeIngredientRole.CATALYST, ENHANCER_POSITION.firstInt(), ENHANCER_POSITION.secondInt() + i * ENHANCER_Y_OFFSET)
                    .addItemStack(enhancer.get().item().getDefaultInstance());
        }
    }

    @Override
    public void draw(@Nonnull Ritual recipe, @Nonnull IRecipeSlotsView slotsView, @Nonnull PoseStack poseStack, double mouseX, double mouseY) {
        this.essences.forEach(essenceInfo -> essenceInfo.drawable().draw(poseStack, essenceInfo.posX(), essenceInfo.posY()));
    }

    @Nonnull
    @Override
    public List<Component> getTooltipStrings(@Nonnull Ritual recipe, @Nonnull IRecipeSlotsView slotsView, double mouseX, double mouseY) {
        for (EssenceInfo essenceInfo : this.essences) {
            if (essenceInfo.shouldDisplayTooltip(mouseX, mouseY)) {
                return Collections.singletonList(essenceInfo.getTooltip(recipe.essences()));
            }
        }

        return Collections.emptyList();
    }
}
