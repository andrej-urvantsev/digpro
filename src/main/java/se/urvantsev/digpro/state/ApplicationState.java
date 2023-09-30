package se.urvantsev.digpro.state;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ApplicationState {

    private final ApplicationEventPublisher applicationEventPublisher;

    private boolean locationReloadEnabled = false;

    @PostConstruct
    void init() {
        publishUpdate();
    }

    private void publishUpdate() {
        applicationEventPublisher.publishEvent(new ApplicationStateUpdated(this));
    }

    public ApplicationState(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = requireNonNull(applicationEventPublisher);
    }

    public boolean locationReloadEnabled() {
        return locationReloadEnabled;
    }

    public void locationReloadEnabled(boolean locationReloadEnabled) {
        this.locationReloadEnabled = locationReloadEnabled;
        publishUpdate();
    }
}
