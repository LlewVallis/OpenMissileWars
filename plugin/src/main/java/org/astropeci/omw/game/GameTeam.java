package org.astropeci.omw.game;

public enum GameTeam {

    GREEN,
    RED;

    @Override
    public String toString() {
        return this == GREEN ? "green" : "red";
    }
}
