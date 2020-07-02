package org.astropeci.omw.commandbuilder;

import java.util.List;

public interface CommandCallback {

    boolean onSuccess(List<Object> argumentValues, List<Object> variadicArgumentValues, CommandContext context);

    boolean onFailure(CommandParseException cause, CommandContext context);
}
