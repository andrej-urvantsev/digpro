package se.urvantsev.digpro.location;

import org.springframework.context.ApplicationEvent;

public class LocationLoadStartedEvent extends ApplicationEvent {

	public LocationLoadStartedEvent(Object source) {
		super(source);
	}

}
