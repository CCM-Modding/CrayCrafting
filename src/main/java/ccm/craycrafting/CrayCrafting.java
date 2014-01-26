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

package ccm.craycrafting;

import ccm.craycrafting.util.ConnectionHandler;
import static ccm.craycrafting.util.Constants.*;
import ccm.craycrafting.util.Helper;
import ccm.craycrafting.util.PacketHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Mod(modid = MODID, name = MODID)
@NetworkMod(clientSideRequired = true, connectionHandler = ConnectionHandler.class, packetHandler = PacketHandler.class, channels = {MODID})
public class CrayCrafting
{
    public static Logger         logger;
    private       File           recipeFile;
    public static NBTTagCompound root;

    @Mod.Metadata(MODID)
    public static ModMetadata metadata;

    @Mod.EventHandler()
    public void eventHandler(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
        try
        {
            new Metrics(metadata.modId, metadata.version).start();
        }
        catch (IOException e)
        {
            logger.warning("Something went wrong, no metrics.");
            e.printStackTrace();
        }
    }

    @Mod.EventHandler()
    public void eventHandler(FMLServerStartedEvent event)
    {
        recipeFile = new File(DimensionManager.getCurrentSaveRootDirectory(), MODID + ".dat");
        if (recipeFile.exists())
        {
            try
            {
                root = CompressedStreamTools.read(recipeFile);
                Helper.loadRecipesFromNBT(root);
                return;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        Helper.randomizeRecipes(recipeFile);
    }
}
