public class SSAACalcThread extends DefaultCalcThread {

	public SSAACalcThread(int i, Config con) {
		super(i, con);
	}
	
	@Override
	protected RawData.DataChunk calcPixel(int x, int y) {
		int dcc = 0;
		RawData.DataChunk[] dcs = new RawData.DataChunk[con.AaZ];
		double a00 = x * con.scale - con.x0;
		double b0 = y * con.scale - con.y0;
		for (int dy=0; dy < con.set.AaY; dy++) {
			if (dy != 0) b0 += con.mscaley;
			double a0 = a00;
			ch: for (int dx=0; dx < con.set.AaX; dx++) {
				if (dx != 0) a0 += con.mscalex;
				double b = 0.0, a = 0.0;
				for (int i=0; i < con.set.iters; i++) {
					double at = a*a - b*b + a0;
					b = 2*a*b + b0;
					a = at;
					double sq = (a*a) + (b*b);
					if (sq > CUT_OFF) {
						double n = (Math.log(Math.log(sq)) - D2_LOG_2)/LOG_2;
						n = i + 1 - n;
						dcs[dcc++] = new RawData.MissDataChunk(n);
						continue ch;
					}
				}
				dcs[dcc++] = RawData.empty;
			}
		}
		return new RawData.AADataChunk(dcs);
	}
	
}
