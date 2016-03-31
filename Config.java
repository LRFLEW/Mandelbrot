import java.awt.image.BufferedImage;

public class Config {
	public final int width, height, cores, AaZ;
	public final double scale, x0, y0, mscalex, mscaley;
	public final BufferedImage image;
	public final Settings set;
	public final RawData data;
	public final Config sub;

	public Config(int width, int height, double scale, double x0, double y0, 
			int cores, BufferedImage image,
			RawData data, boolean twoStep, Settings set) {
		this.set = set;
		this.x0 = x0;
		this.y0 = y0;
		this.data = data;
		this.width = width;
		this.scale = scale;
		this.cores = cores;
		this.image = image;
		this.height = height;
		
		this.mscalex = scale/set.AaX;
		this.mscaley = scale/set.AaY;
		this.AaZ = set.AaX*set.AaY;
		
		if (twoStep) {
			this.sub = new Config(width, height, scale, x0, y0, cores,
					image, data.lockingAlt(), false, set);
		} else {
			this.sub = null;
		}
	}

}