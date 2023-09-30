package se.urvantsev.digpro.location;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import org.springframework.context.ApplicationEvent;

public class LocationsLoadedEvent extends ApplicationEvent {

    private final Set<Location> locations;

    public LocationsLoadedEvent(Object source, Set<Location> locations) {
        super(source);
        this.locations = requireNonNull(locations);
    }

    public Set<Location> locations() {
        return locations;
    }
}
