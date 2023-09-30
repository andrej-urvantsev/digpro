package se.urvantsev.digpro;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.urvantsev.digpro.ui.MainWindow;

// @SpringBootApplication
@Configuration
@ComponentScan
@Import({
    LifecycleAutoConfiguration.class,
    PropertyPlaceholderAutoConfiguration.class,
    TaskExecutionAutoConfiguration.class
})
public class ApplicationRunner {

    public static void main(String[] args)
            throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException,
                    IllegalAccessException {

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            for (var info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }

        var ctx = new SpringApplicationBuilder(ApplicationRunner.class)
                .headless(false)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            var ex = ctx.getBean(MainWindow.class);
            ex.setVisible(true);
        });
    }
}
