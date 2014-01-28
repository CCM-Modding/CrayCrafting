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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Useful methods, all static.
 *
 * @author Dries007
 */
public class Helper
{
    private Helper() {}

    /**
     * Use for ItemStack(s) and OreDictionary only.
     */
    public static boolean inputEquals(Object[] input1, Object[] input2)
    {
        if (input1.length != input2.length) return false;
        for (int i = 0; i < input1.length; i++)
        {
            if (input1[i] == null && input2[i] == null) continue;
            if (input1[i] == null || input2[i] == null) return false;
            if (input1[i] instanceof ItemStack && input2[i] instanceof ItemStack)
            {
                if (itemStacksEqual((ItemStack) input1[i], (ItemStack) input2[i])) continue;
            }
            else if (input1[i] instanceof ArrayList && input2[i] instanceof ArrayList)
            {
                if (inputEquals((List) input1[i], (List) input2[i])) continue;
            }

            return false;
        }
        return true;
    }

    /**
     * Use for ItemStack(s) and OreDictionary only.
     */
    public static boolean inputEquals(ItemStack[] input1, ItemStack[] input2)
    {
        if (input1.length != input2.length) return false;
        for (int i = 0; i < input1.length; i++)
        {
            if (itemStacksEqual(input1[i], input2[i])) continue;
            return false;
        }
        return true;
    }

    /**
     * Use for ItemStack(s) and OreDictionary only.
     */
    public static boolean inputEquals(List input1, List input2)
    {
        if (input1.size() != input2.size()) return false;
        for (int i = 0; i < input1.size(); i++)
        {
            Object o1 = input1.get(i);
            Object o2 = input2.get(i);
            if (o1 instanceof ItemStack && o2 instanceof ItemStack)
            {
                if (itemStacksEqual((ItemStack) o1, (ItemStack) o2)) continue;
            }
            else if (o1 instanceof ArrayList && o2 instanceof ArrayList)
            {
                if (inputEquals((List) o1, (List) o2)) continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Use for ItemStack(s) and OreDictionary only.
     */
    public static boolean itemStacksEqual(ItemStack itemStack1, ItemStack itemStack2)
    {
        return ItemStack.areItemStacksEqual(itemStack1, itemStack2) && ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
    }

    /**
     * Send all of the data!
     */
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

    /**
     * Receive all of the data!
     */
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

    /**
     * Send all of the data to the stream!
     */
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

    /**
     * Receive all of the data from the stream!
     */
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
