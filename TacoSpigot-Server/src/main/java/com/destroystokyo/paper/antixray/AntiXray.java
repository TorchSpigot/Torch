package com.destroystokyo.paper.antixray;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.destroystokyo.paper.PaperWorldConfig;
import net.minecraft.server.Block;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Blocks;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.DataBits;
import net.minecraft.server.DataPaletteBlock;
import net.minecraft.server.IBlockData;
import net.minecraft.server.PacketDataSerializer;
import net.minecraft.server.PacketPlayOutMapChunk;
import net.minecraft.server.World;

public class AntiXray {
private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
private final boolean antiXray;
private final int engineMode;
private final int maxChunkY;
private final boolean asynchronous;
private final int neighborsMode;
// Used to keep track of which blocks to obfuscate
private final boolean[] obfuscateBlocks = new boolean[Short.MAX_VALUE];
// Used to select a random replacement ore
private final IBlockData[] replacementOres;

public AntiXray(PaperWorldConfig config) {
    antiXray = config.antiXray;
    engineMode = config.engineMode;
    maxChunkY = config.maxChunkY;
    asynchronous = config.asynchronous;
    neighborsMode = config.neighborsMode;
    // Set all listed blocks as true to be obfuscated
    for (Object id : (engineMode == 1) ? config.hiddenBlocks : config.replaceBlocks) {
        Block block = Block.getByName(String.valueOf(id));

        if (block != null) {
            int intId = Block.getId(block);
            obfuscateBlocks[intId] = true;
        }
    }

    Set<IBlockData> replacementOreList = new HashSet<IBlockData>();

    for (Object id : config.hiddenBlocks) {
        Block block = Block.getByName(String.valueOf(id));
        // Check it exists and is not a tile entity
        if (block != null && !block.isTileEntity()) {
            // Add it to the list of replacement blocks
            replacementOreList.add(block.getBlockData());
        }
    }

    replacementOres = replacementOreList.toArray(new IBlockData[replacementOreList.size()]);
}

public IBlockData[] getPredefinedBlockData(Chunk chunk, int chunkY) {
    if (antiXray && chunkY <= maxChunkY) {
        switch (engineMode) {
            case 1:
                switch (chunk.world.getWorld().getEnvironment()) {
                    case NETHER:
                        return new IBlockData[] {Blocks.NETHERRACK.getBlockData()};
                    case THE_END:
                        return new IBlockData[] {Blocks.END_STONE.getBlockData()};
                    default:
                        return new IBlockData[] {Blocks.STONE.getBlockData()};
                }
            case 2:
            case 3:
                return replacementOres;
        }
    }

    return null;
}

public boolean onPacketCreate(Chunk chunk, int chunkSectionSelector) {
    if (antiXray) {
        if (neighborsMode == 1) {
            return true;
        } else if (neighborsMode == 2) {
            if (chunk.world.getChunkIfLoaded(chunk.locX - 1, chunk.locZ) == null || chunk.world.getChunkIfLoaded(chunk.locX + 1, chunk.locZ) == null || chunk.world.getChunkIfLoaded(chunk.locX, chunk.locZ - 1) == null || chunk.world.getChunkIfLoaded(chunk.locX, chunk.locZ + 1) == null) {
                return false;
            } else {
                return true;
            }
        } else if (neighborsMode == 3) {
            chunk.world.getChunkAt(chunk.locX - 1, chunk.locZ);
            chunk.world.getChunkAt(chunk.locX + 1, chunk.locZ);
            chunk.world.getChunkAt(chunk.locX, chunk.locZ - 1);
            chunk.world.getChunkAt(chunk.locX, chunk.locZ + 1);
            return true;
        }
    }

    return true;
}

public void createPacket(PacketPlayOutMapChunk packetPlayOutMapChunk, PacketDataSerializer packetDataSerializer, Chunk chunk, boolean writeSkyLightArray, int chunkSectionSelector) {
    if (antiXray) {
        Chunk[] nearbyChunks = {chunk.world.getChunkIfLoaded(chunk.locX - 1, chunk.locZ), chunk.world.getChunkIfLoaded(chunk.locX + 1, chunk.locZ), chunk.world.getChunkIfLoaded(chunk.locX, chunk.locZ - 1), chunk.world.getChunkIfLoaded(chunk.locX, chunk.locZ + 1)};

        if (asynchronous) {
            executorService.execute(new ObfuscatorRunnable(packetPlayOutMapChunk, packetDataSerializer, chunk, writeSkyLightArray, chunkSectionSelector, nearbyChunks));
        } else {
            packetPlayOutMapChunk.setWrittenChunkSections(packetPlayOutMapChunk.a(packetDataSerializer, chunk, writeSkyLightArray, chunkSectionSelector, nearbyChunks));
            packetPlayOutMapChunk.setReady(true);
        }
    } else {
        packetPlayOutMapChunk.setWrittenChunkSections(packetPlayOutMapChunk.a(packetDataSerializer, chunk, writeSkyLightArray, chunkSectionSelector, null));
        packetPlayOutMapChunk.setReady(true);
    }
}

public void serializeOrObfuscate(PacketDataSerializer packetDataSerializer, Chunk chunk, int chunkY, DataPaletteBlock dataPaletteBlock, DataBits dataBits, Chunk[] nearbyChunks) {
    long[] dataBitsArray = dataBits.a();

    if (antiXray && chunkY <= maxChunkY && dataPaletteBlock.getCurrentPredefinedBlockData() != null && dataPaletteBlock.getCurrentPredefinedBlockData().length > 0 && nearbyChunks != null) {
        // The iterator marking which random ore we should use next
        int randomOre = 0;
        // Boolean used to check if the engine mode is 3 (used for the initial value of x in the inner loop)
        boolean engineMode3 = engineMode == 3;
        // Increment the inner loop by 3 in engine mode 3 for more efficiency
        int increment = engineMode3 ? 3 : 1;
        // Stores the last dataBits-array-index which was obfuscated
        int dataBitsIndex = 0;
        // Stores the last data which was obfuscated but not written to the packet
        long currentData = dataBitsArray[0];
        // Write the length of the dataBits-array to the packet as it is in vanilla
        packetDataSerializer.d(dataBitsArray.length);
        int xMin = nearbyChunks[0] == null ? 1 : 0;
        int xMax = nearbyChunks[1] == null ? 15 : 16;
        int zMin = nearbyChunks[2] == null ? 1 : 0;
        int zMax = nearbyChunks[3] == null ? 15 : 16;
        // Write the dataBits-array to the packet
        // Work through all blocks in the chunkSection
        for (int y = 0; y < 16; y++) {
            for (int z = zMin; z < zMax; z++) {
                // Shift the initial value of x and increment by 3 in engine mode 3
                int x;

                if (engineMode3) {
                    x = (y + z) % 3;

                    if (x < xMin) {
                        x += 3;
                    }
                } else {
                    x = xMin;
                }

                for (; x < xMax; x += increment) {
                    // Calculate the blockIndex from y, z, x and get the blockData from dataPaletteBlock
                    // More efficient because we may use the blockIndex again later
                    int blockIndex = y << 8 | z << 4 | x;
                    IBlockData blockData = dataPaletteBlock.a(blockIndex);
                    // Check if the block should be obfuscated
                    if (obfuscateBlocks[Block.getId(blockData.getBlock())]) {
                        // Check if the nearby blocks are not transparent, we can obfuscate
                        if (isHiddenBlock(x, y, z, chunk, chunkY, nearbyChunks)) {
                            // Get one of the predefined blocks which can be used for obfuscation
                            if (randomOre >= dataPaletteBlock.getCurrentPredefinedBlockData().length) {
                                randomOre = 0;
                            }

                            int newBlockData = dataPaletteBlock.getCurrentPredefinedBlockData()[randomOre++];
                            // Get the current dataBits-array-index for the block index
                            int currentDataBitsIndex = dataBits.getArrayIndex(blockIndex);
                            // Check if it has been changed
                            if (currentDataBitsIndex != dataBitsIndex) {
                                // If so, we can write the last obfuscated data to the packet because it is finished with the obfuscation
                                packetDataSerializer.writeLong(currentData);
                                dataBitsIndex++;
                                // We can also write all further content of the dataBits-array to the packet (until currentDataBitsIndex is reached)
                                // because they didn't change
                                while (dataBitsIndex < currentDataBitsIndex) {
                                    packetDataSerializer.writeLong(dataBitsArray[dataBitsIndex]);
                                    dataBitsIndex++;
                                }
                                // Now we get the data which has to be obfuscated
                                currentData = dataBitsArray[dataBitsIndex];
                            }
                            // Obfuscate currentData
                            currentData = dataBits.obfuscate(blockIndex, newBlockData, currentData);
                            // Check if the data of the current block is spitted to the next dataBits-array-index
                            if (dataBits.isSplitted(blockIndex, dataBitsIndex)) {
                                // If so, we can write currentData to the packet because it is finished with the obfuscation
                                packetDataSerializer.writeLong(currentData);
                                dataBitsIndex++;
                                // Get the data at the next index
                                currentData = dataBitsArray[dataBitsIndex];
                                // And obfuscate it
                                currentData = dataBits.obfuscateSplittedPart(blockIndex, newBlockData, currentData);
                            }
                        }
                    }
                }
            }
        }
        // Write the rest of the dataBits-array to the packet
        packetDataSerializer.writeLong(currentData);
        dataBitsIndex++;

        while (dataBitsIndex < dataBitsArray.length) {
            packetDataSerializer.writeLong(dataBitsArray[dataBitsIndex]);
            dataBitsIndex++;
        }
    } else {
        packetDataSerializer.a(dataBitsArray);
    }
}

public void updateNearbyBlocks(World world, BlockPosition position) {
    if (antiXray) {
        // 2 is the radius, we shouldn't change it as that would make it exponentially slower
        updateNearbyBlocks(world, position, 2, false);
    }
}

private void updateNearbyBlocks(World world, BlockPosition position, int radius, boolean updateSelf) {
    // If the block in question is loaded
    if (world.isLoaded(position)) {
        // Get block id
        Block block = world.getType(position).getBlock();
        // See if it needs update
        if (updateSelf && obfuscateBlocks[Block.getId(block)]) {
            // Send the update
            world.notify(position);
        }
        // Check other blocks for updates
        if (radius > 0) {
            updateNearbyBlocks(world, position.east(), radius - 1, true);
            updateNearbyBlocks(world, position.west(), radius - 1, true);
            updateNearbyBlocks(world, position.up(), radius - 1, true);
            updateNearbyBlocks(world, position.down(), radius - 1, true);
            updateNearbyBlocks(world, position.south(), radius - 1, true);
            updateNearbyBlocks(world, position.north(), radius - 1, true);
        }
    }
}

private static boolean isHiddenBlock(int x, int y, int z, Chunk chunk, int chunkY, Chunk[] nearbyChunks) {
    return isSolidBlock(getType(x, y + 1, z, chunk, chunkY, nearbyChunks).getBlock())
        && isSolidBlock(getType(x + 1, y, z, chunk, chunkY, nearbyChunks).getBlock())
        && isSolidBlock(getType(x - 1, y, z, chunk, chunkY, nearbyChunks).getBlock())
        && isSolidBlock(getType(x, y, z + 1, chunk, chunkY, nearbyChunks).getBlock())
        && isSolidBlock(getType(x, y, z - 1, chunk, chunkY, nearbyChunks).getBlock())
        && isSolidBlock(getType(x, y - 1, z, chunk, chunkY, nearbyChunks).getBlock());
}

private static IBlockData getType(int x, int y, int z, Chunk chunk, int chunkY, Chunk[] nearbyChunks) {
    if (x < 0) {
        chunk = nearbyChunks[0];
    } else if (x > 15) {
        chunk = nearbyChunks[1];
    } else if (z < 0) {
        chunk = nearbyChunks[2];
    } else if (z > 15) {
        chunk = nearbyChunks[3];
    }

    int blockY = (chunkY << 4) + y;

    if (blockY >= 0 && blockY >> 4 < chunk.getSections().length) {
        ChunkSection chunkSection = chunk.getSections()[blockY >> 4];

        if (chunkSection != Chunk.a) {
            return chunkSection.getType(x & 15, y & 15, z & 15);
        }
    }

    return Blocks.AIR.getBlockData();
}

private static boolean isSolidBlock(Block block)
{
    return block.isOccluding(block.getBlockData()) && block != Blocks.MOB_SPAWNER && block != Blocks.BARRIER;
}
}