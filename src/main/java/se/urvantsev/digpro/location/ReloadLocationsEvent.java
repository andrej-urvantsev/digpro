package se.urvantsev.digpro.location;

import org.springframework.context.ApplicationEvent;

public class ReloadLocationsEvent extends ApplicationEvent {

    public ReloadLocationsEvent(Object source) {
        super(source);
    }
}
