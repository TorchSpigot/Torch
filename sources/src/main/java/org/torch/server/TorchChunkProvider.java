package org.torch.server;

import lombok.Getter;
import net.minecraft.server.ChunkProviderServer;

import org.torch.api.TorchReactor;

@Getter
public final class TorchChunkProvider implements TorchReactor {

	@Override
	public ChunkProviderServer getServant() {
		return null;
	}
    
}
