package org.torch.api;

@FunctionalInterface
public interface TorchReactor {
	/**
	 * Returns the instance in NMS internal(fake) implemention
	 */
	@Deprecated public abstract java.lang.Object getServant();
}
