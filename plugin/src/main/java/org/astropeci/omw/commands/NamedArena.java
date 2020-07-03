package org.astropeci.omw.commands;

import org.astropeci.omw.worlds.Arena;

public class NamedArena {

    public final String name;
    public final Arena arena;

    public NamedArena(String name, Arena arena) {
        this.name = name;
        this.arena = arena;
    }
}
