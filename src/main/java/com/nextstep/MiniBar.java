package com.nextstep;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import net.runelite.client.ui.FontManager;

/** Small custom-painted progress bar (avoids look-and-feel quirks of JProgressBar). */
class MiniBar extends JComponent
{
	private static final int HEIGHT = 14;

	private final double fraction;
	private final Color color;
	private final String text;

	MiniBar(double fraction, Color color, String text)
	{
		this.fraction = Math.max(0, Math.min(1, fraction));
		this.color = color;
		this.text = text;
		setPreferredSize(new Dimension(0, HEIGHT));
		setMinimumSize(new Dimension(0, HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth();
			int h = getHeight();
			g.setColor(new Color(30, 30, 30));
			g.fillRoundRect(0, 0, w, h, 6, 6);
			g.setColor(color.darker());
			g.fillRoundRect(0, 0, (int) Math.round(w * fraction), h, 6, 6);

			if (text != null)
			{
				g.setFont(FontManager.getRunescapeSmallFont());
				FontMetrics fm = g.getFontMetrics();
				int tx = (w - fm.stringWidth(text)) / 2;
				int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
				g.setColor(Color.BLACK);
				g.drawString(text, tx + 1, ty + 1);
				g.setColor(Color.WHITE);
				g.drawString(text, tx, ty);
			}
		}
		finally
		{
			g.dispose();
		}
	}
}
