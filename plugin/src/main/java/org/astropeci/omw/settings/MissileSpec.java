package org.astropeci.omw.settings;

import lombok.Getter;

import java.util.Map;

import static org.astropeci.omw.settings.ConfigUtil.getInt;
import static org.astropeci.omw.settings.ConfigUtil.getString;

@Getter
public class MissileSpec {

    private final String structureName;
    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;
    private final int width;
    private final int height;
    private final int length;

    public MissileSpec(Map<?, ?> map) {
        structureName = getString(map, "structureName");
        offsetX = getInt(map, "offsetX");
        offsetY = getInt(map, "offsetY");
        offsetZ = getInt(map, "offsetZ");
        width = getInt(map, "width");
        height = getInt(map, "height");
        length = getInt(map, "length");
    }
}
