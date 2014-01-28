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

package ccm.craycrafting.recipes;

import ccm.craycrafting.CrayCrafting;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.LinkedList;
import java.util.List;

/**
 * Extend to add new recipe type to the system
 * This class registers itself, just make sure you initiate it somewhere.
 *
 * @param <T>
 * @author Dries007
 */
public abstract class BaseType<T extends IRecipe>
{
    final Class<T> type;

    final protected LinkedList<T> addedList   = new LinkedList<T>();
    final protected LinkedList<T> removedList = new LinkedList<T>();
    private         NBTTagList    nbtList     = new NBTTagList();

    boolean applied;

    /**
     * Just pass this the class your type wants. Stupid java...
     */
    @SuppressWarnings("unchecked")
    public BaseType(Class<T> type)
    {
        this.type = type;
        nbtList.setName(getTypeName());
        if (!IRecipe.class.isAssignableFrom(type)) throw new IllegalArgumentException("Type must be specified and extend IRecipe.");
        RecipeRegistry.register((BaseType<IRecipe>) this);
    }

    /**
     * Don't miss private fields...
     * Used on server only.
     */
    public abstract NBTTagCompound getNBTFromRecipe(T recipe, ItemStack newOutput) throws IllegalAccessException;

    /**
     * Don't forget to set special properties if required.
     * Used on both sides.
     */
    public abstract T getRecipeFromNBT(NBTTagCompound nbtRecipe);

    /**
     * Check everything EXCEPT the output ItemStack.
     * Used on both sides.
     *
     * @see ccm.craycrafting.util.Helper
     */
    public abstract boolean equalsExceptOutput(T recipe1, T recipe2) throws IllegalAccessException;

    /**
     * Must be unique!
     * Used on both sides.
     */
    public String getTypeName()
    {
        return type.getSimpleName();
    }

    /**
     * Replaces an instanceof check
     * Used on both sides.
     */
    public boolean accept(IRecipe recipe)
    {
        return type.isAssignableFrom(recipe.getClass());
    }

    /**
     * Used to read from disk and from the packet send to the server.
     * Make sure you don't apply twice!
     * <p/>
     * Used on both sides.
     */
    @SuppressWarnings("unchecked")
    public void loadRecipesFromNBT(NBTTagCompound root) throws Exception
    {
        NBTTagList list = root.getTagList(getTypeName());
        for (int i = 0; i < list.tagCount(); i++)
        {
            T newRecipe = getRecipeFromNBT((NBTTagCompound) list.tagAt(i));
            addedList.add(newRecipe);

            for (IRecipe oldRecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
            {
                if (type.isAssignableFrom(oldRecipe.getClass())) // instanceof basically
                {
                    if (equalsExceptOutput(newRecipe, (T) oldRecipe))
                    {
                        removedList.add((T) oldRecipe);
                        break;
                    }
                }
            }
        }
        apply();
    }

    /**
     * Used on both sides.
     */
    @SuppressWarnings("unchecked")
    public void apply()
    {
        if (!applied)
        {
            CrayCrafting.logger.info("APPLY " + getTypeName() + " Removed " + removedList.size() + "  & added " + addedList.size() + " recipes from " + FMLCommonHandler.instance().getEffectiveSide());
            applied = true;
            CraftingManager.getInstance().getRecipeList().removeAll(removedList);
            CraftingManager.getInstance().getRecipeList().addAll(addedList);
        }
    }

    /**
     * Used on client only.
     */
    @SuppressWarnings("unchecked")
    public void undo()
    {
        if (applied)
        {
            CrayCrafting.logger.info("UNDO " + getTypeName() + " Removed " + removedList.size() + " & added " + addedList.size() + " recipes from " + FMLCommonHandler.instance().getEffectiveSide());
            applied = false;
            CraftingManager.getInstance().getRecipeList().removeAll(addedList);
            CraftingManager.getInstance().getRecipeList().addAll(removedList);

            addedList.clear();
            removedList.clear();
        }
    }

    /**
     * Used on server only.
     */
    public NBTTagList getNBTList()
    {
        return nbtList;
    }

    /**
     * Used on server only.
     */
    public void applyRandomization(T recipe, ItemStack itemStack)
    {
        try
        {
            removedList.add(recipe);
            NBTTagCompound nbtRecipe = getNBTFromRecipe(recipe, itemStack);
            nbtList.appendTag(nbtRecipe);
            addedList.add(getRecipeFromNBT(nbtRecipe));
        }
        catch (Exception e)
        {
            CrayCrafting.logger.warning("Error in " + getTypeName() + " (" + recipe + "), adding back the original.");
            removedList.remove(recipe);
            e.printStackTrace();
        }
    }
}
