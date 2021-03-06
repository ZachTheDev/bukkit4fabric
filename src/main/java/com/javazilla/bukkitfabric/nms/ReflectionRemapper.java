/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.nms;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import com.javazilla.bukkitfabric.BukkitFabricMod;

/**
 * Very unsafe re-mapping of Reflection.
 */
public class ReflectionRemapper {

    private static final String NMS_VERSION = "v1_16_R2";
    public static JavaPlugin plugin;

    public static String mapClassName(String className) {
        if (className.startsWith("org.bukkit.craftbukkit." + NMS_VERSION + "."))
            return MappingsReader.getIntermedClass("org.bukkit.craftbukkit." + className.substring(23 + NMS_VERSION.length() + 1));

        if (className.startsWith("org.bukkit.craftbukkit.CraftServer."))
            return MappingsReader.getIntermedClass(className.replace("org.bukkit.craftbukkit.CraftServer.", "org.bukkit.craftbukkit."));

        if (className.startsWith("net.minecraft.server." + NMS_VERSION + "."))
            return MappingsReader.getIntermedClass(className.replace("net.minecraft.server." + NMS_VERSION + ".", "net.minecraft.server."));

        if (className.startsWith("org.bukkit.craftbukkit."))
            return MappingsReader.getIntermedClass(className); // We are not CraftBukkit, check for our own version of the class.

        if (className.startsWith("net.minecraft.server.CraftServer."))
            return MappingsReader.getIntermedClass(className.replace("net.minecraft.server.CraftServer.", "net.minecraft.server."));

        return className;
    }

    public static Class<?> getClassForName(String className) throws ClassNotFoundException {
        return getClassFromJPL(className);
    }

    public static Field getFieldByName(Class<?> calling, String f) throws ClassNotFoundException {
        try {
            Field field = calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Field a = whyIsAsmBroken.getDeclaredField(MappingsReader.getIntermedField(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    e2.printStackTrace();
                }
                return null;
            }
        }
    }

    public static Field getDeclaredFieldByName(Class<?> calling, String f) throws ClassNotFoundException {
        try {
            return calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Field a = whyIsAsmBroken.getDeclaredField(MappingsReader.getIntermedField(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    e1.printStackTrace();
                }
                return null;
            }
        }
    }

    public static Method getMethodByName(Class<?> calling, String f) throws ClassNotFoundException {
        Method m = getDeclaredMethodByName(calling, f);
        m.setAccessible(true);
        return m;
    }

    public static Method getDeclaredMethodByName(Class<?> calling, String f) throws ClassNotFoundException {
        try {
            return calling.getDeclaredMethod(MappingsReader.getIntermedMethod(calling.getName(), f));
        } catch (NoSuchMethodException | SecurityException e) {
            try {
                Method a = calling.getDeclaredMethod(MappingsReader.getIntermedMethod(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchMethodException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Method a = whyIsAsmBroken.getDeclaredMethod(MappingsReader.getIntermedMethod(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchMethodException | SecurityException e2) {
                    e1.printStackTrace();
                }
                return null;
            }
        }
    }

    /**
     * Retrieve a class that is from a plugin
     * 
     * @author Isaiah
     */
    @SuppressWarnings("unchecked")
    public static Class<?> getClassFromJPL(String name) {
        try {
            SimplePluginManager pm = (SimplePluginManager) Bukkit.getPluginManager();
            Field fa = SimplePluginManager.class.getDeclaredField("fileAssociations");
            fa.setAccessible(true);
            Map<Pattern, PluginLoader> pl = (Map<Pattern, PluginLoader>) fa.get(pm);
            JavaPluginLoader jpl = null;
            for (PluginLoader loader : pl.values()) {
                if (loader instanceof JavaPluginLoader) {
                    jpl = (JavaPluginLoader) loader;
                    break;
                }
            }

            Method fc = JavaPluginLoader.class.getDeclaredMethod("getClassByName", String.class);
            fc.setAccessible(true);
            return (Class<?>) fc.invoke(jpl, name);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            BukkitFabricMod.LOGGER.warning("SOMETHING EVERY WRONG! PLEASE REPORT THE EXCEPTION BELOW TO BUKKIT4FABRIC:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     */
    public static String getCallerClassName() { 
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(ReflectionRemapper.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0)
                return ste.getClassName();
        }
        return null;
    }

    /**
     */
    public static String getPackageName(Package pkage) {
        String name = pkage.getName();
        if (name.startsWith("org.bukkit.craftbukkit"))
            name = name.replace("org.bukkit.craftbukkit", "org.bukkit.craftbukkit." + NMS_VERSION);
        return name;
    }

}