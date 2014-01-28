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
import net.minecraft.nbt.*;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.*;
import java.util.*;

import static ccm.craycrafting.util.Constants.*;

public class Helper
{
    private static final LinkedList<IRecipe> ADDED_RECIPES   = new LinkedList<IRecipe>();
    private static final LinkedList<IRecipe> REMOVED_RECIPES = new LinkedList<IRecipe>();

    public static NBTTagCompound getNBTFromRecipe(ShapedRecipes recipe, ItemStack newOutput) throws IllegalAccessException
    {
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        nbtRecipe.setInteger(NBT_recipeWidth, recipe.recipeWidth);
        nbtRecipe.setInteger(NBT_recipeHeight, recipe.recipeHeight);
        NBTTagList NBTInput = new NBTTagList();
        for (ItemStack is : recipe.recipeItems)
        {
            if (is == null) NBTInput.appendTag(new NBTTagCompound());
            else NBTInput.appendTag(is.writeToNBT(new NBTTagCompound()));
        }
        nbtRecipe.setTag(NBT_input, NBTInput);
        nbtRecipe.setCompoundTag(NBT_oldOutput, recipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));
        if (newOutput != null) nbtRecipe.setCompoundTag(NBT_newOutput, newOutput.writeToNBT(new NBTTagCompound()));
        nbtRecipe.setBoolean(NBT_field_92101_f, ShapedRecipes_field_92101_f.getBoolean(recipe));

