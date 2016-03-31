import java.awt.Color;
import java.util.concurrent.CountDownLatch;

public class RawData {
	public static final DataChunk empty = new DataChunk();
	public static final DataChunk debug = new DebugChunk();
	
	private final int width, height;
	private final DataChunk[][] data;
	private final CountDownLatch[][] latches;
	
	public RawData(int width, int height) {
		this(width, height, false);
	}
	
	private RawData(int width, int height, boolean includeLock) {
		this.width = width;
		this.height = height;
		data = new DataChunk[height][width];
		if (includeLock) {
			latches = new CountDownLatch[height][width];
			resetLatches();
		}
		else latches = null;
	}
	
	public void resetLatches() {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				latches[y][x] = new CountDownLatch(1);
			}
		}
	}
	
	public DataChunk getData(int x, int y) {
		return data[y][x];
	}
	
	public void setData(int x, int y, DataChunk dc) {
		data[y][x] = dc;
		if (latches != null) latches[y][x].countDown();
	}
	
	public void awaitSet(int x, int y) throws InterruptedException {
		if (latches != null) latches[y][x].await();
	}
	
	public RawData lockingAlt() {
		return new RawData(width, height, true);
	}

	public static class DataChunk {
		private DataChunk() {}
		
		@Override
		public String toString() {
			return "InSet";
		}
		
		public Color getColor(Settings set) {
			return set.getIn();
		}
	}
	
	public static class DebugChunk extends DataChunk {
		private DebugChunk() {}
		
		@Override
		public String toString() {
			return "Debug";
		}
		
		@Override
		public Color getColor(Settings set) {
			return set.getDebug();
		}
	}
	
	public static class MissDataChunk extends DataChunk {
		public final float i;
		
		public MissDataChunk(double ave) {
			if (ave < 0.0) ave = 0.0;
			ave = 2.0/(ave + 1.0);
			if (ave >= 1.0) ave = 1.0;
			i = (float) ave;
		}
		
		@Override
		public String toString() {
			return "OutSet:" + i;
		}
		
		@Override
		public Color getColor(Settings set) {
			return set.getColor(i);
		}
	}
	
	public static class AADataChunk extends DataChunk {
		public final DataChunk[] dcs;
		
		public AADataChunk(DataChunk[] dcs) {
			this.dcs = dcs;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Antialiased Set: {");
			boolean cm = false;
			for(DataChunk dc : dcs) {
				if (cm) sb.append(", ");
				else cm = true;
				sb.append(dc.toString());
			}
			sb.append("}");
			return sb.toString();
		}
		
		@Override
		public Color getColor(Settings set) {
			if (dcs.length == 0) return debug.getColor(set);
			float[] color, tmp;
			color = dcs[0].getColor(set).getRGBColorComponents(null);
			tmp = new float[3];
			for (int i=0; i<3; ++i) color[i] *= color[i];
			for (int i=1; i < dcs.length; ++i) {
				dcs[i].getColor(set).getRGBColorComponents(tmp);
				for (int j=0; j<3; ++j) tmp[j] *= tmp[j];
				for (int j=0; j<3; ++j) color[j] = (color[j]*i + tmp[j])/(i+1);
			}
			for (int i=0; i<3; ++i) color[i] = (float) Math.sqrt(color[i]);
			return new Color(color[0], color[1], color[2]);
		}
	}
	
}
