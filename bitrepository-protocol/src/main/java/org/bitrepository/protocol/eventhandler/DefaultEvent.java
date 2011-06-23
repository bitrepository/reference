package org.bitrepository.protocol.eventhandler;

public class DefaultEvent implements OperationEvent {
    private final OperationEventType type;
    private final String info;

    public DefaultEvent(OperationEventType type, String info) {
        super();
        this.type = type;
        this.info = info;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public OperationEventType getType() {
        return type;
    }
}
