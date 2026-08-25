package net.tadacko.tadackosdrinks.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.crafting.*;

/** A shapeless recipe that fully consumes filled buckets (which would normally leave behind an empty bucket). */
public class ShapelessBucketConsumingRecipe extends ShapelessRecipe {

    public ShapelessBucketConsumingRecipe(ResourceLocation id, String group,
                                          CraftingBookCategory category, ItemStack result,
                                          NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.getItem() instanceof BucketItem || stack.getItem() instanceof MilkBucketItem) continue;

            if (stack.hasCraftingRemainingItem()) {
                // Non-bucket container item (e.g. glass bottle): keep vanilla behavior
                remaining.set(i, stack.getCraftingRemainingItem());
            }
        }

        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SHAPELESS_BUCKET_CONSUMING.get();
    }

    // Serializer — mirrors ShapelessRecipe.Serializer but produces our class
    public static class Serializer implements RecipeSerializer<ShapelessBucketConsumingRecipe> {
        @Override
        public ShapelessBucketConsumingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", null),
                    CraftingBookCategory.MISC);

            NonNullList<Ingredient> ingredients = readIngredients(
                    GsonHelper.getAsJsonArray(json, "ingredients"));

            ItemStack result = ShapedRecipe.itemStackFromJson(
                    GsonHelper.getAsJsonObject(json, "result"));

            return new ShapelessBucketConsumingRecipe(id, group, category, result, ingredients);
        }

        @Override
        public ShapelessBucketConsumingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);

            int count = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buf));

            ItemStack result = buf.readItem();

            return new ShapelessBucketConsumingRecipe(id, group, category, result, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShapelessBucketConsumingRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeEnum(recipe.category());

            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buf);
            }

            buf.writeItem(recipe.getResultItem(null)); // null registryAccess is fine for simple items
        }

        // Parse the "ingredients" JSON array into a NonNullList
        private NonNullList<Ingredient> readIngredients(JsonArray array) {
            NonNullList<Ingredient> list = NonNullList.create();
            for (JsonElement element : array) {
                list.add(Ingredient.fromJson(element));
            }
            return list;
        }
    }
}