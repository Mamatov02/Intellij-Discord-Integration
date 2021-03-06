package com.almightyalpaca.intellij.plugins.discord.collections.cloneable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CloneableHashMap<K, V extends ReallyCloneable<V>> extends HashMap<K, V> implements CloneableMap<K, V>
{
    public CloneableHashMap()
    {
        super();
    }

    public CloneableHashMap(int initialCapacity)
    {
        super(initialCapacity);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NotNull
    @Override
    public CloneableHashMap<K, V> clone()
    {
        CloneableHashMap<K, V> map = new CloneableHashMap<>(size());

        this.forEach((l, e) -> map.put(l, e.clone()));

        return map;
    }
}
