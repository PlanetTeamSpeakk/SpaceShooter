package com.ptsmods.spaceshooter.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.xml.bind.DatatypeConverter;

import com.sun.management.OperatingSystemMXBean;

public class Miscellaneous {

	private static final ThreadPoolExecutor miscellaneousExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	public static BufferedInputStream getResourceAsStream(String name) {
		return new BufferedInputStream(Miscellaneous.class.getResourceAsStream(name) == null ? Miscellaneous.class.getClassLoader().getResourceAsStream(name) : Miscellaneous.class.getResourceAsStream(name));
	}

	public static void runAsynchronously(Runnable runnable) {
		miscellaneousExecutor.execute(() -> {
			try {
				runnable.run();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		});
	}

	public static void sleep(long millis) {
		sleep(millis, TimeUnit.MILLISECONDS);
	}

	public static void sleep(long units, TimeUnit unit) {
		long start = System.currentTimeMillis();
		try {
			unit.sleep(units);
		} catch (InterruptedException e) {
			// TimeUnit.sleep requires way less CPU, but this is only if the Thread was
			// interrupted.
			long stop = System.currentTimeMillis() - (start + unit.toMillis(units) - System.currentTimeMillis());
			while (System.currentTimeMillis() < stop);
		}
	}

	public static Integer[] range(int range) {
		Integer[] array = new Integer[range];
		for (int x = 0; x < array.length; x++)
			array[x] = x;
		return array;
	}

	public static byte[] createChecksum(InputStream stream) throws Exception {
		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		while ((numRead = stream.read(buffer)) > 0)
			complete.update(buffer, 0, numRead);
		return complete.digest();
	}

	public static String getMD5Checksum(InputStream stream) throws Exception {
		byte[] b = createChecksum(stream);
		String result = "";
		for (byte element : b)
			result += Integer.toString((element & 0xff) + 0x100, 16).substring(1);
		return result;
	}

	public static <K, V> HashMap<K, V> newHashMap(K[] keys, V[] values) {
		HashMap<K, V> map = new HashMap<>();
		for (int x = 0; x < keys.length && x < values.length; x++)
			map.put(keys[x], values[x]);
		return map;
	}

	public static <E> ArrayList<E> newArrayList(E... elements) {
		ArrayList<E> list = new ArrayList();
		for (E element : elements)
			list.add(element);
		return list;
	}

	public static String getFormattedTime() {
		return (LocalDateTime.now().getHour() < 10 ? "0" : "") + LocalDateTime.now().getHour() + "H" + (LocalDateTime.now().getMinute() < 10 ? "0" : "") + LocalDateTime.now().getMinute() + "M" + (LocalDateTime.now().getSecond() < 10 ? "0" : "") + LocalDateTime.now().getSecond() + "S";
	}

	public static String getFormattedDate() {
		return joinCustomChar("-", LocalDateTime.now().getDayOfMonth(), LocalDateTime.now().getMonthValue(), LocalDateTime.now().getYear());
	}

	public static String joinCustomChar(String character, Object... array) {
		String data = "";
		for (int x = 0; x < array.length; x++)
			data += array[x] + (x + 1 == array.length ? "" : character);
		return data.trim();
	}

	public static Image rotate(Image img, double angle) {
		double sin = Math.abs(Math.sin(Math.toRadians(angle))), cos = Math.abs(Math.cos(Math.toRadians(angle)));
		int w = img.getWidth(null), h = img.getHeight(null);
		int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
		BufferedImage bimg = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bimg.createGraphics();
		g.translate((neww - w) / 2, (newh - h) / 2);
		g.rotate(Math.toRadians(angle), w / 2, h / 2);
		g.drawRenderedImage(toBufferedImage(img), null);
		g.dispose();
		return bimg;
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage)
			return (BufferedImage) img;
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		img.flush();
		return bimage;
	}

	public static float getProcessCpuLoad() {
		return (float) ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad() * 100;
	}

	@SuppressWarnings("resource")
	public static String convertStreamToString(InputStream is) {
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static float getVolume(Clip clip) {
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		return (float) Math.pow(10f, gainControl.getValue() / 20f);
	}

	public static void setVolume(Clip clip, float volume) {
		if (volume < 0f || volume > 1f)
			throw new IllegalArgumentException("Volume not valid: " + volume);
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(20f * (float) Math.log10(volume));
	}

	public static BufferedInputStream decryptInputStream(InputStream encryptedIS) {
		return new BufferedInputStream(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(Krypto.decrypt("IuJFKOizCX9MJTHO", convertStreamToString(encryptedIS)))));
	}

}
