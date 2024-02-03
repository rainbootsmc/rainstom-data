package net.minestom.generators;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.SoundType;
import net.minestom.datagen.DataGenerator;

import java.util.Arrays;

public class SoundTypeGenerator extends DataGenerator {
    @Override
    public JsonElement generate() throws Exception {
        final var jsonObject = new JsonObject();
        Arrays.stream(SoundType.class.getDeclaredFields())
                .filter(field -> {
                    try {
                        return field.get(null) instanceof SoundType;
                    } catch (Throwable e) {
                        return false;
                    }
                })
                .forEach(field -> {
                    try {
                        final var name = field.getName();
                        final var soundType = (SoundType) field.get(null);
                        final var obj = new JsonObject();
                        obj.addProperty("volume", soundType.volume);
                        obj.addProperty("pitch", soundType.pitch);
                        obj.addProperty("breakSound", soundType.getBreakSound().getLocation().toString());
                        obj.addProperty("stepSound", soundType.getStepSound().getLocation().toString());
                        obj.addProperty("placeSound", soundType.getPlaceSound().getLocation().toString());
                        obj.addProperty("hitSound", soundType.getHitSound().getLocation().toString());
                        obj.addProperty("fallSound", soundType.getFallSound().getLocation().toString());
                        jsonObject.add(name, obj);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
        return jsonObject;
    }
}
