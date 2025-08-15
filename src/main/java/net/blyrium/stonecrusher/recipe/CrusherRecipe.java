package net.blyrium.stonecrusher.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CrusherRecipe extends AbstractCookingRecipe {
    public CrusherRecipe(Identifier id, String group, CookingRecipeCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(Type.INSTANCE, id, group, category, input, output, experience, cookTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Type implements RecipeType<CrusherRecipe> {
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer implements RecipeSerializer<CrusherRecipe> {
        public static final RecipeSerializer<CrusherRecipe> INSTANCE = new Serializer();

        @Override
        public CrusherRecipe read(Identifier id, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");

            // Parse category - handle it manually since CODEC.byName doesn't exist in 1.20.1
            CookingRecipeCategory category = CookingRecipeCategory.MISC; // default
            if (json.has("category")) {
                String categoryName = JsonHelper.getString(json, "category");
                try {
                    category = CookingRecipeCategory.valueOf(categoryName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // If invalid category name, use default MISC
                    category = CookingRecipeCategory.MISC;
                }
            }

            // Parse ingredient
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));

            // Parse result - support both old and new formats
            ItemStack result;
            JsonElement resultElement = json.get("result");
            if (resultElement.isJsonPrimitive()) {
                // Old/Vanilla format: "result": "minecraft:sand" (single item)
                Item item = Registries.ITEM.get(new Identifier(resultElement.getAsString()));
                result = new ItemStack(item);
            } else {
                // New format: "result": {"item": "minecraft:sand", "count": 7} (several items)
                JsonObject resultObj = resultElement.getAsJsonObject();
                Item item = Registries.ITEM.get(new Identifier(resultObj.get("item").getAsString()));
                int count = JsonHelper.getInt(resultObj, "count", 1);
                result = new ItemStack(item, count);
            }

            // No experience granted by default
            float experience = JsonHelper.getFloat(json, "experience", 0.0f);
            int cookingTime = JsonHelper.getInt(json, "cookingtime", 50);

            return new CrusherRecipe(id, group, category, ingredient, result, experience, cookingTime);
        }

        @Override
        public CrusherRecipe read(Identifier id, PacketByteBuf buf) {
            String group = buf.readString();
            CookingRecipeCategory category = buf.readEnumConstant(CookingRecipeCategory.class);
            Ingredient ingredient = Ingredient.fromPacket(buf);
            ItemStack result = buf.readItemStack();
            float experience = buf.readFloat();
            int cookingTime = buf.readVarInt();

            return new CrusherRecipe(id, group, category, ingredient, result, experience, cookingTime);
        }

        @Override
        public void write(PacketByteBuf buf, CrusherRecipe recipe) {
            buf.writeString(recipe.getGroup());
            buf.writeEnumConstant(recipe.getCategory());
            recipe.getIngredients().get(0).write(buf);
            buf.writeItemStack(recipe.getOutput(null));
            buf.writeFloat(recipe.getExperience());
            buf.writeVarInt(recipe.getCookTime());
        }
    }
}