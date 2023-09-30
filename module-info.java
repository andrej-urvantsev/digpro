module se.urvantsev.digpro {
    requires java.desktop;
    requires jakarta.annotation;
    requires com.formdev.flatlaf;
    requires okhttp3;
    requires okio;
    requires org.slf4j;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.boot;

    opens se.urvantsev.digpro to
            spring.core;

    exports se.urvantsev.digpro;
}
