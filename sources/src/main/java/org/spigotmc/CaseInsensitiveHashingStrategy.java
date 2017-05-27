package org.spigotmc;

import gnu.trove.strategy.HashingStrategy;

@SuppressWarnings("serial")
class CaseInsensitiveHashingStrategy implements HashingStrategy<String> {
    
    static final CaseInsensitiveHashingStrategy INSTANCE = new CaseInsensitiveHashingStrategy();

    @Override
    public int computeHashCode(String object) {
        return object.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(String o1, String o2) {
        return o1.equals(o2) || (o1 instanceof String && o2 instanceof String && o1.equalsIgnoreCase(o2));
    }
}