        return nbtRecipe;
    }

    public static ShapedRecipes getShapedRecipeFromNBT(NBTTagCompound nbtRecipe, boolean newOutput)
    {
        int width = nbtRecipe.getInteger(NBT_recipeWidth);
        int height = nbtRecipe.getInteger(NBT_recipeHeight);
        NBTTagList list = nbtRecipe.getTagList(NBT_input);

        ItemStack[] input = new ItemStack[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) input[i] = ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i));

        ItemStack output = ItemStack.loadItemStackFromNBT(newOutput ? nbtRecipe.getCompoundTag(NBT_newOutput) : nbtRecipe.getCompoundTag(NBT_oldOutput));

        ShapedRecipes recipes = new ShapedRecipes(width, height, input, output);

        if (nbtRecipe.getBoolean(NBT_field_92101_f)) recipes.func_92100_c();

        return recipes;
    }

    public static NBTTagCompound getNBTFromRecipe(ShapelessRecipes recipe, ItemStack newOutput)
    {
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        NBTTagList NBTInput = new NBTTagList();
        for (Object is : recipe.recipeItems)
        {
            if (is == null) NBTInput.appendTag(new NBTTagCompound());
            else NBTInput.appendTag(((ItemStack) is).writeToNBT(new NBTTagCompound()));
        }
        nbtRecipe.setTag(NBT_input, NBTInput);
        nbtRecipe.setCompoundTag(NBT_oldOutput, recipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));
        if (newOutput != null) nbtRecipe.setCompoundTag(NBT_newOutput, newOutput.writeToNBT(new NBTTagCompound()));

        return nbtRecipe;
    }

    public static ShapelessRecipes getShapelessRecipeFromNBT(NBTTagCompound nbtRecipe, boolean newOutput)
    {
        ItemStack output = ItemStack.loadItemStackFromNBT(newOutput ? nbtRecipe.getCompoundTag(NBT_newOutput) : nbtRecipe.getCompoundTag(NBT_oldOutput));
        NBTTagList list = nbtRecipe.getTagList(NBT_input);

        LinkedList<ItemStack> input = new LinkedList<ItemStack>();
        for (int i = 0; i < list.tagCount(); i++) input.add(ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i)));

        return new ShapelessRecipes(output, input);
    }

    public static NBTTagCompound getNBTFromRecipe(ShapedOreRecipe recipe, ItemStack newOutput) throws IllegalAccessException
    {
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        NBTTagList NBTInput = new NBTTagList();

        int width = ShapedOreRecipe_width.getInt(recipe);
        int height = ShapedOreRecipe_height.getInt(recipe);

        /**
         * Build a map to convert the object array into recipe format.
         */
        HashBiMap<Character, Object> map = HashBiMap.create();
        HashMap<ArrayList, Object> arrayListMap = new HashMap<ArrayList, Object>(); // Lookup map for oredict entries.
        for (Object o : recipe.getInput())
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
                if (recipe.getInput()[i] == null) chars[h][w] = ' ';
                else if (recipe.getInput()[i] instanceof ArrayList) chars[h][w] = map.inverse().get(arrayListMap.get(recipe.getInput()[i]));
                else chars[h][w] = map.inverse().get(recipe.getInput()[i]);
            }
            String line = new String(chars[h]);
            NBTInput.appendTag(new NBTTagString(null, line));
        }
        nbtRecipe.setTag(NBT_input, NBTInput);

        /**
         * Add the char to itemstack thing
         * aka: 'a' = "plank"
         */
        NBTTagCompound nbtMap = new NBTTagCompound();
        for (Map.Entry<Character, Object> entry : map.entrySet())
        {
            if (entry.getValue() instanceof String) nbtMap.setString(entry.getKey().toString(), entry.getValue().toString());
            else if (entry.getValue() instanceof ItemStack) nbtMap.setCompoundTag(entry.getKey().toString(), ((ItemStack) entry.getValue()).writeToNBT(new NBTTagCompound()));
            else
            {
                CrayCrafting.logger.severe("NBT RECIPE ERROR: " + entry.getValue() + " IS NOT STRING OR ITEMSTACK ???");
            }
        }
        nbtRecipe.setCompoundTag(NBT_map, nbtMap);

        nbtRecipe.setCompoundTag(NBT_oldOutput, recipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));
        if (newOutput != null) nbtRecipe.setCompoundTag(NBT_newOutput, newOutput.writeToNBT(new NBTTagCompound()));
        nbtRecipe.setBoolean(NBT_mirror, ShapedOreRecipe_mirror.getBoolean(recipe));

        return nbtRecipe;
    }

    public static ShapedOreRecipe getShapedOreRecipeFromNBT(NBTTagCompound nbtRecipe, boolean newOutput)
    {
        LinkedList<Object> input = new LinkedList<Object>(); // Becomes entire recipe input
        ItemStack output = ItemStack.loadItemStackFromNBT(newOutput ? nbtRecipe.getCompoundTag(NBT_newOutput) : nbtRecipe.getCompoundTag(NBT_oldOutput));

        NBTTagList inputs = nbtRecipe.getTagList(NBT_input);
        for (int i = 0; i < inputs.tagCount(); i++) input.add(((NBTTagString)inputs.tagAt(i)).data);
        NBTTagCompound map = nbtRecipe.getCompoundTag(NBT_map);
        for (NBTBase entry : (Collection<NBTBase>)map.getTags())
        {
            input.add(entry.getName().charAt(0));
            if (entry instanceof NBTTagString) input.add(((NBTTagString) entry).data);
            else input.add(ItemStack.loadItemStackFromNBT((NBTTagCompound) entry));
        }

        return new ShapedOreRecipe(output, input.toArray()).setMirrored(nbtRecipe.getBoolean(NBT_mirror));
    }

    public static NBTTagCompound getNBTFromRecipe(ShapelessOreRecipe recipe, ItemStack newOutput)
    {
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        NBTTagList nbtInput = new NBTTagList();

        for (Object o : recipe.getInput())
        {
            if (o instanceof ArrayList)
            {
                for (String name : OreDictionary.getOreNames())
                {
                    if (OreDictionary.getOres(name).equals(o))
                    {
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setString(NBT_oredictname, name);
                        nbtInput.appendTag(tag);
                        break;
                    }
                }
            }
            else if (o instanceof ItemStack)
            {
                nbtInput.appendTag(((ItemStack) o).writeToNBT(new NBTTagCompound()));
            }
            else
            {
                CrayCrafting.logger.severe("NBT RECIPE ERROR: " + o + " IS NOT STRING OR ITEMSTACK ???");
            }
        }
        nbtRecipe.setTag(NBT_input, nbtInput);

        nbtRecipe.setCompoundTag(NBT_oldOutput, recipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));
        if (newOutput != null) nbtRecipe.setCompoundTag(NBT_newOutput, newOutput.writeToNBT(new NBTTagCompound()));

        return nbtRecipe;
    }

    public static ShapelessOreRecipe getShapelessOreRecipeFromNBT(NBTTagCompound nbtRecipe, boolean newOutput)
    {
        LinkedList<Object> input = new LinkedList<Object>();
        ItemStack output = ItemStack.loadItemStackFromNBT(newOutput ? nbtRecipe.getCompoundTag(NBT_newOutput) : nbtRecipe.getCompoundTag(NBT_oldOutput));

        NBTTagList inputs = nbtRecipe.getTagList(NBT_input);
        for (int i = 0; i < inputs.tagCount(); i++)
        {
            NBTTagCompound nbtInput = (NBTTagCompound) inputs.tagAt(i);
            if (nbtInput.hasKey(NBT_oredictname)) input.add(nbtInput.getString(NBT_oredictname));
            else input.add(ItemStack.loadItemStackFromNBT(nbtInput));
        }

        return new ShapelessOreRecipe(output, input.toArray());
    }

    public static void randomizeRecipes(File recipeFile)
    {
        NBTTagCompound root = new NBTTagCompound();
        ArrayList<ItemStack> outputs = new ArrayList<ItemStack>(CraftingManager.getInstance().getRecipeList().size());
        LinkedList<ShapedRecipes> shapedRecipeses = new LinkedList<ShapedRecipes>();
        LinkedList<ShapelessRecipes> shapelessRecipeses = new LinkedList<ShapelessRecipes>();
        LinkedList<ShapedOreRecipe> shapedOreRecipes = new LinkedList<ShapedOreRecipe>();
        LinkedList<ShapelessOreRecipe> shapelessOreRecipes = new LinkedList<ShapelessOreRecipe>();

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

        /**
         * Randomize!
         */
        Collections.shuffle(outputs);

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
                    REMOVED_RECIPES.add(shapedRecipes);
                    NBTTagCompound recipe = getNBTFromRecipe(shapedRecipes, outputs.get(outputIndex++));
                    shapedRecipesesNBTagList.appendTag(recipe);
                    ADDED_RECIPES.add(getShapedRecipeFromNBT(recipe, true));
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
                    REMOVED_RECIPES.add(shapelessRecipes);
                    NBTTagCompound recipe = getNBTFromRecipe(shapelessRecipes, outputs.get(outputIndex++));
                    shapelessRecipesesNBTagList.appendTag(recipe);
                    ADDED_RECIPES.add(getShapelessRecipeFromNBT(recipe, true));
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
                    REMOVED_RECIPES.add(shapedOreRecipe);
                    NBTTagCompound recipe = getNBTFromRecipe(shapedOreRecipe, outputs.get(outputIndex++));
                    shapedOreRecipesNBTagList.appendTag(recipe);
                    ADDED_RECIPES.add(getShapedOreRecipeFromNBT(recipe, true));
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
                    REMOVED_RECIPES.add(shapelessOreRecipe);
                    NBTTagCompound recipe = getNBTFromRecipe(shapelessOreRecipe, outputs.get(outputIndex++));
                    shapelessOreRecipesNBTagList.appendTag(recipe);
                    ADDED_RECIPES.add(getShapelessOreRecipeFromNBT(recipe, true));
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
         * Swaps the new recipes and save the list.
         */
        CraftingManager.getInstance().getRecipeList().removeAll(REMOVED_RECIPES);
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

        ADDED_RECIPES.clear();
        REMOVED_RECIPES.clear();
    }

    public static void loadRecipesFromNBT(NBTTagCompound root) throws IllegalAccessException
    {
        if (root.hasKey(NBT_shapedRecipes))
        {
            NBTTagList shapedRecipesesNBTagList = root.getTagList(NBT_shapedRecipes);
            for (int i = 0; i < shapedRecipesesNBTagList.tagCount(); i++)
            {
                NBTTagCompound recipe = (NBTTagCompound) shapedRecipesesNBTagList.tagAt(i);
                NBTTagCompound recipe2 = (NBTTagCompound) recipe.copy();
                recipe2.removeTag(NBT_newOutput);

                for (IRecipe oldRecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
                {
                    if (oldRecipe instanceof ShapedRecipes)
                    {
                        if (recipe2.equals(getNBTFromRecipe((ShapedRecipes) oldRecipe, null)))
                        {
                            REMOVED_RECIPES.add(oldRecipe);
                            break;
                        }
                    }
                }

                ADDED_RECIPES.add(getShapedRecipeFromNBT(recipe, true));
            }
        }

        if (root.hasKey(NBT_shapelessRecipes))
        {
            NBTTagList shapelessRecipesesNBTagList = root.getTagList(NBT_shapelessRecipes);
            for (int i = 0; i < shapelessRecipesesNBTagList.tagCount(); i++)
            {
                NBTTagCompound recipe = (NBTTagCompound) shapelessRecipesesNBTagList.tagAt(i);
                NBTTagCompound recipe2 = (NBTTagCompound) recipe.copy();
                recipe2.removeTag(NBT_newOutput);

                for (IRecipe oldRecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
                {
                    if (oldRecipe instanceof ShapelessRecipes)
                    {
                        if (recipe2.equals(getNBTFromRecipe((ShapelessRecipes) oldRecipe, null)))
                        {
                            REMOVED_RECIPES.add(oldRecipe);
                            break;
                        }
                    }
                }

                ADDED_RECIPES.add(getShapelessRecipeFromNBT(recipe, true));
            }
        }

        if (root.hasKey(NBT_shapedOreRecipes))
        {
            NBTTagList shapedOreRecipesNBTagList = root.getTagList(NBT_shapedOreRecipes);
            for (int i = 0; i < shapedOreRecipesNBTagList.tagCount(); i++)
            {
                NBTTagCompound recipe = (NBTTagCompound) shapedOreRecipesNBTagList.tagAt(i);
                NBTTagCompound recipe2 = (NBTTagCompound) recipe.copy();
                recipe2.removeTag(NBT_newOutput);

                for (IRecipe oldRecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
                {
                    if (oldRecipe instanceof ShapedOreRecipe)
                    {
                        if (recipe2.equals(getNBTFromRecipe((ShapedOreRecipe) oldRecipe, null)))
                        {
                            REMOVED_RECIPES.add(oldRecipe);
                            break;
                        }
                    }
                }

                ADDED_RECIPES.add(getShapedOreRecipeFromNBT(recipe, true));
            }
        }

        if (root.hasKey(NBT_shapelessOreRecipes))
        {
            NBTTagList shapelessOreRecipesNBTagList = root.getTagList(NBT_shapelessOreRecipes);
            for (int i = 0; i < shapelessOreRecipesNBTagList.tagCount(); i++)
            {
                NBTTagCompound recipe = (NBTTagCompound) shapelessOreRecipesNBTagList.tagAt(i);
                NBTTagCompound recipe2 = (NBTTagCompound) recipe.copy();
                recipe2.removeTag(NBT_newOutput);

                for (IRecipe oldRecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
                {
                    if (oldRecipe instanceof ShapedOreRecipe)
                    {
                        if (recipe2.equals(getNBTFromRecipe((ShapedOreRecipe) oldRecipe, null)))
                        {
                            REMOVED_RECIPES.add(oldRecipe);
                            break;
                        }
                    }
                }

                ADDED_RECIPES.add(getShapelessOreRecipeFromNBT(recipe, true));
            }
        }

        CraftingManager.getInstance().getRecipeList().removeAll(REMOVED_RECIPES);
        CraftingManager.getInstance().getRecipeList().addAll(ADDED_RECIPES);
    }

    public static byte[] nbtToByteArray(NBTTagCompound nbtTagCompound)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            writeNBTTagCompound(nbtTagCompound, stream);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return streambyte.toByteArray();
    }

    public static NBTTagCompound byteArrayToNBT(byte[] data)
    {
        NBTTagCompound nbtTagCompound;
        ByteArrayInputStream streambyte = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(streambyte);

        try
        {
            nbtTagCompound = readNBTTagCompound(stream);
        }
        catch (IOException e)
        {
            nbtTagCompound = new NBTTagCompound();
            e.printStackTrace();
        }

        return nbtTagCompound;
    }

    public static void writeNBTTagCompound(NBTTagCompound nbtTagCompound, DataOutput dataOutput) throws IOException
    {
        if (nbtTagCompound == null)
        {
            dataOutput.writeShort(-1);
        }
        else
        {
            byte[] abyte = CompressedStreamTools.compress(nbtTagCompound);
            dataOutput.writeShort((short) abyte.length);
            dataOutput.write(abyte);
        }
    }

    public static NBTTagCompound readNBTTagCompound(DataInput dataInput) throws IOException
    {
        short short1 = dataInput.readShort();

        if (short1 < 0)
        {
            return null;
        }
        else
        {
            byte[] abyte = new byte[short1];
            dataInput.readFully(abyte);
            return CompressedStreamTools.decompress(abyte);
        }
    }
}
