import java.awt.Color;

public class InterlaceDrawThread extends Thread {
	
	protected boolean shutdown = false;
	protected final Config con;
	protected final int i;
	
	public InterlaceDrawThread(int i, Config con) {
		this.setName(this.getClass().getName() + ":" + i);
		this.con = con;
		this.i = i;
	}
	
	@Override
	public void run() {
		preRun();
		for (int y=i; y < con.height; y += con.cores) {
			preX(y);
			for (int x=0; x < con.width; x++) {
				if (shutdown) return;
				RawData.DataChunk dc = calcPixel(x, y);
				setData(x, y, dc);
				drawPixel(x, y, dc);
			}
		}
	}
	
	protected void preRun() {}
	
	protected void preX(int y) {}
	
	protected RawData.DataChunk calcPixel(int x, int y) {
		return con.data.getData(x, y);
	}
	
	protected void setData(int x, int y, RawData.DataChunk dc) {}
	
	protected synchronized void drawPixel(int x, int y, RawData.DataChunk dc) {
		if (shutdown) return;
		con.image.setRGB(x, y, getColor(dc).getRGB());
	}
	
	protected Color getColor(RawData.DataChunk dc) {
		return dc.getColor(con.set);
	}
	
	public synchronized void shutdown() {
		shutdown = true;
		this.interrupt();
	}
	
}
