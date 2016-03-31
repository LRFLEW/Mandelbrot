import java.awt.Color;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Settings {
	
	private final TreeMap<Float, Color> map = new TreeMap<Float, Color>();
	private Color in = Color.BLACK;
	private Color debug = Color.RED;
	
	public float screenScale = Mandelbrot.getScale();
	public int iters = 5000;
	public int AaX = 4;
	public int AaY = 4;
	public MasterThread.RenderType renderType = MasterThread.RenderType.MSAA;
	
	public double zoom = 1.0;
	public double xPan = 0.5;
	public double yPan = 0.5;
	
	public Settings() {
		map.put(1.0f, Color.RED);
		map.put(0.5f, Color.ORANGE);
		map.put(0.25f, Color.WHITE);
		map.put(0.1f, Color.GRAY);
		map.put(0.0f, Color.BLUE);
	}
	
	public Color getColor(float i) {
		Entry<Float, Color> high = map.ceilingEntry(i);
		Entry<Float, Color> low = map.floorEntry(i);
		Color lowC = low.getValue();
		Color highC = high.getValue();
		if (lowC.equals(highC)) return low.getValue();
		float hk = high.getKey();
		float lk = low.getKey();
		double c = (double) (i - lk)/(hk - lk);
		return gammaInterpolation(highC, lowC, c);
	}
	
	public static Color gammaInterpolation(Color c1, Color c2, double c) {
		double d = 1.0 - c;
		float[] f1 = c1.getRGBColorComponents(null);
		float[] f2 = c2.getRGBColorComponents(null);
		for (int i=0; i<3; ++i) f1[i] = (float) (f1[i] * f1[i]);
		for (int i=0; i<3; ++i) f2[i] = (float) (f2[i] * f2[i]);
		for (int i=0; i<3; ++i) f1[i] = (float) (f1[i]*c + f2[i]*d);
		for (int i=0; i<3; ++i) f1[i] = (float) Math.sqrt(f1[i]);
		return new Color(f1[0], f1[1], f1[2]);
	}

	public Color getIn() {
		return in;
	}
	
	public Color getDebug() {
		return debug;
	}
	
	public TreeMap<Float, Color> getMap() {
		return map;
	}

	public void setIn(Color in) {
		this.in = in;
	}
	
	public void copyValues(Settings set) {
		map.clear();
		map.putAll(set.map);
		in = set.in;
		debug = set.debug;
		
		screenScale = set.screenScale;
		iters = set.iters;
		AaX = set.AaX;
		AaY = set.AaY;
		renderType = set.renderType;
		
		zoom = set.zoom;
		xPan = set.xPan;
		yPan = set.yPan;
	}
	
}
