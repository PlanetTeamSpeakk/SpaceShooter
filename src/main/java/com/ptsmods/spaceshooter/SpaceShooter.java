package com.ptsmods.spaceshooter;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import com.ptsmods.spaceshooter.utils.Miscellaneous;
import com.ptsmods.spaceshooter.utils.Random;
import com.ptsmods.spaceshooter.utils.Vec2i;

/**
 * @author PlanetTeamSpeak with ideas by Leroy Mourits
 */
public class SpaceShooter extends JPanel {

	private static volatile Font				eightBit;
	private static volatile Map<String, String>	md5hashes				= Miscellaneous.newHashMap(new String[] {"sounds/explosion.wav", "sounds/gameOver.wav", "sounds/laser.wav", "sounds/powerup.wav", "sounds/powerupEnded.wav", "sounds/rocket.wav", "sounds/shot.wav"}, new String[] {"08B49108A99F4FE4F298CCFEF4BAB4C8", "6a450b2fdad62bb13419323a7845fcdc", "B2FDB5F847D60DADD5D32826AD60CB14", "0CF0BC3444E81DD1A724BC06D47D40B0", "122EAF2052D661D91CBFF76A191BF581", "C0BC49BEFFBD596AD564CBB48ADA8011", "BA76EC724D5B441E8106A48227CBE6F8"});
	private static final long					serialVersionUID		= 5306268652751327522L;
	private static volatile Image				player;
	private static volatile Image				playerBullet;
	private static volatile Image				enemy;
	private static volatile Image				enemyBullet;
	private static volatile Image				heart;
	private static volatile Image				emptyHeart;
	private static volatile Image				overlay;
	private static volatile Image				boss;
	private static volatile Image				rocket;
	private static volatile Image				bigBoss;
	private static volatile Image				cherry;
	private static volatile Image				battery;
	private static volatile Image				laser;
	private static volatile JFrame				frame;
	private static volatile Image				background;
	private volatile Vec2i						playerLoc				= null;
	private volatile Vec2i						minPlayerLoc			= null;
	private volatile Vec2i						maxPlayerLoc			= null;
	private volatile Vec2i						restartLoc				= new Vec2i(0, 0);
	private volatile Boss						currentBoss				= null;
	private volatile BigBoss					currentBigBoss			= null;
	private volatile boolean					isInitialized			= false;
	private volatile boolean					dead					= true;
	private volatile boolean					newSession				= true;
	private volatile boolean					paused					= false;
	private volatile boolean					holdsCtrl				= false;
	private volatile boolean					playedPowerupSound		= true;
	private volatile long						lastSs					= 0;
	private volatile long						lastShot				= 0;
	private volatile long						backgroundLastGenerated	= 0;
	private volatile long						fpsLastChecked			= 0;
	private volatile long						cherryEatenAt			= 0;
	private volatile long						batteryEatenAt			= 0;
	private volatile int						holdsKey				= -1;
	private volatile int						restartButtonWidth		= -1;
	private volatile int						backgroundY				= -1440;
	private volatile int						defaultBackgroundY		= -1440;
	private volatile int						health					= 3;
	private volatile int						points					= 0;
	private volatile int						highscore				= 0;
	private volatile static int					targetFps				= 256;
	private volatile int						fps						= 0;
	private volatile int						framesPassed			= 0;
	private volatile int						volume					= 100;
	private static volatile List<Image>			explosionFrames			= new ArrayList();
	private volatile List<PlayerBullet>			playerBullets			= new ArrayList();
	private volatile List<EnemyBullet>			enemyBullets			= new ArrayList();
	private volatile List<Explosion>			explosions				= new ArrayList();
	private volatile List<Enemy>				enemies					= new ArrayList();
	private volatile List<HardEnemy>			hardEnemies				= new ArrayList();
	private volatile Cherry						currentCherry			= null;
	private volatile Heart						currentHeart			= null;
	private volatile Battery					currentBattery			= null;
	private volatile Clip						backgroundSong			= null;
	private final Laser							laserObj				= new Laser();
	private static final Cursor					blankCursor				= Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");
	private volatile Robot						robot					= null;
	//@formatter:off
	public final KeyListener keyListener = new KeyListener() {
		@Override public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			holdsKey = e.getKeyCode();
			holdsCtrl = e.isControlDown();
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				paused = dead ? false : !paused;
				frame.setCursor(paused ? Cursor.getDefaultCursor() : blankCursor);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == holdsKey) holdsKey = -1;
		}
	};
	public final MouseInputListener mouseListener = new MouseInputListener() {

		@Override public void mouseClicked(MouseEvent arg0) {} @Override public void mouseEntered(MouseEvent arg0) {} @Override public void mouseExited(MouseEvent arg0) {} @Override public void mousePressed(MouseEvent arg0) {} @Override public void mouseDragged(MouseEvent arg0) {} @Override public void mouseMoved(MouseEvent event) {}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (doesMouseHoverOverRestartButton())
				if (dead) {
					points = 0;
					newSession = false;
					dead = false;
					health = 3;
					frame.setCursor(blankCursor);
					playSound(Miscellaneous.getResourceAsStream("sounds/click.wav"));
				} else if (paused) {
					paused = false;
					frame.setCursor(blankCursor);
					playSound(Miscellaneous.getResourceAsStream("sounds/click.wav"));
				}
		}

	};
	public final MouseWheelListener mouseWheelListener = event -> {volume = volume - event.getWheelRotation() * (event.isControlDown() ? 5 : 1) > 100 ? 100 : volume - event.getWheelRotation() * (event.isControlDown() ? 5 : 1) < 0 ? 0 : volume - event.getWheelRotation() * (event.isControlDown() ? 5 : 1); if (backgroundSong != null) Miscellaneous.setVolume(backgroundSong, volume / 100f);};//@formatter:on

	public SpaceShooter() {
		super();
		try {
			robot = new Robot();
		} catch (AWTException e1) {
			throw new RuntimeException(e1);
		}
		Miscellaneous.runAsynchronously(() -> {
			while (true)
				if (!dead && !paused)
					robot.mouseMove(getLocationOnScreen().x + getWidth() / 2, getLocationOnScreen().y + getHeight() / 2);
		});
		int i = 6;
		Miscellaneous.runAsynchronously(() -> {
			while (true) {
				switch (holdsKey) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_W: {
					if (!dead && !paused && playerLoc.getY() - i >= minPlayerLoc.getY())
						playerLoc.subtractY(i);
					break;
				}
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_S:
					if (holdsCtrl && holdsKey == KeyEvent.VK_S && System.currentTimeMillis() - lastSs > 1000) {
						BufferedImage screenshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
						paint(screenshot.createGraphics());
						try {
							ImageIO.write(screenshot, "png", new File(getCurrentSsName()));
						} catch (IOException e) {
							e.printStackTrace();
						}
						lastSs = System.currentTimeMillis();
					} else
						if (!dead && !paused && playerLoc.getY() + i <= maxPlayerLoc.getY())
							playerLoc.addY(i);
					break;
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_A: {
					if (!dead && !paused && playerLoc.getX() - i >= minPlayerLoc.getX())
						playerLoc.subtractX(i);
					break;
				}
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_D: {
					if (!dead && !paused && playerLoc.getX() + i <= maxPlayerLoc.getX())
						playerLoc.addX(i);
					break;
				}
				case KeyEvent.VK_SPACE: {
					if (!dead && !paused)
						shoot();
					break;
				}
				default:
					break;
				}
				backgroundY = backgroundY == 0 ? defaultBackgroundY + 1 : backgroundY + 1;
				for (Explosion explosion : new ArrayList<>(explosions))
					if (!explosion.move())
						explosions.remove(explosion);
				if (!paused && !dead)
					move();
				Miscellaneous.sleep(1000 / 60);
			}
		});
		try {
			backgroundSong = loopSound(new URL("https://cdn.impulsebot.com/spaceshooter/backgroundSong.wav").openStream(), -1); // this is the main reason why that splash/loading screen was mandatory.
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		try {
			Miscellaneous.runAsynchronously(() -> {
				while (true) {
					for (String file : md5hashes.keySet())
						try {
							if (!md5hashes.get(file).equalsIgnoreCase(Miscellaneous.getMD5Checksum(Miscellaneous.getResourceAsStream(file)))) {
								System.out.println("The file " + file + " in this JAR has been modified, please revert the changes in order to launch this application. (Original MD5: " + md5hashes.get(file) + ", current MD5: " + Miscellaneous.getMD5Checksum(Miscellaneous.getResourceAsStream(file)) + ")");
								System.exit(1);
							}
						} catch (Exception e) {}
					Miscellaneous.sleep(10, TimeUnit.SECONDS);
				}
			});
			player = ImageIO.read(Miscellaneous.getResourceAsStream("images/player.png"));
			frame = new JFrame();
			frame.setIconImage(player);
			frame.setTitle("Java SpaceShooter");
			frame.setBounds((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 1080 / 2, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 720 / 2, 1080, 720);
			frame.setResizable(false);
			frame.setAutoRequestFocus(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			Image background = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/background.jpg")));
			eightBit = Font.createFont(Font.TRUETYPE_FONT, Miscellaneous.getResourceAsStream("others/eightBit.ttf")).deriveFont(24F);
			JPanel splashScreen = new JPanel() {

				private static final long serialVersionUID = 4729782890469021540L;

				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.drawImage(background, 0, 0, 1080, 720, 0, 0, background.getWidth(null), background.getHeight(null), null);
					g.setFont(eightBit);
					g.setColor(Color.WHITE);
					g.drawString("Loading...", 1080 / 2 - g.getFontMetrics().stringWidth("Loading...") / 2, 720 / 2 - 24 / 2);
				}

			};
			frame.getContentPane().add(splashScreen);
			frame.setVisible(true);
			initializeImages();
			SpaceShooter spaceShooter = new SpaceShooter();
			frame.addWindowFocusListener(new WindowFocusListener() {
				@Override
				public void windowGainedFocus(WindowEvent arg0) {}

				@Override
				public void windowLostFocus(WindowEvent arg0) {
					if (!spaceShooter.dead) {
						spaceShooter.paused = true;
						frame.setCursor(Cursor.getDefaultCursor());
					}
				}

			});
			frame.addKeyListener(spaceShooter.keyListener);
			spaceShooter.addMouseListener(spaceShooter.mouseListener);
			spaceShooter.addMouseWheelListener(spaceShooter.mouseWheelListener);
			frame.getContentPane().remove(splashScreen);
			frame.getContentPane().add(spaceShooter);
			frame.setVisible(true);
			Miscellaneous.runAsynchronously(() -> {
				while (true) {
					spaceShooter.repaint();
					Miscellaneous.sleep(1000 / targetFps);
				}
			});
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static String getCurrentSsName() {
		return "SpaceShooter_screenshot_" + Miscellaneous.getFormattedDate() + "_" + Miscellaneous.getFormattedTime() + ".png";
	}

	private static final void initializeImages() {
		try {
			playerBullet = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/playerBullet.png")));
			enemy = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/enemy.png")));
			enemyBullet = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/enemyBullet.png")));
			heart = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/heart.png")));
			emptyHeart = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/emptyHeart.png")));
			for (int i : Miscellaneous.range(17))
				explosionFrames.add(ImageIO.read(Miscellaneous.getResourceAsStream("images/explosion/frame_" + i + ".png")));
			overlay = new BufferedImage(1080, 720, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) overlay.getGraphics();
			g.setColor(new Color(0f, 0f, 0f, 0.5f));
			g.fillRect(0, 0, 1080, 720);
			BufferedImage tempBoss = new BufferedImage(180, 160, BufferedImage.TYPE_INT_ARGB);
			tempBoss.createGraphics().drawImage(ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/boss.png"))), 0, 0, 180, 160, 0, 0, 183, 162, null);
			boss = tempBoss;
			rocket = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/rocket.png")));
			bigBoss = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/bigBoss.png")));
			cherry = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/cherry.png")));
			battery = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/battery.png")));
			laser = new BufferedImage(16, 800, BufferedImage.TYPE_INT_ARGB);
			Image tempLaser = ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/laser.png")));
			for (int i : Miscellaneous.range(800 / 32))
				laser.getGraphics().drawImage(tempLaser, 0, i * 32 - 32, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (!isInitialized)
			initialize();
		super.paintComponent(g);
		g.drawImage(background, 0, backgroundY, null);
		if (!dead) {
			for (Enemy enemy : new ArrayList<>(enemies)) {
				if (enemy != null)
					enemy.paint(g);
				if (enemy.location.getY() > getHeight())
					enemies.remove(enemy);
			}
			for (HardEnemy enemy : new ArrayList<>(hardEnemies)) {
				if (enemy != null)
					enemy.paint(g);
				if (enemy.location.getY() > getHeight())
					hardEnemies.remove(enemy);
			}
			if (currentBoss != null)
				currentBoss.paint(g);
			if (currentBigBoss != null)
				currentBigBoss.paint(g);
			for (Explosion explosion : new ArrayList<>(explosions))
				explosion.paint(g);
			for (EnemyBullet bullet : new ArrayList<>(enemyBullets))
				if (bullet.location.getY() < getHeight())
					bullet.paint(g);
				else
					enemyBullets.remove(bullet);
			for (PlayerBullet bullet : new ArrayList<>(playerBullets))
				if (bullet.location.getY() > 0)
					bullet.paint(g);
				else
					playerBullets.remove(bullet);
			if (currentCherry != null)
				currentCherry.paint(g);
			if (currentHeart != null)
				currentHeart.paint(g);
			if (currentBattery != null)
				currentBattery.paint(g);
			if (System.currentTimeMillis() - batteryEatenAt < 4000)
				laserObj.paint(g);
			if (System.currentTimeMillis() - cherryEatenAt < 5000)
				((Graphics2D) g).setComposite(AlphaComposite.SrcOver.derive(0.5f));
			drawImage(g, player, playerLoc.getX(), playerLoc.getY());
			((Graphics2D) g).setComposite(AlphaComposite.SrcOver.derive(1f));
			g.setFont(eightBit);
			g.setColor(Color.WHITE);
			g.drawString("HIGHSCORE: " + highscore, 20, getHeight() - eightBit.getSize() * 2 - 20);
			g.drawString(points + " POINT" + (points == 1 ? "" : "S"), 20, getHeight() - eightBit.getSize() - 10);
			g.drawImage(health >= 1 ? heart : emptyHeart, getWidth() - 110, getHeight() - 50, null);
			g.drawImage(health >= 2 ? heart : emptyHeart, getWidth() - 80, getHeight() - 50, null);
			g.drawImage(health >= 3 ? heart : emptyHeart, getWidth() - 50, getHeight() - 50, null);
			if (paused) {
				g.drawImage(overlay, 0, 0, null);
				g.setFont(eightBit.deriveFont(48F));
				g.setColor(Color.WHITE);
				g.drawString("Paused", getWidth() / 2 - g.getFontMetrics().stringWidth("Paused") / 2, 200);
				g.setFont(eightBit);
				restartButtonWidth = g.getFontMetrics().stringWidth("CONTINUE") + 16;
				restartLoc.set(getWidth() / 2 - restartButtonWidth / 2 - 2, getHeight() / 2 - 22);
				g.fillRect(restartLoc.getX(), restartLoc.getY(), restartButtonWidth + 4, 44);
				g.setColor(doesMouseHoverOverRestartButton() ? Color.DARK_GRAY : Color.BLACK);
				g.fillRect(restartLoc.getX() + 2, restartLoc.getY() + 2, restartButtonWidth, 40);
				g.setColor(Color.WHITE);
				g.drawString("CONTINUE", getWidth() / 2 - g.getFontMetrics().stringWidth("CONTINUE") / 2, getHeight() / 2 + 12);
			}
		} else {
			for (Explosion explosion : new ArrayList<>(explosions))
				explosion.paint(g);
			g.setFont(eightBit.deriveFont(48F));
			g.setColor(Color.WHITE);
			g.drawString(newSession ? "Welcome" : "Game Over", getWidth() / 2 - g.getFontMetrics().stringWidth(newSession ? "Welcome" : "Game Over") / 2, 200);
			g.setFont(eightBit);
			if (!newSession) {
				String score = "POINTS: " + points + " HIGHSCORE: " + highscore;
				g.drawString(score, getWidth() / 2 - g.getFontMetrics().stringWidth(score) / 2, 275);
			}
			restartButtonWidth = g.getFontMetrics().stringWidth(newSession ? "START" : "RESTART") + 16;
			restartLoc.set(getWidth() / 2 - restartButtonWidth / 2 - 2, getHeight() / 2 - 22);
			g.fillRect(restartLoc.getX(), restartLoc.getY(), restartButtonWidth + 4, 44);
			g.setColor(doesMouseHoverOverRestartButton() ? Color.DARK_GRAY : Color.BLACK);
			g.fillRect(restartLoc.getX() + 2, restartLoc.getY() + 2, restartButtonWidth, 40);
			g.setColor(Color.WHITE);
			g.drawString(newSession ? "START" : "RESTART", getWidth() / 2 - g.getFontMetrics().stringWidth(newSession ? "START" : "RESTART") / 2, getHeight() / 2 + 12);
		}
		g.setFont(eightBit.deriveFont(8f));
		g.drawString("FPS: " + fps, 10, 18);
		if (System.currentTimeMillis() - fpsLastChecked >= 1000) {
			fps = framesPassed;
			fpsLastChecked = System.currentTimeMillis();
			framesPassed = 0;
		}
		framesPassed += 1;
	}

	private void move() {
		if (Random.randInt(150 - points * 2 < 60 ? 60 : 150 - points * 2) == 0 && !paused && currentBoss == null && currentBigBoss == null)
			if (points < 45)
				enemies.add(new Enemy(new Vec2i(Random.randInt(getWidth() - 100), -80)));
			else
				hardEnemies.add(new HardEnemy(new Vec2i(Random.randInt(getWidth() - 110), -120)));
		if (currentBoss != null) {
			currentBoss.move();
			if (currentBoss != null && new Rectangle(currentBoss.location.getX(), currentBoss.location.getY(), 180, 160).intersects(new Rectangle(playerLoc.getX(), playerLoc.getY(), 100, 80)))
				playerDied();
		}
		if (currentBigBoss != null) {
			currentBigBoss.move();
			if (currentBigBoss != null && new Rectangle(currentBigBoss.location.getX(), currentBigBoss.location.getY(), 200, 200).intersects(new Rectangle(playerLoc.getX(), playerLoc.getY(), 100, 80)))
				playerDied();
		}
		for (Enemy enemy : new ArrayList<>(enemies)) {
			enemy.move();
			Rectangle e = new Rectangle(enemy.location.getX(), enemy.location.getY(), 100, 80);
			Rectangle p = new Rectangle(playerLoc.getX(), playerLoc.getY(), 100, 80);
			if (e.intersects(p)) {
				playerDied();
				break;
			}
		}
		for (HardEnemy enemy : new ArrayList<>(hardEnemies)) {
			enemy.move();
			Rectangle e = new Rectangle(enemy.location.getX(), enemy.location.getY(), 100, 80);
			Rectangle p = new Rectangle(playerLoc.getX(), playerLoc.getY(), 100, 80);
			if (e.intersects(p)) {
				playerDied();
				break;
			}
		}
		for (EnemyBullet bullet : new ArrayList<>(enemyBullets))
			if (bullet.move())
				playerDied();
		for (PlayerBullet bullet : new ArrayList<>(playerBullets)) {
			Object died = bullet.move();
			if (died != null) {
				if (died instanceof Enemy) {
					((Enemy) died).kill();
					enemies.remove(died);
				} else
					if (died instanceof HardEnemy) {
						((HardEnemy) died).kill();
						((HardEnemy) died).health -= 1;
						if (((HardEnemy) died).health <= 0)
							hardEnemies.remove(died);
					}
				playerBullets.remove(bullet);
				points += 1;
				highscore = points > highscore ? points : highscore;
				if (points % 30 == 0)
					currentBigBoss = new BigBoss();
				else
					if (points % 15 == 0)
						currentBoss = new Boss();
			}
		}
		if (currentCherry != null && currentCherry.move()) {
			cherryEatenAt = System.currentTimeMillis();
			currentCherry = null;
			playedPowerupSound = false;
			playSound(Miscellaneous.getResourceAsStream("sounds/powerup.wav"));
		} else
			if (currentCherry == null && Random.randInt(6000) == 0)
				currentCherry = new Cherry(new Vec2i(Random.randInt(getWidth() - 32), -32));
		if (currentHeart != null && currentHeart.move()) {
			currentHeart = null;
			health += 1;
			playSound(Miscellaneous.getResourceAsStream("sounds/powerup.wav"));
		} else
			if (currentHeart == null && health < 3 && Random.randInt(4000) == 0)
				currentHeart = new Heart(new Vec2i(Random.randInt(getWidth() - 30), -30));
		if (currentBattery != null && currentBattery.move()) {
			currentBattery = null;
			batteryEatenAt = System.currentTimeMillis();
			playSound(Miscellaneous.getResourceAsStream("sounds/laser.wav"));
		} else
			if (currentBattery == null && Random.randInt(6000) == 0)
				currentBattery = new Battery(new Vec2i(Random.randInt(getWidth() - 16), -32));
		if (System.currentTimeMillis() - batteryEatenAt < 4000)
			laserObj.checkDeaths();
		if (currentCherry != null && currentCherry.location.getY() > getHeight())
			currentCherry = null;
		if (currentHeart != null && currentHeart.location.getY() > getHeight())
			currentHeart = null;
		if (currentBattery != null && currentBattery.location.getY() > getHeight())
			currentBattery = null;
		if (System.currentTimeMillis() - cherryEatenAt > 5000 && !playedPowerupSound) {
			playSound(Miscellaneous.getResourceAsStream("sounds/powerupEnded.wav"));
			playedPowerupSound = true;
		}
	}

	private void generateBackground() throws IOException {
		if (System.currentTimeMillis() - backgroundLastGenerated > 250) {
			int width = getWidth();
			int height = getHeight();
			BufferedImage backgroundTemp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			backgroundTemp.createGraphics().drawImage(ImageIO.read(Miscellaneous.decryptInputStream(Miscellaneous.getResourceAsStream("images/background.jpg"))), 0, 0, width, height, 0, 0, 1920, 1080, null);
			BufferedImage image = new BufferedImage(width, height * 3, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(backgroundTemp, 0, height, null);
			AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -backgroundTemp.getHeight());
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			backgroundTemp = op.filter(backgroundTemp, null);
			g.drawImage(backgroundTemp, 0, 0, null);
			g.drawImage(backgroundTemp, 0, height * 2, null);
			background = image;
			backgroundY = -height * 2;
			defaultBackgroundY = backgroundY;
			backgroundLastGenerated = System.currentTimeMillis();
		}
	}

	private void drawImage(Graphics g, Image image, int x, int y) {
		g.drawImage(image, x, y, x + image.getWidth(null), y + image.getHeight(null), 0, 0, image.getWidth(null), image.getHeight(null), null);
	}

	private void drawHealthBar(Graphics g, int health, int maxHealth, int x, int y) {
		drawHealthBar(g, health, maxHealth, x, y, 1f);
	}

	private void drawHealthBar(Graphics g, int health, int maxHealth, int x, int y, float sizeMultiplier) {
		Color originalColor = new Color(g.getColor().getRGB());
		g.setColor(Color.WHITE);
		g.fillRect(x, y, (int) (100 * sizeMultiplier), (int) (10 * sizeMultiplier));
		int percentLeft = (int) ((float) health / (float) maxHealth * 100);
		g.setColor(new Color(255 - 255 / 100 * percentLeft, 255 / 100 * percentLeft, 0)); // green health bar at 100% hp, red at 5%.
		g.fillRect(x, y, percentLeft, (int) (10 * sizeMultiplier));
		g.setColor(originalColor);
	}

	private void initialize() {
		minPlayerLoc = new Vec2i(0, 80);
		maxPlayerLoc = new Vec2i(getWidth() - player.getWidth(null), getHeight() - 180);
		playerLoc = new Vec2i(getWidth() / 2 - player.getWidth(null) / 2, maxPlayerLoc.getY());
		if (background == null)
			try {
				generateBackground();
			} catch (IOException e) {
				e.printStackTrace();
			}
		isInitialized = true;
	}

	private void shoot() {
		if (System.currentTimeMillis() - lastShot > 250) {
			playerBullets.add(new PlayerBullet());
			lastShot = System.currentTimeMillis();
		}
	}

	private boolean doesMouseHoverOverRestartButton() {
		Vec2i loc = new Vec2i(MouseInfo.getPointerInfo().getLocation());
		Vec2i floc = new Vec2i(frame.getLocationOnScreen());
		return restartLoc == null ? false : loc.getX() >= floc.getX() + restartLoc.getX() && loc.getX() <= floc.getX() + restartLoc.getX() + restartButtonWidth + 7 && loc.getY() >= floc.getY() + restartLoc.getY() + 25 && loc.getY() <= floc.getY() + restartLoc.getY() + 69;
	}

	private void playerDied() {
		if (System.currentTimeMillis() - cherryEatenAt > 5000) {
			for (Enemy enemy : enemies)
				enemy.kill();
			for (HardEnemy enemy : hardEnemies)
				enemy.kill();
			health -= 1;
			playSound(Miscellaneous.getResourceAsStream("sounds/gameOver.wav")).setMicrosecondPosition(1000000);
			enemyBullets.clear();
			playerBullets.clear();
			enemies.clear();
			hardEnemies.clear();
			explosions.add(new Explosion(playerLoc));
			initialize();
			if (health == 0) {
				if (currentBoss != null)
					currentBoss.kill();
				else
					if (currentBigBoss != null)
						currentBigBoss.kill();
				currentBoss = null;
				currentBigBoss = null;
				dead = true;
				frame.setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	private Clip playSound(InputStream inputStream) {
		try {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(inputStream instanceof BufferedInputStream ? inputStream : new BufferedInputStream(inputStream)));
			Miscellaneous.setVolume(clip, volume / 100f);
			clip.start();
			return clip;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Clip loopSound(InputStream inputStream, int loop) {
		try {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(inputStream instanceof BufferedInputStream ? inputStream : new BufferedInputStream(inputStream)));
			Miscellaneous.setVolume(clip, volume / 100f);
			clip.loop(loop);
			return clip;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// ===BULLETS===

	private class PlayerBullet {

		private final Vec2i			location;
		private volatile boolean	shouldDraw	= true;

		private PlayerBullet() {
			location = new Vec2i(playerLoc.getX() + 43, playerLoc.getY());
			playSound(Miscellaneous.getResourceAsStream("sounds/shot.wav"));
		}

		private Object move() {
			if (!paused) {
				location.subtractY(5);
				if (shouldDraw) {
					for (Enemy enemy : enemies) {
						Rectangle loc = new Rectangle(location.getX(), location.getY(), 15, 15);
						Rectangle enemyLoc = new Rectangle(enemy.location.getX(), enemy.location.getY(), 100, 80);
						if (loc.intersects(enemyLoc)) {
							shouldDraw = false;
							return enemy;
						}
					}
					for (HardEnemy enemy : hardEnemies) {
						Rectangle loc = new Rectangle(location.getX(), location.getY(), 15, 15);
						Rectangle enemyLoc = new Rectangle(enemy.location.getX(), enemy.location.getY(), 100, 80);
						if (loc.intersects(enemyLoc)) {
							shouldDraw = false;
							return enemy;
						}
					}
					Rectangle loc = new Rectangle(location.getX(), location.getY(), 15, 15);
					if (currentBoss != null) {
						Rectangle bossLoc = new Rectangle(currentBoss.location.getX(), currentBoss.location.getY(), 180, 160);
						if (loc.intersects(bossLoc)) {
							currentBoss.health -= 1;
							explosions.add(new Explosion(currentBoss.location));
							shouldDraw = false;
							if (currentBoss.health == 0) {
								currentBoss.kill();
								currentBoss = null;
								points += 2;
								highscore = points > highscore ? points : highscore;
							}
						}
					}
					if (currentBigBoss != null) {
						Rectangle bbossLoc = new Rectangle(currentBigBoss.location.getX(), currentBigBoss.location.getY(), 200, 200);
						if (loc.intersects(bbossLoc)) {
							currentBigBoss.health -= 1;
							explosions.add(new Explosion(new Vec2i(currentBigBoss.location.getX() + 40, currentBigBoss.location.getY())));
							shouldDraw = false;
							if (currentBigBoss.health == 0) {
								currentBigBoss.kill();
								currentBigBoss = null;
								points += 2;
								highscore = points > highscore ? points : highscore;
							}
						}
					}
				}
			}
			return null;
		}

		private void paint(Graphics g) {
			if (shouldDraw)
				drawImage(g, playerBullet, location.getX(), location.getY());
		}
	}

	private class EnemyBullet {

		private final Vec2i			location;
		private volatile boolean	shouldDraw	= true;

		private EnemyBullet(Vec2i location) {
			this.location = location;
			playSound(Miscellaneous.getResourceAsStream("sounds/shot.wav"));
		}

		private boolean move() {
			if (!paused)
				location.addY(5);
			Rectangle loc = new Rectangle(location.getX(), location.getY(), 15, 15);
			Rectangle playerLocr = new Rectangle(playerLoc.getX(), playerLoc.getY(), 100, 80);
			if (loc.intersects(playerLocr)) {
				shouldDraw = false;
				return true;
			}
			return false;
		}

		private void paint(Graphics g) {
			if (shouldDraw)
				drawImage(g, enemyBullet, location.getX(), location.getY());
		}

	}

	private class Rocket {
		private final Vec2i	location;
		private final Vec2i	aimedAt	= new Vec2i(playerLoc.getX() + 50, playerLoc.getY() + 40);
		private final Image	rotatedRocket;
		private final int	xPerFrame;
		private final int	yPerFrame;

		private Rocket(Vec2i location) {
			this.location = location;
			double arc = Math.atan2(aimedAt.getY() - location.getY(), aimedAt.getX() - location.getX());
			double degrees = Math.toDegrees(arc) < 0 ? Math.toDegrees(arc) + 360 : Math.toDegrees(arc) > 360 ? Math.toDegrees(arc) - 360 : Math.toDegrees(arc);
			rotatedRocket = Miscellaneous.rotate(rocket, degrees);
			int framesToTravel = ((location.getY() - aimedAt.getY() < 0 ? -(location.getY() - aimedAt.getY()) : location.getY() - aimedAt.getY()) + (location.getX() - aimedAt.getX() < 0 ? -(location.getX() - aimedAt.getX()) : location.getX() - aimedAt.getX())) / 9;
			xPerFrame = (location.getX() - aimedAt.getX()) / framesToTravel;
			yPerFrame = (location.getY() - aimedAt.getY()) / framesToTravel;
			playSound(Miscellaneous.getResourceAsStream("sounds/rocket.wav"));
		}

		private boolean move() {
			if (!paused)
				location.add(-xPerFrame, -yPerFrame);
			return new Rectangle(location.getX(), location.getY(), rotatedRocket.getWidth(null), rotatedRocket.getHeight(null)).intersects(new Rectangle(playerLoc.getX(), playerLoc.getY(), 100, 80));
		}

		private void paint(Graphics g) {
			g.drawImage(rotatedRocket, location.getX(), location.getY(), null);
		}

	}

	private class Laser {

		private void checkDeaths() {
			Rectangle loc = new Rectangle(playerLoc.getX() + 42, playerLoc.getY() - 740, 16, 800);
			for (EnemyBullet bullet : new ArrayList<>(enemyBullets))
				if (loc.intersects(new Rectangle(bullet.location.getX(), bullet.location.getY())))
					enemyBullets.remove(bullet);
			for (Enemy enemy : new ArrayList<>(enemies))
				if (loc.intersects(new Rectangle(enemy.location.getX(), enemy.location.getY(), 100, 80))) {
					points += 1;
					highscore = points > highscore ? points : highscore;
					enemy.kill();
					enemies.remove(enemy);
				}
			for (HardEnemy enemy : new ArrayList<>(hardEnemies))
				if (loc.intersects(new Rectangle(enemy.location.getX(), enemy.location.getY(), 100, 80))) {
					points += 1;
					highscore = points > highscore ? points : highscore;
					enemy.kill();
					hardEnemies.remove(enemy);
				}
			if (currentBoss != null && loc.intersects(new Rectangle(currentBoss.location.getX(), currentBoss.location.getY(), 180, 160))) {
				points += 2;
				highscore = points > highscore ? points : highscore;
				currentBoss.kill();
				currentBoss = null;
			}
			if (currentBigBoss != null && loc.intersects(new Rectangle(currentBigBoss.location.getX(), currentBigBoss.location.getY(), 200, 200))) {
				points += 2;
				highscore = points > highscore ? points : highscore;
				currentBigBoss.kill();
				currentBigBoss = null;
			}
		}

		private void paint(Graphics g) {
			drawImage(g, laser, playerLoc.getX() + 42, playerLoc.getY() - 740);
		}

	}

	// ===ENEMIES===

	private class Enemy {

		private static final int	maxStrafeCount	= 50;
		private final Vec2i			location;
		private volatile boolean	strafeLeft		= false;
		private volatile boolean	strafeRight		= false;
		private volatile int		strafeCount		= 0;

		private Enemy(Vec2i location) {
			this.location = location;
		}

		private void move() {
			location.addY(2);
			if (strafeLeft && strafeCount < maxStrafeCount) {
				location.subtractX(1);
				strafeCount += 1;
			} else
				if (strafeRight && strafeCount < maxStrafeCount) {
					location.addX(1);
					strafeCount += 1;
				} else
					if (Random.randInt(50) == 25)
						if (Random.choice(true, false) && location.getX() > maxStrafeCount)
							strafeLeft = true;
						else
							if (location.getX() < getWidth() - maxStrafeCount - 100)
								strafeRight = true;
			if (strafeCount == maxStrafeCount) {
				strafeCount = 0;
				strafeLeft = false;
				strafeRight = false;
			}
			if (Random.randInt(250) == 150)
				shoot();
		}

		private void paint(Graphics g) {
			drawImage(g, enemy, location.getX(), location.getY());
		}

		private void kill() {
			explosions.add(new Explosion(location));
		}

		private void shoot() {
			if (!paused)
				enemyBullets.add(new EnemyBullet(new Vec2i(location.getX() + 50, location.getY() + 80)));
		}

	}

	private class HardEnemy {

		private static final int	maxStrafeCount	= 50;
		private final Vec2i			location;
		private volatile int		health			= 2;
		private volatile boolean	strafeLeft		= false;
		private volatile boolean	strafeRight		= false;
		private volatile int		strafeCount		= 0;

		private HardEnemy(Vec2i location) {
			this.location = location;
		}

		private void move() {
			location.addY(2);
			if (strafeLeft && strafeCount < maxStrafeCount) {
				location.subtractX(1);
				strafeCount += 1;
			} else
				if (strafeRight && strafeCount < maxStrafeCount) {
					location.addX(1);
					strafeCount += 1;
				} else
					if (Random.randInt(50) == 25)
						if (Random.choice(true, false) && location.getX() > maxStrafeCount)
							strafeLeft = true;
						else
							if (location.getX() < getWidth() - maxStrafeCount - 100)
								strafeRight = true;
			if (strafeCount == maxStrafeCount) {
				strafeCount = 0;
				strafeLeft = false;
				strafeRight = false;
			}
			if (Random.randInt(125) == 0)
				shoot();
		}

		private void paint(Graphics g) {
			g.drawImage(boss, location.getX(), location.getY(), location.getX() + 120, location.getY() + 110, 0, 0, 180, 160, null);
		}

		private void kill() {
			explosions.add(new Explosion(location));
		}

		private void shoot() {
			if (!paused)
				enemyBullets.add(new EnemyBullet(new Vec2i(location.getX() + 50, location.getY() + 80)));
		}

	}

	private class Boss {
		private final Vec2i			location;
		private final int			movePerTime		= 100;
		private volatile int		health			= points / 15 * 2;
		private final int			maxHealth		= health;
		private volatile Rocket		currentRocket	= null;
		private volatile boolean	isMoving		= false;
		private volatile boolean	moveRight		= false;
		private volatile boolean	shouldShoot		= false;
		private volatile boolean	isDead			= false;
		private volatile int		moveCount		= 0;
		private volatile int		framesWaited	= 0;

		private Boss() {
			location = new Vec2i(getWidth() / 2 - 90, -160);
		}

		void move() {
			if (location.getY() < 80)
				location.addY(5);
			else
				if (!isDead && !paused) {
					if (Random.randInt(100) == 0 && currentRocket == null)
						currentRocket = new Rocket(new Vec2i(location.getX() + 90, location.getY() + 80));
					if (currentRocket != null) {
						if (currentRocket.move())
							playerDied();
						if (currentRocket.location.getY() > getHeight() || currentRocket.location.getY() < -currentRocket.rotatedRocket.getHeight(null) || currentRocket.location.getX() > getWidth() || currentRocket.location.getX() < -currentRocket.rotatedRocket.getWidth(null))
							currentRocket = null;
					}
					if (!isMoving && Random.randInt(points < 50 ? 150 - points : 100) == 0 && !shouldShoot) {
						isMoving = true;
						moveRight = location.getX() - movePerTime < 0 ? true : location.getX() + movePerTime + 160 > getWidth() ? false : Random.choice(true, false);
					}
					if (isMoving) {
						location.addX(moveRight ? 5 : -5);
						moveCount += 5;
					}
					if (moveCount == movePerTime) {
						isMoving = false;
						moveCount = 0;
						shouldShoot = true;
					}
					if (shouldShoot)
						framesWaited += 1;
					if (shouldShoot && framesWaited % 20 == 0)
						if (framesWaited > 60) {
							framesWaited = 0;
							shouldShoot = false;
						} else
							if (framesWaited <= 60) {
								enemyBullets.add(new EnemyBullet(new Vec2i(location.getX() + 120, location.getY() + 160)));
								enemyBullets.add(new EnemyBullet(new Vec2i(location.getX() + 45, location.getY() + 160)));
							}
				}
		}

		void paint(Graphics g) {
			drawImage(g, boss, location.getX(), location.getY());
			if (currentRocket != null)
				currentRocket.paint(g);
			drawHealthBar(g, health, maxHealth, location.getX() + 40, location.getY() - 20);
		}

		private void kill() {
			isDead = true;
			explosions.add(new Explosion(location, true));
			explosions.add(new Explosion(new Vec2i(location.getX() + 180, location.getY())));
			explosions.add(new Explosion(new Vec2i(location.getX(), location.getY() + 160)));
			explosions.add(new Explosion(new Vec2i(location.getX() + 180, location.getY() + 160)));
		}
	}

	private class BigBoss {
		private final Vec2i			location;
		private final int			movePerTime		= 200;
		private volatile boolean	isDead			= false;
		private volatile int		health			= points / 30 * 3;
		private volatile int		maxHealth		= health;
		private volatile Rocket		rocket1;
		private volatile Rocket		rocket2;
		private volatile boolean	isMoving		= false;
		private volatile boolean	isShooting		= false;
		private volatile boolean	moveRight		= false;
		private volatile int		moveCount		= 0;
		private volatile int		framesWaited	= 0;
		private volatile boolean	shouldFire		= false;

		private BigBoss() {
			location = new Vec2i(getWidth() / 2 - bigBoss.getWidth(null) / 2, -200);
		}

		private void move() {
			if (!isDead)
				if (location.getY() < 80)
					location.addY(5);
				else {
					if (!isMoving)
						if (Random.randInt(75) == 0 && rocket1 == null && rocket1 == null) {
							rocket1 = new Rocket(new Vec2i(location.getX() + 61, location.getY() + 160));
							rocket2 = new Rocket(new Vec2i(location.getX() + 127, location.getY() + 160));
							isShooting = true;
						} else {
							if (rocket1 != null)
								if (rocket1.move())
									playerDied();
								else
									if (rocket1.location.getY() > getHeight() || rocket1.location.getY() < -rocket1.rotatedRocket.getHeight(null) || rocket1.location.getX() > getWidth() || rocket1.location.getX() < -rocket1.rotatedRocket.getWidth(null)) {
										rocket1 = null;
										if (rocket2 == null)
											isShooting = false;
									}
							if (rocket2 != null) {
								if (rocket2.move())
									playerDied();
								if (rocket2.location.getY() > getHeight() || rocket2.location.getY() < -rocket2.rotatedRocket.getHeight(null) || rocket2.location.getX() > getWidth() || rocket2.location.getX() < -rocket2.rotatedRocket.getWidth(null)) {
									rocket2 = null;
									if (rocket1 == null)
										isShooting = false;
								}
							}
						}
					if (!isMoving && Random.randInt(points < 50 ? 150 - points : 100) == 0 && !isShooting) {
						isMoving = true;
						moveRight = location.getX() - movePerTime < 0 ? true : location.getX() + movePerTime + 200 > getWidth() ? false : Random.choice(true, false);
					}
					if (isMoving) {
						location.addX(moveRight ? 10 : -10);
						moveCount += 10;
					}
					if (moveCount == movePerTime) {
						isMoving = false;
						moveCount = 0;
						shouldFire = true;
					}
					if (shouldFire) {
						framesWaited += 1;
						if (framesWaited % 20 == 0)
							if (framesWaited == 180) {
								framesWaited = 0;
								shouldFire = false;
							} else
								if (framesWaited <= 60 || framesWaited >= 120) {
									enemyBullets.add(new EnemyBullet(new Vec2i(location.getX() + 61, location.getY() + 145)));
									enemyBullets.add(new EnemyBullet(new Vec2i(location.getX() + 127, location.getY() + 145)));
								}
					}
				}
		}

		private void paint(Graphics g) {
			if (rocket1 != null)
				rocket1.paint(g);
			if (rocket2 != null)
				rocket2.paint(g);
			drawImage(g, bigBoss, location.getX(), location.getY());
			g.setColor(Color.WHITE);
			drawHealthBar(g, health, maxHealth, location.getX() + 50, location.getY() - 20);
		}

		private void kill() {
			isDead = true;
			explosions.add(new Explosion(location, true));
			explosions.add(new Explosion(new Vec2i(location.getX() + 180, location.getY())));
			explosions.add(new Explosion(new Vec2i(location.getX(), location.getY() + 160)));
			explosions.add(new Explosion(new Vec2i(location.getX() + 180, location.getY() + 160)));
		}

	}

	// ===CONSUMABLES===

	private class Cherry {
		private final Vec2i location;

		private Cherry(Vec2i location) {
			this.location = location;
		}

		private boolean move() {
			location.addY(1);
			return new Rectangle(location.getX(), location.getY(), 32, 32).intersects(new Rectangle(playerLoc.getX(), playerLoc.getY(), 100, 80));
		}

		private void paint(Graphics g) {
			drawImage(g, cherry, location.getX(), location.getY());
		}

	}

	private class Heart {

		private final Vec2i location;

		private Heart(Vec2i location) {
			this.location = location;
		}

		private boolean move() {
			location.addY(1);
			return new Rectangle(location.getX(), location.getY(), 30, 30).intersects(new Rectangle(playerLoc.getX(), playerLoc.getY(), 100, 80));
		}

		private void paint(Graphics g) {
			drawImage(g, heart, location.getX(), location.getY());
		}

	}

	private class Battery {

		private final Vec2i location;

		private Battery(Vec2i location) {
			this.location = location;
		}

		private boolean move() {
			location.addY(1);
			return new Rectangle(location.getX(), location.getY(), 16, 32).intersects(new Rectangle(playerLoc.getX(), playerLoc.getY(), 100, 80));
		}

		private void paint(Graphics g) {
			drawImage(g, battery, location.getX(), location.getY());
		}

	}

	// ===SPECIAL EFFECTS===

	private class Explosion {
		private final Vec2i		location;
		private final boolean	isBossExplosion;
		private volatile int	frame	= 0;
		private volatile int	passed	= 0;

		private Explosion(Vec2i location) {
			this(location, false);
		}

		private Explosion(Vec2i location, boolean isBossExplosion) {
			this.location = location;
			this.isBossExplosion = isBossExplosion;
			playSound(Miscellaneous.getResourceAsStream("sounds/explosion.wav"));
		}

		private boolean move() {
			passed += 1;
			if (passed == 2) {
				passed = 0;
				frame += 1;
			}
			if (frame > 16) {
				if (isBossExplosion)
					currentBoss = null;
				return false;
			}
			return true;
		}

		private void paint(Graphics g) {
			if (frame <= 16)
				drawImage(g, explosionFrames.get(frame), location.getX(), location.getY());
		}
	}

}
