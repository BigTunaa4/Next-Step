package com.nextstep;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/** Animated celebration popup with spinning rays and confetti. Popups queue up and play one at a time. */
public class CelebrationOverlay extends Overlay
{
	private static final int BOX_W = 300;
	private static final int BOX_H = 112;
	private static final double INTRO_SECONDS = 0.35;
	private static final double OUTRO_SECONDS = 0.5;
	private static final double GRAVITY = 620;
	private static final int CONFETTI_COUNT = 110;
	private static final Color[] CONFETTI_COLORS = {
		new Color(255, 200, 60), new Color(90, 220, 110), new Color(100, 180, 255),
		new Color(255, 110, 170), new Color(190, 120, 255), new Color(255, 150, 60)
	};

	private final Client client;
	private final NextStepConfig config;
	private final Queue<Celebration> queue = new ConcurrentLinkedQueue<>();
	private final List<Particle> particles = new ArrayList<>();
	private volatile boolean clearRequested;

	private Celebration current;
	private long startNanos;
	private long lastNanos;

	@Inject
	CelebrationOverlay(Client client, NextStepConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	void enqueue(Celebration celebration)
	{
		queue.add(celebration);
	}

	void clear()
	{
		queue.clear();
		clearRequested = true;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (clearRequested)
		{
			clearRequested = false;
			current = null;
			particles.clear();
		}

		long now = System.nanoTime();
		if (current == null)
		{
			current = queue.poll();
			if (current == null)
			{
				return null;
			}
			startNanos = now;
			lastNanos = now;
			particles.clear();
			if (config.confetti())
			{
				spawnConfetti();
			}
		}

		double t = (now - startNanos) / 1e9;
		double duration = config.popupSeconds();
		double dt = Math.min(0.05, (now - lastNanos) / 1e9);
		lastNanos = now;

		if (t >= duration)
		{
			current = null;
			particles.clear();
			return null;
		}

		int width = client.getCanvasWidth();
		int height = client.getCanvasHeight();
		int cx = width / 2;
		int cy = (int) (height * 0.3);
		float alpha = (float) clamp((duration - t) / OUTRO_SECONDS);
		double scale = Math.max(0.05, easeOutBack(clamp(t / INTRO_SECONDS)));

		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			drawRays(g, cx, cy, t, scale);
			drawBox(g, cx, cy, scale);
			updateAndDrawConfetti(g, dt, height);
		}
		finally
		{
			g.dispose();
		}
		return null;
	}

