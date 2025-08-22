package se.urvantsev.digpro.location;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import se.urvantsev.digpro.state.ApplicationState;
import se.urvantsev.digpro.state.ApplicationStateUpdated;

@Component
public class LocationFetcher {

    private static final String LOCATIONS_URL =
            "https://daily.digpro.se/bios/servlet/bios.servlets.web.RecruitmentTestServlet";

    private static final ReentrantLock FETCH_LOCK = new ReentrantLock();

    private static final ReentrantLock STATE_LOCK = new ReentrantLock();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ApplicationState applicationState;

    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    private ScheduledFuture<?> scheduledFuture = null;

    private final OkHttpClient httpClient;

    LocationFetcher(
            ApplicationEventPublisher applicationEventPublisher,
            ApplicationState applicationState,
            OkHttpClient httpClient) {
        this.applicationEventPublisher = requireNonNull(applicationEventPublisher);
        this.applicationState = requireNonNull(applicationState);
        this.httpClient = requireNonNull(httpClient);

        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("LocationFetcher");
        taskScheduler.afterPropertiesSet();
    }

    private void fetchLocations() {
        try {
            if (!FETCH_LOCK.tryLock(0, TimeUnit.SECONDS) && !FETCH_LOCK.tryLock(1, TimeUnit.MINUTES)) {
                return;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error("Can't lock on locations fetcher", ex);
            return;
        }
        var locations = new HashSet<Location>();
        try {
            logger.debug("Start fetch");
            applicationEventPublisher.publishEvent(new LocationLoadStartedEvent(this));

            var request = new Request.Builder().url(LOCATIONS_URL).build();

            try (var response = httpClient.newCall(request).execute()) {
                var body = response.body();
                var sc = new Scanner(body.byteStream(), StandardCharsets.ISO_8859_1);
                while (sc.hasNextLine()) {
                    var line = sc.nextLine().strip();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    var optionalLocation = Location.parse(line);
                    if (optionalLocation.isEmpty()) {
                        logger.warn("Location can't be parsed: {}", line.replaceAll("[\r\n]", ""));
                    } else {
                        var location = optionalLocation.get();
                        logger.debug("Received {}", location.toString().replaceAll("[\r\n]", ""));
                        locations.add(location);
                    }
                }
                logger.debug("Received {} locations", locations.size());

            } catch (IOException ex) {
                logger.error("Fetching locations failed", ex);
            }
        } finally {
            applicationEventPublisher.publishEvent(new LocationsLoadedEvent(this, locations));
            FETCH_LOCK.unlock();
        }
    }

    @EventListener
    public void onLocationLoadEvent(ReloadLocationsEvent ignored) {
        logger.debug("Locations reload requested");
        taskScheduler.execute(this::fetchLocations);
    }

    @EventListener
    public void onApplicationStateUpdated(ApplicationStateUpdated ignored) throws InterruptedException {
        if (STATE_LOCK.tryLock(0, TimeUnit.SECONDS) || STATE_LOCK.tryLock(3, TimeUnit.SECONDS)) {
            try {
                if (scheduledFuture == null && applicationState.locationReloadEnabled()) {
                    logger.debug("Enabling auto-reload");
                    scheduledFuture = taskScheduler.scheduleAtFixedRate(this::fetchLocations, Duration.ofSeconds(30));
                } else if (scheduledFuture != null && !applicationState.locationReloadEnabled()) {
                    logger.debug("Disabling auto-reload");
                    scheduledFuture.cancel(false);
                    scheduledFuture = null;
                }
            } finally {
                STATE_LOCK.unlock();
            }
        }
    }

    @EventListener
    public void onApplicationStarted(ApplicationStartedEvent ignored) {
        taskScheduler.execute(this::fetchLocations);
    }
}
