package handlers;

import org.jetbrains.annotations.NotNull;

public interface AlertHandler {

    void alert(final @NotNull String message, final @NotNull String permission);

}
