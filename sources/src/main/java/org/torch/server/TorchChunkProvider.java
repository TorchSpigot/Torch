package org.torch.server;

import lombok.Getter;
import org.torch.api.TorchReactor;

@Getter
public final class TorchChunkProvider implements TorchReactor {

	@Override
	public Object getServant() {
		return null;
	}
    
}
