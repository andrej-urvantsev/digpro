package se.urvantsev.digpro;

import org.springframework.context.ApplicationEvent;

public class UIResizedEvent extends ApplicationEvent {

    public UIResizedEvent(Object source) {
        super(source);
    }
}
