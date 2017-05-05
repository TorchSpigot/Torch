package org.torch.api;

@FunctionalInterface
public interface TorchServant {
    /**
     * Returns the instance in Torch implemention
     */
    public abstract java.lang.Object getReactor();
}
