package org.astropeci.omw.commands;

import lombok.Value;
import org.astropeci.omw.game.Arena;

@Value
public class NamedArena {

    public final String name;
    public final Arena arena;
}
