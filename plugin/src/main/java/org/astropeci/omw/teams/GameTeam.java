package org.astropeci.omw.teams;

public enum GameTeam {

    GREEN,
    RED;

    @Override
    public String toString() {
        return this == GREEN ? "green" : "red";
    }
}
