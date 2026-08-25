package net.tadacko.tadackosdrinks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Simple GLM that adds one ItemStack of the configured item to the generated loot.
 * The JSON side should use a loot-table condition (forge:loot_table_id) so this modifier
 * only runs for tall_grass.
 */
public class AddSeedModifier extends LootModifier {
    // Codec - serializes/deserializes the "item" field from JSON
    public static final Codec<AddSeedModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst)
                    .and(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item))
                    .apply(inst, AddSeedModifier::new)
    );

    private final Item item;

    public AddSeedModifier(LootItemCondition[] conditions, Item item) {
        super(conditions);
        this.item = item;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // If the tool was shears, do nothing (vanilla behaviour)
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool != null && tool.getItem() == Items.SHEARS) return generatedLoot;

        // Add one of the configured item to the generated loot
        generatedLoot.add(new ItemStack(item));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}