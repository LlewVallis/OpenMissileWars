package org.astropeci.omw.worlds;

import lombok.Value;
import org.astropeci.omw.worlds.Arena;

@Value
public class NamedArena {

    public String name;
    public Arena arena;
}
