package functions;

import java.util.List;

public final class BlockMessage {

    private String message;
    private String alertQuery;
    private List<String> actions;

    public BlockMessage setMessage(final String message) {
        this.message = message;
        return this;
    }

    public BlockMessage setAlertQuery(final String alertQuery) {
        this.alertQuery = alertQuery;
        return this;
    }

    public BlockMessage setActions(final List<String> actions) {
        this.actions = actions;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public String getAlertQuery() {
        return alertQuery;
    }

    public List<String> getActions() {
        return actions;
    }

}
