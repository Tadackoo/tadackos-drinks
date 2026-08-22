package net.tadacko.tadackosdrinks.recipe;

import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TadackosDrinks.MOD_ID);

    /** Shapeless recipe that consumes all ingredients (no container-item leftovers). */
    public static final RegistryObject<RecipeSerializer<ShapelessBucketConsumingRecipe>>
            SHAPELESS_BUCKET_CONSUMING = RECIPE_SERIALIZERS.register(
            "shapeless_bucket_consuming",
            ShapelessBucketConsumingRecipe.Serializer::new);
}