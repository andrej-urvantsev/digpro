package se.urvantsev.digpro.ui;

import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import se.urvantsev.digpro.location.LocationLoadStartedEvent;
import se.urvantsev.digpro.location.LocationsLoadedEvent;
import se.urvantsev.digpro.location.ReloadLocationsEvent;
import se.urvantsev.digpro.state.ApplicationState;
import se.urvantsev.digpro.state.ApplicationStateUpdated;
import se.urvantsev.digpro.state.UIResizedEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static java.util.Objects.requireNonNull;

@Component
public class MainWindow extends JFrame {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final ApplicationState applicationState;

	private final MapCanvas mapCanvas;

	private final JCheckBox autoReload = new JCheckBox("Auto-reload");

	private final JButton reload = new JButton("Reload");

	private final JProgressBar progressBar = new JProgressBar();

	public MainWindow(ApplicationEventPublisher applicationEventPublisher, ApplicationState applicationState,
			MapCanvas mapCanvas) {
		this.applicationEventPublisher = requireNonNull(applicationEventPublisher);
		this.applicationState = requireNonNull(applicationState);
		this.mapCanvas = requireNonNull(mapCanvas);
	}

	@PostConstruct
	void init() {
		setTitle("Digpro test(drag map with a mouse)");
		setSize(1024, 768);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new BorderLayout());

		var toolBarPanel = new JToolBar();
		toolBarPanel.setFloatable(false);
		toolBarPanel.setRollover(true);

		var center = new JButton("Center");
		center.setFocusable(false);
		toolBarPanel.add(center);
		toolBarPanel.addSeparator();
		center.addActionListener((e) -> mapCanvas.center());

		autoReload.setSelected(false);
		toolBarPanel.add(autoReload);
		toolBarPanel.addSeparator();
		autoReload.addChangeListener((e) -> applicationState.locationReloadEnabled(autoReload.isSelected()));

		reload.setFocusable(false);
		toolBarPanel.add(reload);
		toolBarPanel.addSeparator();
		reload.addActionListener((e) -> {
			reload.setEnabled(false);
			applicationEventPublisher.publishEvent(new ReloadLocationsEvent(this));
		});

		var about = new JButton("About");
		about.setFocusable(false);
		toolBarPanel.add(about);
		toolBarPanel.addSeparator();
		about.addActionListener((e) -> JOptionPane.showMessageDialog(this, """
				Author: Andrej Urvantsev
				Email: urvancevav(at)gmail.com
				""", "About", JOptionPane.INFORMATION_MESSAGE));

		var close = new JButton("Close");
		close.setFocusable(false);
		toolBarPanel.add(close);
		close.addActionListener((e) -> System.exit(0));

		add(toolBarPanel, BorderLayout.NORTH);

		add(mapCanvas, BorderLayout.CENTER);

		progressBar.setIndeterminate(false);
		progressBar.setValue(0);

		add(progressBar, BorderLayout.SOUTH);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				applicationEventPublisher.publishEvent(new UIResizedEvent(this));
			}
		});
	}

	@EventListener
	public void onLocationLoadStartedEvent(LocationLoadStartedEvent ignored) {
		SwingUtilities.invokeLater(() -> {
			reload.setEnabled(false);
			progressBar.setIndeterminate(true);
		});
	}

	@EventListener
	public void onLocationsLoadedEvent(LocationsLoadedEvent ignored) {
		SwingUtilities.invokeLater(() -> {
			progressBar.setIndeterminate(false);
			progressBar.setValue(0);
			reload.setEnabled(true);
		});
	}

	@EventListener
	public void onApplicationStateUpdated(ApplicationStateUpdated ignored) {
		SwingUtilities.invokeLater(() -> autoReload.setSelected(applicationState.locationReloadEnabled()));
	}

}
