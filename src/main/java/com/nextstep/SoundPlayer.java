package com.nextstep;

import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plays a short synthesized jingle for celebrations. The sound is generated in code
 * (no audio files) and played off the client thread. Bigger tiers get longer jingles.
 */
@Singleton
class SoundPlayer
{
	private static final Logger log = LoggerFactory.getLogger(SoundPlayer.class);
	private static final float SAMPLE_RATE = 44100f;

	private final ScheduledExecutorService executor;
	private final NextStepConfig config;

	@Inject
	SoundPlayer(ScheduledExecutorService executor, NextStepConfig config)
	{
		this.executor = executor;
		this.config = config;
	}

	void play(Celebration.Tier tier)
	{
		if (!config.sounds())
		{
			return;
		}
		float volume = config.soundVolume() / 100f;
		if (volume <= 0)
		{
			return;
		}
		executor.submit(() -> playNow(tier, volume));
	}

	private static void playNow(Celebration.Tier tier, float volume)
	{
		double[] notes;
		double noteSeconds;
		switch (tier)
		{
			case MEGA:
				notes = new double[]{523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98, 2093.00};
				noteSeconds = 0.10;
				break;
			case RARE:
				notes = new double[]{523.25, 659.25, 783.99, 1046.50, 1318.51};
				noteSeconds = 0.11;
				break;
			default:
				notes = new double[]{659.25, 783.99, 1046.50};
				noteSeconds = 0.12;
		}

		int step = (int) (SAMPLE_RATE * noteSeconds);
		int ring = (int) (SAMPLE_RATE * 0.5);
		int total = step * (notes.length - 1) + ring;
		double[] mix = new double[total];
		for (int n = 0; n < notes.length; n++)
		{
			int start = n * step;
			for (int s = 0; s < ring && start + s < total; s++)
			{
				double t = s / SAMPLE_RATE;
				double env = Math.exp(-t / 0.13);
				double f = notes[n];
				mix[start + s] += (Math.sin(2 * Math.PI * f * t) * 0.6 + Math.sin(4 * Math.PI * f * t) * 0.15) * env;
			}
		}

		byte[] buf = new byte[total * 2];
		for (int i = 0; i < total; i++)
		{
			double v = Math.max(-1, Math.min(1, mix[i] * 0.35 * volume));
			short sample = (short) (v * Short.MAX_VALUE);
			buf[i * 2] = (byte) (sample & 0xff);
			buf[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
		}

		AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
		try (SourceDataLine line = AudioSystem.getSourceDataLine(format))
		{
			line.open(format);
			line.start();
			line.write(buf, 0, buf.length);
			line.drain();
		}
		catch (Exception e)
		{
			log.debug("Next Step: could not play celebration sound", e);
		}
	}
}
