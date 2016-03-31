import java.awt.image.BufferedImage;

public class MasterThread extends Thread {
	public static final double WIDTH_SCALE = 3.0;
	public static final double HEIGHT_SCALE = 2.5;
	public static final double RATIO = WIDTH_SCALE / HEIGHT_SCALE;
	
	private final Mandelbrot mb;
	private final RenderType type;
	private final Settings set;

	public MasterThread(Mandelbrot mb, RenderType type) {
		super("MasterThread");
		this.mb = mb;
		this.type = type;
		set = mb.getSettings();
	}
	
	@Override
	public void run() {
		BufferedImage image = mb.getImage();
		
		int width = image.getWidth(), height = image.getHeight();
		double scale = Math.max(WIDTH_SCALE/width, HEIGHT_SCALE/height) / set.zoom;
		double x0 = (width * 5.0 - Math.max(width - (height*RATIO), 0.0)) / 7.0 * scale;
		x0 += (0.5 - set.xPan) * WIDTH_SCALE * (set.zoom - 1.0) / set.zoom;
		x0 += (5.0/7.0) * (set.zoom - 1.0) / set.zoom;
		double y0 = height / 2.0 * scale;
		y0 += (0.5 - set.yPan) * HEIGHT_SCALE * (set.zoom - 1.0) / set.zoom;
		
		int cores = Runtime.getRuntime().availableProcessors();
		Config con = new Config(width, height, scale, x0, y0, cores, 
				image, mb.getData(), type.need2Buffers(), mb.getSettings());
		
		InterlaceDrawThread[] threads = new InterlaceDrawThread[cores];
		for (int i=0; i < cores; i++) threads[i] = type.getInstance(i, con);
		for (Thread t : threads) t.start();
		try {
			for (Thread t : threads) t.join();
		} catch (InterruptedException e) {
			for (InterlaceDrawThread t : threads) t.shutdown();
			return;
		}
		mb.getTimer().cancel();
		mb.flipBuffer();
		mb.repaint();
	}
	
	public static enum RenderType {
		DRAW(InterlaceDrawThread.class, false, null),
		NOAA(DefaultCalcThread.class, false, "No Antialiasing"),
		SSAA(SSAACalcThread.class, false, "Supersampling Antialiasing"),
		MSAA(MSAAThread.class, true, "Selective Supersampling"),
		;
		
		Class<? extends InterlaceDrawThread> cla;
		boolean twoBuffers;
		String desc;
		
		RenderType(Class<? extends InterlaceDrawThread> cla, boolean tb, String d) {
			this.cla = cla;
			twoBuffers = tb;
			desc = d;
		}
		
		public boolean need2Buffers() {
			return twoBuffers;
		}
		
		public InterlaceDrawThread getInstance(int i, Config con) {
			try {
				return cla.getConstructor(int.class, Config.class).newInstance(i, con);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public String toString() {
			return desc;
		}
	}
	
}
