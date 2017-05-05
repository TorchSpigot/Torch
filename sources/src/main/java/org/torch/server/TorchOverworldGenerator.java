package org.torch.server;

import lombok.Getter;
import net.minecraft.server.BiomeBase.BiomeMeta;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkGenerator;
import net.minecraft.server.ChunkProviderGenerate;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.World;

import java.util.List;

import org.torch.api.TorchReactor;

@Getter
public final class TorchOverworldGenerator implements ChunkGenerator, TorchReactor {

    @Override
    public ChunkProviderGenerate getServant() {
        return null;
    }

    @Deprecated @Override
    public boolean a(Chunk arg0, int arg1, int arg2) {
        return false;
    }

    @Override
    public BlockPosition findNearestMapFeature(World arg0, String arg1, BlockPosition arg2, boolean arg3) {
        return null;
    }

    @Override
    public List<BiomeMeta> getMobsFor(EnumCreatureType arg0, BlockPosition arg1) {
        return null;
    }

    @Override
    public Chunk getOrCreateChunk(int arg0, int arg1) {
        return null;
    }

    @Override
    public void recreateStructures(int arg0, int arg1) {

    }

    @Override
    public void recreateStructures(Chunk arg0, int arg1, int arg2) {

    }
}
