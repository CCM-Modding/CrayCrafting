/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.craycrafting.util;

import ccm.craycrafting.CrayCrafting;
import com.google.common.collect.HashBiMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static ccm.craycrafting.util.Constants.*;

public class Helper
{
    private static final ArrayList<IRecipe> ADDED_RECIPES   = new ArrayList<IRecipe>();
    private static final ArrayList<IRecipe> REMOVED_RECIPES = new ArrayList<IRecipe>();

    public static void randomizeRecipes(File recipeFile)
    {
        NBTTagCompound root = new NBTTagCompound();
        ArrayList<ItemStack> outputs = new ArrayList<ItemStack>();
        ArrayList<ShapedRecipes> shapedRecipeses = new ArrayList<ShapedRecipes>();
        ArrayList<ShapelessRecipes> shapelessRecipeses = new ArrayList<ShapelessRecipes>();
        ArrayList<ShapedOreRecipe> shapedOreRecipes = new ArrayList<ShapedOreRecipe>();
        ArrayList<ShapelessOreRecipe> shapelessOreRecipes = new ArrayList<ShapelessOreRecipe>();

        /**
         * Loop over all recipes and add the ones we can/will randomise to a list.
         */
        for (IRecipe recipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
        {
            if (recipe instanceof ShapedRecipes)
            {
                ShapedRecipes shapedRecipes = (ShapedRecipes) recipe;
                shapedRecipeses.add(shapedRecipes);
                outputs.add(recipe.getRecipeOutput().copy());
            }
            else if (recipe instanceof ShapelessRecipes)
            {
                ShapelessRecipes shapelessRecipes = (ShapelessRecipes) recipe;
                shapelessRecipeses.add(shapelessRecipes);
                outputs.add(recipe.getRecipeOutput().copy());
            }
            else if (recipe instanceof ShapedOreRecipe)
            {
                ShapedOreRecipe shapedOreRecipe = (ShapedOreRecipe) recipe;
                shapedOreRecipes.add(shapedOreRecipe);
                outputs.add(recipe.getRecipeOutput());
            }
            else if (recipe instanceof ShapelessOreRecipe)
            {
                ShapelessOreRecipe shapelessOreRecipe = (ShapelessOreRecipe) recipe;
                shapelessOreRecipes.add(shapelessOreRecipe);
                outputs.add(recipe.getRecipeOutput());
            }
        }

        REMOVED_RECIPES.addAll(shapedRecipeses);
        REMOVED_RECIPES.addAll(shapelessRecipeses);
        REMOVED_RECIPES.addAll(shapedOreRecipes);
        REMOVED_RECIPES.addAll(shapelessOreRecipes);

        /**
         * Removing the recipes
         */
        CraftingManager.getInstance().getRecipeList().removeAll(REMOVED_RECIPES);

        /**
         * Randomize!
         */
        Collections.shuffle(outputs);
        Collections.shuffle(shapedRecipeses);
        Collections.shuffle(shapelessRecipeses);
        Collections.shuffle(shapedOreRecipes);
        Collections.shuffle(shapelessOreRecipes);

        // To keep track of the output stack we're at. Gets used by ALL subtypes.
        int outputIndex = 0;

        /**
         * Anything ShapedRecipes related
         */
        {
            NBTTagList shapedRecipesesNBTagList = new NBTTagList();
            for (ShapedRecipes shapedRecipes : shapedRecipeses)
            {
                try
                {
                    ItemStack output = outputs.get(outputIndex++);
                    /**
                     * Make an NBT representation of the recipe.
                     */
                    NBTTagCompound NBTRecipe = new NBTTagCompound();
                    NBTRecipe.setInteger(NBT_recipeWidth, shapedRecipes.recipeWidth);
                    NBTRecipe.setInteger(NBT_recipeHeight, shapedRecipes.recipeHeight);
                    NBTTagList NBTInput = new NBTTagList();
                    for (ItemStack is : shapedRecipes.recipeItems)
                    {
                        if (is == null) NBTInput.appendTag(new NBTTagCompound());
                        else NBTInput.appendTag(is.writeToNBT(new NBTTagCompound()));
                    }
                    NBTRecipe.setTag(NBT_input, NBTInput);
                    NBTRecipe.setCompoundTag(NBT_oldOutput, shapedRecipes.getRecipeOutput().writeToNBT(new NBTTagCompound()));
                    NBTRecipe.setCompoundTag(NBT_newOutput, output.writeToNBT(new NBTTagCompound()));
                    NBTRecipe.setBoolean(NBT_field_92101_f, ShapedRecipes_field_92101_f.getBoolean(shapedRecipes));

                    shapedRecipesesNBTagList.appendTag(NBTRecipe);

                    /**
                     * Make the new recipe
                     */
                    ShapedRecipes newRecipe = new ShapedRecipes(shapedRecipes.recipeWidth, shapedRecipes.recipeHeight, shapedRecipes.recipeItems, output);
                    if (ShapedRecipes_field_92101_f.getBoolean(shapedRecipes)) newRecipe.func_92100_c();

                    ADDED_RECIPES.add(newRecipe);
                }
                catch (Exception e)
                {
                    CrayCrafting.logger.warning("Error in ShapedRecipes (" + shapedRecipes + "), adding back the original.");
                    ADDED_RECIPES.addAll(shapedOreRecipes);
                    e.printStackTrace();
                }
            }
            root.setTag(NBT_shapedRecipes, shapedRecipesesNBTagList);
        }

        /**
         * Anything ShapelessRecipes related
         */
        {
            NBTTagList shapelessRecipesesNBTagList = new NBTTagList();
            for (ShapelessRecipes shapelessRecipes : shapelessRecipeses)
            {
                try
                {
                    ItemStack output = outputs.get(outputIndex++);
                    /**
                     * Make an NBT representation of the recipe.
                     */
                    NBTTagCompound NBTRecipe = new NBTTagCompound();
                    NBTTagList NBTInput = new NBTTagList();
                    for (Object is : shapelessRecipes.recipeItems)
                    {
                        if (is == null) NBTInput.appendTag(new NBTTagCompound());
                        else NBTInput.appendTag(((ItemStack) is).writeToNBT(new NBTTagCompound()));
                    }
                    NBTRecipe.setTag(NBT_input, NBTInput);
                    NBTRecipe.setCompoundTag(NBT_oldOutput, shapelessRecipes.getRecipeOutput().writeToNBT(new NBTTagCompound()));
                    NBTRecipe.setCompoundTag(NBT_newOutput, output.writeToNBT(new NBTTagCompound()));

                    shapelessRecipesesNBTagList.appendTag(NBTRecipe);

                    /**
                     * Make the new recipe
                     */
                    ADDED_RECIPES.add(new ShapelessRecipes(output, shapelessRecipes.recipeItems));
                }
                catch (Exception e)
                {
                    CrayCrafting.logger.warning("Error in ShapelessRecipes (" + shapelessRecipes + "), adding back the original.");
                    ADDED_RECIPES.addAll(shapedOreRecipes);
                    e.printStackTrace();
                }
            }
            root.setTag(NBT_shapelessRecipes, shapelessRecipesesNBTagList);
        }

        /**
         * Anything ShapedOreRecipe related
         */
        {
            NBTTagList shapedOreRecipesNBTagList = new NBTTagList();
            for (ShapedOreRecipe shapedOreRecipe : shapedOreRecipes)
            {
                try
                {
                    ItemStack output = outputs.get(outputIndex++);

                    NBTTagCompound NBTRecipe = new NBTTagCompound();
                    NBTTagList NBTInput = new NBTTagList();

                    ArrayList input = new ArrayList(); // Becomes entire recipe input

                    int width = ShapedOreRecipe_width.getInt(shapedOreRecipe);
                    int height = ShapedOreRecipe_height.getInt(shapedOreRecipe);


                    /**
                     * Build a map to convert the object array into recipe format.
                     */
                    HashBiMap<Character, Object> map = HashBiMap.create();
                    HashMap<ArrayList, Object> arrayListMap = new HashMap<ArrayList, Object>(); // Lookup map for oredict entries.
                    for (Object o : shapedOreRecipe.getInput())
                    {
                        if (o == null) continue;
                        if (map.containsValue(o)) continue;
                        if (o instanceof ArrayList)
                        {
                            for (String name : OreDictionary.getOreNames())
                            {
                                if (OreDictionary.getOres(name).equals(o))
                                {
                                    if (map.containsValue(name)) break;
                                    map.put(DUMMY_CHARS.charAt(map.size()), name);
                                    arrayListMap.put((ArrayList) o, name);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            map.put(DUMMY_CHARS.charAt(map.size()), o);
                        }
                    }

                    /**
                     * Make the recipe strings
                     * aka: "aa ", "aa ", "aa "
                     */
                    char[][] chars = new char[height][width];
                    for (int h = 0; h < height; h++)
                    {
                        for (int w = 0; w < width; w++)
                        {
                            int i = h * width + w;
                            if (shapedOreRecipe.getInput()[i] == null) chars[h][w] = ' ';
                            else if (shapedOreRecipe.getInput()[i] instanceof ArrayList) chars[h][w] = map.inverse().get(arrayListMap.get(shapedOreRecipe.getInput()[i]));
                            else chars[h][w] = map.inverse().get(shapedOreRecipe.getInput()[i]);
                        }
                        String line = new String(chars[h]);
                        input.add(line);
                        NBTInput.appendTag(new NBTTagString(null, line));
                    }
                    NBTRecipe.setTag(NBT_input, NBTInput);

                    /**
                     * Add the char to itemstack thing
                     * aka: 'a' = "plank"
                     */
                    NBTTagCompound NBTMap = new NBTTagCompound();
                    for (Map.Entry<Character, Object> entry : map.entrySet())
                    {
                        input.add(entry.getKey());
                        input.add(entry.getValue());

                        if (entry.getValue() instanceof String) NBTMap.setString(entry.getKey().toString(), entry.getValue().toString());
                        else if (entry.getValue() instanceof ItemStack) NBTMap.setCompoundTag(entry.getKey().toString(), ((ItemStack) entry.getValue()).writeToNBT(new NBTTagCompound()));
                        else
                        {
                            CrayCrafting.logger.severe("NBT RECIPE ERROR: " + entry.getValue() + " IS NOT STRING OR ITEMSTACK ???");
                        }
                    }
                    NBTRecipe.setCompoundTag(NBT_map, NBTMap);

                    NBTRecipe.setCompoundTag(NBT_oldOutput, shapedOreRecipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));
                    NBTRecipe.setCompoundTag(NBT_newOutput, output.writeToNBT(new NBTTagCompound()));
                    NBTRecipe.setBoolean(NBT_mirror, ShapedOreRecipe_mirror.getBoolean(shapedOreRecipe));

                    shapedOreRecipesNBTagList.appendTag(NBTRecipe);
                    /**
                     * Make the new recipe
                     */
                    ADDED_RECIPES.add(new ShapedOreRecipe(output, input.toArray()).setMirrored(ShapedOreRecipe_mirror.getBoolean(shapedOreRecipe)));
                }
                catch (Exception e)
                {
                    CrayCrafting.logger.warning("Error in ShapedOreRecipe (" + shapedOreRecipe + "), adding back the original.");
                    ADDED_RECIPES.addAll(shapedOreRecipes);
                    e.printStackTrace();
                }
            }
            root.setTag(NBT_shapedOreRecipes, shapedOreRecipesNBTagList);
        }

        /**
         * Anything ShapelessOreRecipe related
         */
        {
            NBTTagList shapelessOreRecipesNBTagList = new NBTTagList();
            for (ShapelessOreRecipe shapelessOreRecipe : shapelessOreRecipes)
            {
                try
                {
                    ItemStack output = outputs.get(outputIndex++);

                    NBTTagCompound NBTRecipe = new NBTTagCompound();
                    NBTTagList NBTInput = new NBTTagList();

                    ArrayList input = new ArrayList();

                    for (Object o : shapelessOreRecipe.getInput())
                    {
                        if (o instanceof ArrayList)
                        {
                            for (String name : OreDictionary.getOreNames())
                            {
                                if (OreDictionary.getOres(name).equals(o))
                                {
                                    NBTTagCompound tag = new NBTTagCompound();
                                    tag.setString(NBT_oredictname, name);
                                    NBTInput.appendTag(tag);
                                    input.add(name);
                                    break;
                                }
                            }
                        }
                        else if (o instanceof ItemStack)
                        {
                            NBTInput.appendTag(((ItemStack) o).writeToNBT(new NBTTagCompound()));
                            input.add(o);
                        }
                        else
                        {
                            CrayCrafting.logger.severe("NBT RECIPE ERROR: " + o + " IS NOT STRING OR ITEMSTACK ???");
                        }
                    }
                    NBTRecipe.setTag(NBT_input, NBTInput);

                    NBTRecipe.setCompoundTag(NBT_oldOutput, shapelessOreRecipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));
                    NBTRecipe.setCompoundTag(NBT_newOutput, output.writeToNBT(new NBTTagCompound()));

                    shapelessOreRecipesNBTagList.appendTag(NBTRecipe);
                    /**
                     * Make the new recipe
                     */
                    ADDED_RECIPES.add(new ShapelessOreRecipe(output, input.toArray()));
                }
                catch (Exception e)
                {
                    CrayCrafting.logger.warning("Error in ShapelessOreRecipe (" + shapelessOreRecipe + "), adding back the original.");
                    ADDED_RECIPES.addAll(shapedOreRecipes);
                    e.printStackTrace();
                }
            }
            root.setTag(NBT_shapelessOreRecipes, shapelessOreRecipesNBTagList);
        }

        /**
         * Adding the new recipes and saving the list.
         */
        CraftingManager.getInstance().getRecipeList().addAll(ADDED_RECIPES);
        try
        {
            CompressedStreamTools.write(root, recipeFile);
        }
        catch (IOException e)
        {
            CrayCrafting.logger.severe("Fuck me. Something went wrong when saving the recipe file.");
            e.printStackTrace();
        }
    }

    public static void undoChanges()
    {
        CraftingManager.getInstance().getRecipeList().removeAll(ADDED_RECIPES);
        CraftingManager.getInstance().getRecipeList().addAll(REMOVED_RECIPES);
    }
}
