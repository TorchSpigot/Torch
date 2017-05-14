package org.spigotmc;

import java.util.Locale;

import org.torch.server.cache.Caches;

import gnu.trove.strategy.HashingStrategy;

class CaseInsensitiveHashingStrategy implements HashingStrategy<String> {

    private static final long serialVersionUID = -1L;
    
    static final CaseInsensitiveHashingStrategy INSTANCE = new CaseInsensitiveHashingStrategy();

    @Override
    public int computeHashCode(String object) {
        return Caches.toLowerCase(object, Locale.ROOT).hashCode();
    }

    @Override
    public boolean equals(String o1, String o2) {
        return o1.equals(o2) || (o1 instanceof String && o2 instanceof String && Caches.toLowerCase(o1, Locale.ROOT).equals(Caches.toLowerCase(o2, Locale.ROOT)));
    }
}
