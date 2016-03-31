import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.lang.Thread.State;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Mandelbrot extends JComponent {
	private static final long serialVersionUID = 1L;

	private BufferedImage image;
	private BufferedImage oldImage = null;
	private RawData data;
	private Thread oldMt = new Thread();
	private Timer timer = new Timer("RedrawThread");
	private Settings set = new Settings();
	private Thread mt = new MasterThread(this, set.renderType);
	private SettingsPanel sp = new SettingsPanel(this);
	private boolean forceRedraw = false;
	private boolean forceRecalc = false;
	private boolean newSize = true;
	private int oldWidth = -1;
	private int oldHeight = -1;

	public static void main(String[] args) {
		JFrame frame = new JFrame("Mandlebrot Set");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Mandelbrot m = new Mandelbrot();
		m.setPreferredSize(new Dimension(800, 600));
		frame.add(m);
		frame.pack();
		frame.setVisible(true);
		
		frame = m.sp;
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(checkAndDoRerender(), 0, 0, getWidth(), getHeight(), null);
	}
	
	public void forceRerdraw() {
		forceRedraw = true;
		repaint();
	}
	
	public void forceRecalc() {
		forceRecalc = true;
		forceRedraw = true;
		repaint();
	}
	
	public boolean getRecalc() {
		return forceRecalc;
	}
	
	private BufferedImage checkAndDoRerender() {
		boolean b = getWidth() != oldWidth || getHeight() != oldHeight;
		if (b) newSize = true;
		if (forceRedraw || b) reDraw();
		if (b) return oldImage;
		try {
			oldMt.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (newSize) resizeBuffer();
		if (mt.getState() == State.NEW) mt.start();
		return image;
	}
	
	private void reDraw() {
		if (forceRedraw) {
			image.getGraphics().clearRect(0, 0, image.getWidth(), image.getHeight());
			forceRedraw = false;
		}
		oldWidth = getWidth();
		oldHeight = getHeight();
		if (mt.getState() != State.NEW) {
			mt.interrupt();
			oldMt = mt;
			mt = new MasterThread(this, 
					forceRecalc || newSize ? set.renderType : MasterThread.RenderType.DRAW);
			forceRecalc = false;
		}
		timer.cancel();
		timer = new Timer("RefreshThread");
		timer.scheduleAtFixedRate(new TimerTask(){ public void run() { repaint(); } }, 50, 30);
	}
	
	private void resizeBuffer() {
		int w = (int) (getWidth() * set.screenScale), h = (int) (getHeight() * set.screenScale);
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		data = new RawData(w, h);
		newSize = false;
	}
	
	public BufferedImage getImage() {
		return image;
	}

	public Timer getTimer() {
		return timer;
	}
	
	public RawData getData() {
		return data;
	}
	
	public Settings getSettings() {
		return set;
	}
	
	public void setSettings(Settings set) {
		this.set.copyValues(set);
	}

	public void flipBuffer() {
		oldImage = image;
	}

	public static float getScale() {

		float scale = 1;
		GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		try {
			Field field = graphicsDevice.getClass().getDeclaredField("scale");
			if (field != null) {
				field.setAccessible(true);
				Object oscale = field.get(graphicsDevice);
				if(oscale instanceof Integer) scale = (Integer) oscale;
			} else {
				Object property = Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.contentScaleFactor");    
				if (property instanceof Integer) scale = (Float) property;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return scale;
	}

}
