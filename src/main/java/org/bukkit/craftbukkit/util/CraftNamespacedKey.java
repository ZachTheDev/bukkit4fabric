package org.bukkit.craftbukkit.util;

import java.util.Optional;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;

public final class CraftNamespacedKey {

    public CraftNamespacedKey() {
    }

    public static NamespacedKey fromStringOrNull(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        Identifier minecraft = Identifier.tryParse(string);
        return (minecraft == null) ? null : fromMinecraft(minecraft);
    }

    public static NamespacedKey fromString(String string) {
        return fromMinecraft(new Identifier(string));
    }

    @SuppressWarnings("deprecation")
    public static NamespacedKey fromMinecraft(Identifier minecraft) {
        return new NamespacedKey(minecraft.getNamespace(), minecraft.getPath());
    }

    public static Identifier toMinecraft(NamespacedKey key) {
        return new Identifier(key.getNamespace(), key.getKey());
    }

    public static NamespacedKey fromMinecraft(Optional<RegistryKey<Biome>> key) {
        return fromMinecraft(key.get().getValue());
    }

}