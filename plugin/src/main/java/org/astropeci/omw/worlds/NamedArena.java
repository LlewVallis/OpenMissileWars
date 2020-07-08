package org.astropeci.omw.worlds;

import lombok.Value;
import org.astropeci.omw.worlds.Arena;

@Value
public class NamedArena {

    public final String name;
    public final Arena arena;
}
