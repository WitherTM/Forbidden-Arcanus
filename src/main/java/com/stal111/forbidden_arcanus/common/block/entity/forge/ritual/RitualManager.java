package com.stal111.forbidden_arcanus.common.block.entity.forge.ritual;

import com.stal111.forbidden_arcanus.common.block.entity.PedestalBlockEntity;
import com.stal111.forbidden_arcanus.common.block.entity.forge.HephaestusForgeBlockEntity;
import com.stal111.forbidden_arcanus.common.entity.CrimsonLightningBoltEntity;
import com.stal111.forbidden_arcanus.common.loader.RitualLoader;
import com.stal111.forbidden_arcanus.common.network.NetworkHandler;
import com.stal111.forbidden_arcanus.common.network.clientbound.UpdateForgeRitualPacket;
import com.stal111.forbidden_arcanus.common.network.clientbound.UpdatePedestalPacket;
import com.stal111.forbidden_arcanus.core.init.ModEntities;
import com.stal111.forbidden_arcanus.core.init.ModParticles;
import com.stal111.forbidden_arcanus.core.init.other.ModPOITypes;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.valhelsia.valhelsia_core.common.util.NeedsStoring;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Ritual Manager <br>
 * Forbidden Arcanus - com.stal111.forbidden_arcanus.common.tile.forge.ritual.RitualManager
 *
 * @author stal111
 * @since 2021-07-09
 */
public class RitualManager implements NeedsStoring {

    public static final float DEFAULT_RITUAL_TIME = 500.0F;

    private final HephaestusForgeBlockEntity blockEntity;

    private final List<BlockPos> cachedPedestals = new ArrayList<>();
    private Ritual activeRitual;
    private int counter;
    private int lightningCounter;

