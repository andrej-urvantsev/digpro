package se.urvantsev.digpro.state;

import org.springframework.context.ApplicationEvent;

public class ApplicationStateUpdated extends ApplicationEvent {

    public ApplicationStateUpdated(Object source) {
        super(source);
    }
}