	private void drawRays(Graphics2D g, int cx, int cy, double t, double scale)
	{
		Color c = current.getColor();
		g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 45));
		double radius = 210 * scale;
		int rays = 12;
		double half = Math.PI / rays * 0.45;
		double spin = t * 0.6;
		for (int i = 0; i < rays; i++)
		{
			double a = spin + i * 2 * Math.PI / rays;
			Path2D ray = new Path2D.Double();
			ray.moveTo(cx, cy);
			ray.lineTo(cx + Math.cos(a - half) * radius, cy + Math.sin(a - half) * radius);
			ray.lineTo(cx + Math.cos(a + half) * radius, cy + Math.sin(a + half) * radius);
			ray.closePath();
			g.fill(ray);
		}
	}

	private void drawBox(Graphics2D g, int cx, int cy, double scale)
	{
		AffineTransform old = g.getTransform();
		g.translate(cx, cy);
		g.scale(scale, scale);

		int x = -BOX_W / 2;
		int y = -BOX_H / 2;
		Color c = current.getColor();

		RoundRectangle2D box = new RoundRectangle2D.Double(x, y, BOX_W, BOX_H, 18, 18);
		g.setPaint(new GradientPaint(0, y, new Color(52, 44, 32), 0, y + BOX_H, new Color(24, 20, 15)));
		g.fill(box);
		g.setStroke(new BasicStroke(3f));
		g.setColor(c);
		g.draw(box);
		g.setStroke(new BasicStroke(1f));
		g.setColor(new Color(255, 255, 255, 40));
		g.draw(new RoundRectangle2D.Double(x + 5, y + 5, BOX_W - 10, BOX_H - 10, 12, 12));

		drawCentered(g, current.getTitle(), FontManager.getRunescapeBoldFont().deriveFont(24f), c, y + 30);
		drawCentered(g, current.getSubtitle(), FontManager.getRunescapeBoldFont().deriveFont(16f), Color.WHITE, y + 54);
		drawCentered(g, current.getDetail(), FontManager.getRunescapeFont().deriveFont(15f), new Color(210, 210, 210), y + 76);
		drawCentered(g, current.getCheer(), FontManager.getRunescapeSmallFont().deriveFont(14f), c.brighter(), y + 98);

		g.setTransform(old);
	}

	private void drawCentered(Graphics2D g, String text, Font font, Color color, int baseline)
	{
		if (text == null || text.isEmpty())
		{
			return;
		}
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		String fitted = fit(text, fm, BOX_W - 24);
		int x = -fm.stringWidth(fitted) / 2;
		g.setColor(Color.BLACK);
		g.drawString(fitted, x + 1, baseline + 1);
		g.setColor(color);
		g.drawString(fitted, x, baseline);
	}

	private static String fit(String text, FontMetrics fm, int maxWidth)
	{
		if (fm.stringWidth(text) <= maxWidth)
		{
			return text;
		}
		String s = text;
		while (s.length() > 1 && fm.stringWidth(s + "...") > maxWidth)
		{
			s = s.substring(0, s.length() - 1);
		}
		return s + "...";
	}

	private void spawnConfetti()
	{
		ThreadLocalRandom r = ThreadLocalRandom.current();
		double cx = client.getCanvasWidth() / 2.0;
		double cy = client.getCanvasHeight() * 0.3;
		for (int i = 0; i < CONFETTI_COUNT; i++)
		{
			Particle p = new Particle();
			double angle = r.nextDouble(-Math.PI, 0); // upward half
			double speed = r.nextDouble(200, 560);
			p.x = cx + r.nextDouble(-30, 30);
			p.y = cy + r.nextDouble(-10, 10);
			p.vx = Math.cos(angle) * speed;
			p.vy = Math.sin(angle) * speed;
			p.rot = r.nextDouble(0, Math.PI * 2);
			p.vrot = r.nextDouble(-8, 8);
			p.size = r.nextDouble(4, 9);
			p.color = CONFETTI_COLORS[r.nextInt(CONFETTI_COLORS.length)];
			particles.add(p);
		}
	}

	private void updateAndDrawConfetti(Graphics2D g, double dt, int canvasHeight)
	{
		Iterator<Particle> it = particles.iterator();
		while (it.hasNext())
		{
			Particle p = it.next();
			p.vy += GRAVITY * dt;
			p.vx *= (1 - 0.8 * dt);
			p.x += p.vx * dt;
			p.y += p.vy * dt;
			p.rot += p.vrot * dt;
			if (p.y > canvasHeight + 20)
			{
				it.remove();
				continue;
			}
			AffineTransform old = g.getTransform();
			g.translate(p.x, p.y);
			g.rotate(p.rot);
			g.setColor(p.color);
			int w = (int) Math.round(p.size);
			int h = Math.max(2, w / 2);
			g.fillRect(-w / 2, -h / 2, w, h);
			g.setTransform(old);
		}
	}

	private static double clamp(double v)
	{
		return Math.max(0, Math.min(1, v));
	}

	/** Overshoots slightly then settles: gives the popup a "pop". */
	private static double easeOutBack(double x)
	{
		double c1 = 1.70158;
		double c3 = c1 + 1;
		return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
	}

	private static final class Particle
	{
		double x;
		double y;
		double vx;
		double vy;
		double rot;
		double vrot;
		double size;
		Color color;
	}
}
