public class DefaultCalcThread extends InterlaceDrawThread {
	protected static final double LOG_2 = Math.log(2);
	protected static final double D2_LOG_2 = Math.log(2*LOG_2);
	
	public static final double CUT_OFF = (1 << 16);

	public DefaultCalcThread(int i, Config con) {
		super(i, con);
	}
	
	@Override
	protected RawData.DataChunk calcPixel(int x, int y) {
		double b0 = y * con.scale - con.y0;
		double a0 = x * con.scale - con.x0;
		double b = 0.0, a = 0.0;
		for (int i=0; i < con.set.iters; i++) {
			double at = a*a - b*b + a0;
			b = 2*a*b + b0;
			a = at;
			double sq = (a*a) + (b*b);
			if (sq > CUT_OFF) {
				double n = (Math.log(Math.log(sq)) - D2_LOG_2)/LOG_2;
				n = i + 1 - n;
				return new RawData.MissDataChunk(n);
			}
		}
		return RawData.empty;
	}
	
	@Override
	protected synchronized void setData(int x, int y, RawData.DataChunk dc) {
		if (shutdown) return;
		con.data.setData(x, y, dc);
	}

}
