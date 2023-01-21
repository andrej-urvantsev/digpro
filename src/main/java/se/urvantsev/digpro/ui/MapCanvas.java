package se.urvantsev.digpro.ui;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import se.urvantsev.digpro.location.Location;
import se.urvantsev.digpro.location.LocationsLoadedEvent;
import se.urvantsev.digpro.state.UIResizedEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class MapCanvas extends JPanel {

	private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private long x = 0L;

	private long y = 0L;

	private Set<Location> allLocations = Set.of();

	private Set<Location> visibleLocations = Set.of();

	private final Set<JLabel> points = new HashSet<>();

	private Point mousePt;

	@PostConstruct
	void init() {
		setLayout(null);

		var mouseAdapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mousePt = e.getPoint();
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (mousePt == null) {
					return;
				}
				int dx = e.getX() - mousePt.x;
				int dy = e.getY() - mousePt.y;
				x = x - dx;
				y = y - dy;
				logger.trace("Viewport start at: ({}, {})", x, y);
				mousePt = e.getPoint();
				MapCanvas.this.refresh();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mousePt = null;
				repaint();
			}
		};
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	private void refresh() {
		EXECUTOR.execute(() -> {
			// For big datasets something like kd-trees range should be more performant
			this.visibleLocations = allLocations.stream()
					.filter((l) -> l.x() >= x && l.x() < (x + getWidth()) && l.y() >= y && l.y() < (y + getHeight()))
					.collect(Collectors.toSet());

			logger.trace("{} points in the viewport", this.visibleLocations.size());

			points.forEach(this::remove);
			points.clear();

			for (var location : visibleLocations) {
				var point = new JLabel("✜");
				point.setForeground(Color.BLACK);
				point.setSize(point.getFont().getSize(), point.getFont().getSize());
				point.setFont(getFont().deriveFont(Map.of(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD)));
				point.setLocation((int) (location.x() - x), (int) (location.y() - y));
				point.setToolTipText(location.name());
				points.add(point);
				this.add(point);
			}
			SwingUtilities.invokeLater(() -> {
				revalidate();
				repaint();
			});
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (g instanceof Graphics2D canvas) {
			canvas.setPaint(new Color(120, 150, 120));
			canvas.fillRect(0, 0, getWidth(), getHeight());

			canvas.setPaint(Color.BLACK);
			canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			canvas.setStroke(new BasicStroke(2));
		}
		else {
			super.paintComponent(g);
		}

	}

	public void center() {
		x = 0;
		y = 0;
		refresh();
	}

	@EventListener
	public void onLocationsLoadedEvent(LocationsLoadedEvent event) {
		this.allLocations = event.locations();
		refresh();
	}

	@EventListener
	public void onUIResizedEvent(UIResizedEvent ignored) {
		refresh();
	}

}
