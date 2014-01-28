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
import ccm.craycrafting.util.Helper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ccm.craycrafting.util.Constants.*;

/**
 * Replaces a lot of the old helper crap
 *
 * @author Dries007
 */
public class RecipeRegistry
{
    private static final ArrayList<BaseType<IRecipe>> typeList = new ArrayList<BaseType<IRecipe>>();

    /**
     * Used to read from disk and from the packet send to the server.
     * Used on both sides.
     */
    public static void loadRecipesFromNBT(NBTTagCompound root) throws Exception
    {
        for (BaseType<IRecipe> baseType : typeList)
        {
            if (root.hasKey(baseType.getTypeName())) baseType.loadRecipesFromNBT(root);
        }
    }

    /**
     * Used on client only.
     */
    public static void undo()
    {
        for (BaseType<IRecipe> baseType : typeList)
        {
            baseType.undo();
        }
    }

    /**
     * Used on both sides.
     */
    protected static void register(BaseType<IRecipe> baseType)
    {
        typeList.add(baseType);
    }

    /**
     * Used on server only.
     */
    public static void sendPacketTo(Player player)
    {
        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_STATUS, STATUS_RESET.getBytes()), player);
        for (BaseType<IRecipe> baseType : typeList)
        {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setTag(baseType.getTypeName(), baseType.getNBTList());
            CrayCrafting.logger.info("Sending " + baseType.getTypeName());
            PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_DATA, Helper.nbtToByteArray(nbtTagCompound)), player);
        }
    }

    /**
     * Used on both sides.
     */
    public static void randomizeRecipes(File recipeFile)
    {
        CrayCrafting.logger.severe("randomizeRecipes " + FMLCommonHandler.instance().getSide() + " " + FMLCommonHandler.instance().getEffectiveSide());
        NBTTagCompound root = new NBTTagCompound();
        ArrayList<ItemStack> outputs = new ArrayList<ItemStack>(CraftingManager.getInstance().getRecipeList().size());
        ArrayList<IRecipe> acceptedRecipes = new ArrayList<IRecipe>();

        for (IRecipe recipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
        {
            for (BaseType<IRecipe> baseType : typeList)
            {
                if (baseType.accept(recipe))
                {
                    outputs.add(recipe.getRecipeOutput());
                    acceptedRecipes.add(recipe);

                    break;
                }
            }
        }

        Collections.shuffle(outputs);
        int outputIndex = 0;

        for (IRecipe recipe : acceptedRecipes)
        {
            for (BaseType<IRecipe> baseType : typeList)
            {
                if (baseType.accept(recipe))
                {
                    baseType.applyRandomization(recipe, outputs.get(outputIndex++));
                    break;
                }
            }
        }

        if (outputIndex != outputs.size())
        {
            CrayCrafting.logger.severe("*********************************************************************************************");
            CrayCrafting.logger.severe("** We have items left over? Index: " + outputIndex + ", Output size: " + outputs.size());
            CrayCrafting.logger.severe("** There will be uncraftable items. Please report with ENTIRE log and modlist.");
            CrayCrafting.logger.severe("*********************************************************************************************");
        }

        for (BaseType<IRecipe> baseType : typeList)
        {
            baseType.apply();
            root.setTag(baseType.getTypeName(), baseType.getNBTList());
        }

        try
        {
            CompressedStreamTools.write(root, recipeFile);
        }
        catch (IOException e)
        {
            CrayCrafting.logger.severe("*********************************************************************************************");
            CrayCrafting.logger.severe("** Fuck me. Something went wrong when saving the recipe file.");
            CrayCrafting.logger.severe("** Please report with ENTIRE log and modlist.");
            CrayCrafting.logger.severe("*********************************************************************************************");
            e.printStackTrace();
        }
    }
}
