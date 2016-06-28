package net.minecraft.server;

import javax.annotation.Nullable;

public class DataPaletteBlock implements DataPaletteExpandable {

    private static final DataPalette d = new DataPaletteGlobal();
    protected static final IBlockData a = Blocks.AIR.getBlockData();
    protected DataBits b; // Paper - nope
    protected DataPalette c;
    private int e = 0;
	private final short[] data = new short[4096]; // Paper
	private final IBlockData[] predefinedBlockData; // Paper - Anti-Xray - Blocks used for obfuscation (since 1.9 MC uses DataPalettes which have to be changed if more different blocks are used in a ChunkSection -> to avoid that while setting the fake-blocks we add them from the beginning)
    private final int[] currentPredefinedBlockData;

    public DataPaletteBlock() {
        this(null);
    }
	
	// Paper start - Anti-Xray - Modified constructor
 public DataPaletteBlock(@Nullable IBlockData[] predefinedBlockData) { // Paper - Anti-Xray - Add blocks used for obfuscation to the DataPalette
     this.predefinedBlockData = predefinedBlockData;
     if (predefinedBlockData == null) { // Paper - Anti-Xray - default constructor
         this.currentPredefinedBlockData = null;
         this.b(4);
     } else {
         this.currentPredefinedBlockData = new int[predefinedBlockData.length];
         int maxIndex = predefinedBlockData.length; // Paper - Anti-Xray - count bits of the maximum array index (+1 because AIR is also added) -> array length
         int bitCount = 0;
         while (maxIndex != 0) {
             maxIndex >>= 1;
             bitCount++;
         }
         this.b(bitCount == 0 ? 4 : bitCount); // Paper - Anti-Xray - initialize a DataPalette with bitCount
     }
 }
 // Paper end

 // Paper start - Anti-Xray - Getters
 @Nullable
 public IBlockData[] getPredefinedBlockData() {
     return this.predefinedBlockData;
     }
 
 @Nullable
 public int[] getCurrentPredefinedBlockData() {
     return this.currentPredefinedBlockData;
 }
 // Paper end

    private static int b(int i, int j, int k) {
        return j << 8 | k << 4 | i;
    }

    private void b(int i) {
        if (i != this.e) {
            this.e = i;
            if (this.e <= 4) {
                this.e = 4;
                this.c = new DataPaletteLinear(this.e, this);
            } else if (this.e <= 8) {
                this.c = new DataPaletteHash(this.e, this);
            } else {
                this.c = DataPaletteBlock.d;
                this.e = MathHelper.d(Block.REGISTRY_ID.a());
            }

            this.c.a(DataPaletteBlock.a);
			// Paper start - Anti-Xray - Add blocks used for obfuscation to the DataPalette and update the array with the current data bits
            if (this.predefinedBlockData != null) {
                for (int j = 0; j < this.predefinedBlockData.length; j++) {
                    this.currentPredefinedBlockData[j] = this.c.a(this.predefinedBlockData[j]);
                }
            }
            // Paper end
            this.b = new DataBits(this.e, 4096);
        }
    }

    public int a(int i, IBlockData iblockdata) {
        DataBits databits = this.b;
        DataPalette datapalette = this.c;

        this.b(i);

        for (int j = 0; j < databits.b(); ++j) {
            IBlockData iblockdata1 = datapalette.a(databits.a(j));

            if (iblockdata1 != null) {
                this.setBlockIndex(j, iblockdata1);
            }
        }

        return this.c.a(iblockdata);
    }

    public void setBlock(int i, int j, int k, IBlockData iblockdata) {
        this.setBlockIndex(b(i, j, k), iblockdata);
    }

    protected void setBlockIndex(int i, IBlockData iblockdata) {
        int j = this.c.a(iblockdata);
		data[i] = (short) Block.REGISTRY_ID.getId(iblockdata);
        this.b.a(i, j);
    }

    public IBlockData a(int i, int j, int k) {
        return this.a(b(i, j, k));
    }

    public IBlockData a(int i) { // Paper - Anti-Xray - protected -> public (Used inside the obfuscator loop)
        IBlockData iblockdata = Block.REGISTRY_ID.fromId(data[i]); // Paper - performance sanity

        return iblockdata == null ? DataPaletteBlock.a : iblockdata;
    }
	
	// Paper start - (Async-)Anti-Xray - Called instead of this.b(PacketDataSerializer packetdataserializer) (with nearbyChunks as parameter for the async part)
 public void serializeOrObfuscate(PacketDataSerializer packetdataserializer, Chunk chunk, int y, Chunk[] nearbyChunks) {
     packetdataserializer.writeByte(this.e);
     this.c.b(packetdataserializer);
     chunk.world.paperConfig.antiXrayInstance.serializeOrObfuscate(packetdataserializer, chunk, y, this, this.b, nearbyChunks);
 }
 // Paper end
	
	// Paper start - serialization method
   public DataBits toMojangBits() {
       DataBits bits = new DataBits(this.e, 4096);
       for (int i = 0; i < 4096; i++) {
           bits.a(i, this.c.a(Block.REGISTRY_ID.fromId(data[i])));
       }
       return bits;
   }
   // Paper end

    public void b(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.e);
        this.c.b(packetdataserializer);
        packetdataserializer.a(this.b.a());
    }

    @Nullable
    public NibbleArray exportData(byte[] abyte, NibbleArray nibblearray) {
        NibbleArray nibblearray1 = null;

        for (int i = 0; i < 4096; ++i) {
            int j = Block.REGISTRY_ID.getId(this.a(i));
            int k = i & 15;
            int l = i >> 8 & 15;
            int i1 = i >> 4 & 15;

            if ((j >> 12 & 15) != 0) {
                if (nibblearray1 == null) {
                    nibblearray1 = new NibbleArray();
                }

                nibblearray1.a(k, l, i1, j >> 12 & 15);
            }

            abyte[i] = (byte) (j >> 4 & 255);
            nibblearray.a(k, l, i1, j & 15);
        }

        return nibblearray1;
    }

    public void a(byte[] abyte, NibbleArray nibblearray, @Nullable NibbleArray nibblearray1) {
        for (int i = 0; i < 4096; ++i) {
            int j = i & 15;
            int k = i >> 8 & 15;
            int l = i >> 4 & 15;
            int i1 = nibblearray1 == null ? 0 : nibblearray1.a(j, k, l);
            int j1 = i1 << 12 | (abyte[i] & 255) << 4 | nibblearray.a(j, k, l);

            // CraftBukkit start - fix blocks with random data values (caused by plugins)
            IBlockData data = Block.REGISTRY_ID.fromId(j1);
            if (data == null) {
                Block block = Block.getById(j1 >> 4);
                if (block != null) {
                    try {
                        data = block.fromLegacyData(j1 & 0xF);
                    } catch (Exception ignored) {
                        data = block.getBlockData();
                    }
                }
            }
            this.setBlockIndex(i, data);
            // this.setBlockIndex(i, (IBlockData) Block.REGISTRY_ID.fromId(j1));
            // CraftBukkit end
        }

    }

    public int a() {
        return 1 + this.c.a() + PacketDataSerializer.a(this.b.b()) + this.b.a().length * 8;
    }
}
