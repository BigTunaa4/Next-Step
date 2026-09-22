package com.nextstep;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

/**
 * Shows the way to a destination: world map marker, hint arrow when close,
 * and (if installed) a walking route from the Shortest Path plugin.
 * All methods must be called on the client thread.
 */
@Singleton
class Navigator
{
	/** Dungeons/instances live at y >= 6400; surface directions don't apply there. */
	private static final int UNDERGROUND_Y = 6400;
	private static final int HINT_ARROW_RANGE = 40;
	private static final String[] COMPASS_LONG = {
		"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"
	};
	private static final String[] COMPASS_SHORT = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

	private final Client client;
	private final WorldMapPointManager worldMapPointManager;
	private final EventBus eventBus;
	private final NextStepConfig config;
	private final BufferedImage markerImage = buildMarker();

	private WorldMapPoint mapPoint;
	private WorldPoint target;
	private boolean pathRequested;

	@Inject
	Navigator(Client client, WorldMapPointManager worldMapPointManager, EventBus eventBus, NextStepConfig config)
	{
		this.client = client;
		this.worldMapPointManager = worldMapPointManager;
		this.eventBus = eventBus;
		this.config = config;
	}

	void navigateTo(Suggestion s)
	{
		clear();
		target = s.getLocation();
		if (target == null)
		{
			return;
		}

		if (config.mapMarker())
		{
			mapPoint = new WorldMapPoint(target, markerImage);
			mapPoint.setTooltip("Next Step: " + s.getName() + " - " + s.getLocationName());
			mapPoint.setJumpOnClick(true);
			mapPoint.setSnapToEdge(true);
			worldMapPointManager.add(mapPoint);
		}

		if (config.shortestPath())
		{
			// Ignored harmlessly if the Shortest Path plugin isn't installed.
			Map<String, Object> data = new HashMap<>();
			data.put("target", target);
			eventBus.post(new PluginMessage("shortestpath", "path", data));
			pathRequested = true;
		}
	}

	/** Call every tick while navigating: keeps the hint arrow up when close. */
	void tick(WorldPoint player)
	{
		if (target == null || player == null || !config.hintArrow())
		{
			return;
		}
		int dist = distance(player, target);
		if (dist >= 0 && dist <= HINT_ARROW_RANGE && !client.hasHintArrow())
		{
			client.setHintArrow(target);
		}
	}

	void clear()
	{
		if (mapPoint != null)
		{
			worldMapPointManager.remove(mapPoint);
			mapPoint = null;
		}
		if (target != null && client.hasHintArrow() && target.equals(client.getHintArrowPoint()))
		{
			client.clearHintArrow();
		}
		if (pathRequested)
		{
			eventBus.post(new PluginMessage("shortestpath", "clear", new HashMap<>()));
			pathRequested = false;
		}
		target = null;
	}

	WorldPoint getTarget()
	{
		return target;
	}

	// ---------------- Direction helpers ----------------

	/** Tile distance on the surface, or -1 if either point is underground. */
	static int distance(WorldPoint from, WorldPoint to)
	{
		if (from == null || to == null || from.getY() >= UNDERGROUND_Y || to.getY() >= UNDERGROUND_Y)
		{
			return -1;
		}
		return Math.max(Math.abs(to.getX() - from.getX()), Math.abs(to.getY() - from.getY()));
	}

	static String compassShort(WorldPoint from, WorldPoint to)
	{
		return COMPASS_SHORT[compassIndex(from, to)];
	}

	/** Friendly direction text, e.g. "Head north-west (~340 tiles)". */
	static String describe(WorldPoint from, WorldPoint to)
	{
		if (from == null || to == null)
		{
			return null;
		}
		if (from.getY() >= UNDERGROUND_Y)
		{
			return "You're underground; head to the surface";
		}
		int dist = distance(from, to);
		if (dist < 0)
		{
			return null;
		}
		if (dist <= 8)
		{
			if (to.getPlane() > from.getPlane())
			{
				return "Right here! Go upstairs";
			}
			if (to.getPlane() < from.getPlane())
			{
				return "Right here! Go downstairs";
			}
			return "You're here!";
		}
		return "Head " + COMPASS_LONG[compassIndex(from, to)] + " (~" + dist + " tiles)";
	}

	private static int compassIndex(WorldPoint from, WorldPoint to)
	{
		int dx = to.getX() - from.getX();
		int dy = to.getY() - from.getY(); // north is +y
		double angle = Math.toDegrees(Math.atan2(dx, dy)); // 0 = north, 90 = east
		double normalized = ((angle % 360) + 360) % 360;
		return (int) Math.round(normalized / 45.0) % 8;
	}

	private static BufferedImage buildMarker()
	{
		BufferedImage img = new BufferedImage(20, 26, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Path2D pin = new Path2D.Double();
		pin.moveTo(10, 25);
		pin.curveTo(3, 15, 1, 12, 1, 9);
		pin.curveTo(1, 4, 5, 1, 10, 1);
		pin.curveTo(15, 1, 19, 4, 19, 9);
		pin.curveTo(19, 12, 17, 15, 10, 25);
		pin.closePath();
		g.setColor(new Color(255, 200, 60));
		g.fill(pin);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1.5f));
		g.draw(pin);
		g.setColor(new Color(40, 30, 10));
		g.fill(new Ellipse2D.Double(6, 5, 8, 8));
		g.dispose();
		return img;
	}
}
