package se.urvantsev.digpro.state;

import org.springframework.context.ApplicationEvent;

public class UIResizedEvent extends ApplicationEvent {

	public UIResizedEvent(Object source) {
		super(source);
	}

}
