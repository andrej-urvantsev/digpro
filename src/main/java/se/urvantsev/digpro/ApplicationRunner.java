package se.urvantsev.digpro;

import com.formdev.flatlaf.FlatDarkLaf;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import se.urvantsev.digpro.ui.MainWindow;

import javax.swing.*;

@SpringBootApplication
public class ApplicationRunner {

	public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		try {
			UIManager.setLookAndFeel(new FlatDarkLaf());
		}
		catch (Exception ex) {
			for (var info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}

		var ctx = new SpringApplicationBuilder(ApplicationRunner.class).headless(false).run(args);

		SwingUtilities.invokeLater(() -> {
			var ex = ctx.getBean(MainWindow.class);
			ex.setVisible(true);
		});
	}

}
