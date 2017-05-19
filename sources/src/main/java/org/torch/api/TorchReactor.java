package org.torch.api;

import javax.annotation.Nullable;

@FunctionalInterface
public interface TorchReactor {
    /**
     * Returns the instance in NMS internal(fake) implemention
     */
    @Nullable @Deprecated public abstract java.lang.Object getServant();
}