    public RitualManager(HephaestusForgeBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public HephaestusForgeBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public Ritual getActiveRitual() {
        return activeRitual;
    }

    public void setActiveRitual(Ritual ritual) {
        this.activeRitual = ritual;
    }

    public boolean isRitualActive() {
        return this.activeRitual != null;
    }

    public void tryStartRitual(ServerLevel level, EssencesStorage storage, BooleanConsumer started) {
        List<ItemStack> list = new ArrayList<>();

        this.forEachPedestal(level, PedestalBlockEntity::hasStack, pedestalBlockEntity -> list.add(pedestalBlockEntity.getStack()), true);

        for (Ritual ritual : RitualLoader.getRituals()) {
            if (storage.hasMoreThan(ritual.getEssences()) && ritual.checkIngredients(list, this.blockEntity.getStack(4))) {

                this.startRitual(storage, ritual);

                started.accept(true);

                return;
            }
        }

        started.accept(false);
    }

    public void startRitual(EssencesStorage storage, Ritual ritual) {
        this.setActiveRitual(ritual);

        storage.reduce(ritual.getEssences());
    }

    public void tick(ServerLevel level, BlockPos pos) {
        if (!this.isRitualActive()) {
            return;
        }

        RandomSource random = level.getRandom();
        float progress = RitualManager.getRitualProgress(this.counter);

        this.counter++;
        this.updateCachedPedestals(level);

        if (this.lightningCounter != 0) {
            this.lightningCounter++;

            if (this.lightningCounter == 300) {
                List<ItemStack> list = new ArrayList<>();

                this.forEachPedestal(level, PedestalBlockEntity::hasStack, pedestalBlockEntity -> list.add(pedestalBlockEntity.getStack()));

                if (!this.getActiveRitual().checkIngredients(list, this.blockEntity.getStack(4))) {
                    this.failRitual(level);

                    NetworkHandler.sendToTrackingChunk(level.getChunkAt(pos), new UpdateForgeRitualPacket(pos, this.activeRitual));
                    return;
                }

                this.lightningCounter = 0;
            }
        }

        this.forEachPedestal(level, PedestalBlockEntity::hasStack, pedestalBlockEntity -> {
            BlockPos pedestalPos = pedestalBlockEntity.getBlockPos();

            if (pedestalBlockEntity.getItemHeight() != 140) {
                int height = pedestalBlockEntity.getItemHeight() + 1;
                pedestalBlockEntity.setItemHeight(height);

                NetworkHandler.sendToTrackingChunk(level.getChunkAt(pedestalPos), new UpdatePedestalPacket(pedestalPos, pedestalBlockEntity.getStack(), height));
            }

            this.addItemParticles(level, pedestalPos, pedestalBlockEntity.getItemHeight(), pedestalBlockEntity.getStack());
        });

        if (progress == 0.5F && random.nextDouble() <= this.getFailureChance() * 2) {
            CrimsonLightningBoltEntity entity = new CrimsonLightningBoltEntity(ModEntities.CRIMSON_LIGHTNING_BOLT.get(), level);
            entity.setPos(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
            entity.setVisualOnly(true);

            level.addFreshEntity(entity);

            this.lightningCounter++;

            this.forEachPedestal(level, PedestalBlockEntity::hasStack, pedestalBlockEntity -> {
                if (random.nextBoolean()) {
                    ItemStack stack = pedestalBlockEntity.getStack().copy();
                    BlockPos pedestalPos = pedestalBlockEntity.getBlockPos();

                    level.addFreshEntity(new ItemEntity(level, pedestalPos.getX() + 0.5, pedestalPos.getY() + pedestalBlockEntity.getItemHeight() / 100.0F, pedestalPos.getZ() + 0.5, stack));
                    pedestalBlockEntity.clearStack(level);
                }
            });
        }

        if (progress == 1.0F) {
            if (random.nextDouble() > this.getFailureChance()) {
                this.finishRitual(level);
            } else {
                this.failRitual(level);
            }
        }

        NetworkHandler.sendToTrackingChunk(level.getChunkAt(pos), new UpdateForgeRitualPacket(pos, this.activeRitual));
    }

    public void finishRitual(ServerLevel level) {
        this.blockEntity.setStack(4, this.getActiveRitual().getResult());
        this.reset();

        this.forEachPedestal(level, PedestalBlockEntity::hasStack, pedestalBlockEntity -> {
            pedestalBlockEntity.clearStack(level);
        });
    }

    public void failRitual(ServerLevel level) {
        ItemStack stack = this.blockEntity.getStack(4);
        BlockPos pos = this.blockEntity.getBlockPos();

        this.reset();

        if (!stack.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack));

            this.blockEntity.setStack(4, ItemStack.EMPTY);
        }

        this.forEachPedestal(level, PedestalBlockEntity::hasStack, pedestalBlockEntity -> {
            pedestalBlockEntity.clearStack(level);
           // this.blockEntity.getEssenceManager().increaseCorruption(2);
        });

        level.sendParticles(ModParticles.HUGE_MAGIC_EXPLOSION.get(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0, 1.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F);
    }

    private void addItemParticles(ServerLevel level, BlockPos pedestalPos, int itemHeight, ItemStack stack) {
        BlockPos pos = this.blockEntity.getBlockPos();

        double posX = pedestalPos.getX() + 0.5D;
        double posY = pedestalPos.getY() + 0.1D + itemHeight / 100.0F;
        double posZ = pedestalPos.getZ() + 0.5D;
        double xSpeed = 0.1D * (pos.getX() - pedestalPos.getX());
        double ySpeed = 0.22D;
        double zSpeed = 0.1D * (pos.getZ() - pedestalPos.getZ());

        if (level.getRandom().nextDouble() < 0.6D) {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), posX, posY, posZ, 0, xSpeed, ySpeed, zSpeed, 0.9D);
        }
    }

    private void reset() {
        this.counter = 0;
        this.lightningCounter = 0;
        this.setActiveRitual(null);
    }

    public double getFailureChance() {
        //TODO
        return 0.0D;
        //return ((this.getBlockEntity().getEssenceManager().getCorruption() + 5) / (float) this.getBlockEntity().getForgeLevel().getMaxCorruption()) / 2;
    }

    /**
     * @return the progress of the currently active ritual. Between {@code 0.0F} and {@code 1.0F} if the ritual is finished.
     */
    public static float getRitualProgress(float counter) {
        return counter / DEFAULT_RITUAL_TIME;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (this.isRitualActive()) {
            tag.putString("ActiveRitual", this.getActiveRitual().getName().toString());
            tag.putInt("Counter", this.counter);

            if (this.lightningCounter != 0) {
                tag.putInt("LightningCounter", this.lightningCounter);
            }
        }

        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("ActiveRitual")) {
            this.setActiveRitual(RitualLoader.getRitual(new ResourceLocation(tag.getString("ActiveRitual"))));
            this.counter = tag.getInt("Counter");

            if (this.counter != 0) {
                this.blockEntity.getMagicCircle().setCounter(this.counter);
            }

            if (tag.contains("LightningCounter")) {
                this.lightningCounter = tag.getInt("LightningCounter");
            }
        }
    }

    public void updateCachedPedestals(ServerLevel level) {
        PoiManager manager = level.getPoiManager();

        this.cachedPedestals.clear();
        manager.getInRange(poiType -> poiType.get() == ModPOITypes.PEDESTAL.get(), this.blockEntity.getBlockPos(), 4, PoiManager.Occupancy.ANY).forEach(pointOfInterest -> this.cachedPedestals.add(pointOfInterest.getPos()));
    }

    public void forEachPedestal(ServerLevel level, Predicate<PedestalBlockEntity> predicate, Consumer<PedestalBlockEntity> consumer) {
        this.forEachPedestal(level, predicate, consumer, false);
    }

    public void forEachPedestal(ServerLevel level, Predicate<PedestalBlockEntity> predicate, Consumer<PedestalBlockEntity> consumer, boolean updatePedestals) {
        if (updatePedestals) {
            this.updateCachedPedestals(level);
        }
        this.cachedPedestals.stream().map(pos -> (PedestalBlockEntity) level.getBlockEntity(pos)).filter(predicate).forEach(consumer);
    }
}
