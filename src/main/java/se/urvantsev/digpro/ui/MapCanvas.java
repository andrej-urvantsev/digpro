package se.urvantsev.digpro.ui;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import se.urvantsev.digpro.UIResizedEvent;
import se.urvantsev.digpro.location.Location;
import se.urvantsev.digpro.location.LocationsLoadedEvent;

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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class MapCanvas extends JPanel {

	private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AtomicLong x = new AtomicLong();

	private final AtomicLong y = new AtomicLong();

	private final Set<Location> allLocations = new HashSet<>();

	private final Set<Location> visibleLocations = new HashSet<>();

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
				x.addAndGet(-dx);
				y.addAndGet(-dy);
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
			this.visibleLocations.clear();
			var viewportX = x.get();
			var viewportY = y.get();
			this.visibleLocations
					.addAll(allLocations
							.stream().filter((l) -> l.x() >= viewportX && l.x() < (viewportX + getWidth())
									&& l.y() >= viewportY && l.y() < (viewportY + getHeight()))
							.collect(Collectors.toSet()));

			logger.trace("{} points in the viewport", this.visibleLocations.size());

			points.forEach(this::remove);
			points.clear();

			for (var location : visibleLocations) {
				var point = new JLabel("✜");
				point.setForeground(Color.BLACK);
				point.setSize(point.getFont().getSize(), point.getFont().getSize());
				point.setFont(getFont().deriveFont(Map.of(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD)));
				point.setLocation((int) (location.x() - viewportX), (int) (location.y() - viewportY));
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
		x.set(0);
		y.set(0);
		refresh();
	}

	@EventListener
	public void onLocationsLoadedEvent(LocationsLoadedEvent event) {
		this.allLocations.clear();
		this.allLocations.addAll(event.locations());
		refresh();
	}

	@EventListener
	public void onUIResizedEvent(UIResizedEvent ignored) {
		refresh();
	}

}
