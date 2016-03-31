public class MSAAThread extends SSAACalcThread {
	
	public static final int MID_PRIORITY = (Thread.NORM_PRIORITY + Thread.MIN_PRIORITY)/2;
	
	private RawData.DataChunk[][] dcs = new RawData.DataChunk[3][];
	private SubCalcThread st = new SubCalcThread(i, con.sub);

	public MSAAThread(int i, Config con) {
		super(i, con);
	}

	@Override
	protected RawData.DataChunk calcPixel(int x, int y) {
		dcs[2] = getSubColumn(x, y);
		if (shutdown) return null;
		boolean aa = false;
		checks: for (RawData.DataChunk[] dcc : dcs) {
			if (dcc == null) continue;
			for (RawData.DataChunk dc : dcc) {
				if (dc == null) continue;
				if (dc instanceof RawData.MissDataChunk) {
					aa = true;
					break checks;
				}
			}
		}
		RawData.DataChunk dc = aa ? super.calcPixel(x, y) : dcs[1][1];
		dcs[0] = dcs[1];
		dcs[1] = dcs[2];
		return dc;
	}
	
	@Override
	protected void preRun() {
		st.start();
	}
	
	@Override
	protected void preX(int y) {
		if (shutdown) return;
		dcs[0] = null;
		dcs[1] = getSubColumn(-1, y);
	}
	
	@Override
	public synchronized void shutdown() {
		st.shutdown();
		super.shutdown();
	}

	private RawData.DataChunk[] getSubColumn(int x, int y) {
		int vx = x + 1;
		if (vx >= con.width) return null;
		RawData.DataChunk[] dcc = new RawData.DataChunk[3];
		for (int dy=0; dy < 3; dy++) {
			int vy = y + dy - 1;
			if (vy < 0 || vy >= con.height) continue;
			try {
				con.sub.data.awaitSet(vx, vy);
			} catch (InterruptedException e) {
				return null;
			}
			dcc[dy] = con.sub.data.getData(vx, vy);
		}
		return dcc;
	}
	
	private static class SubCalcThread extends DefaultCalcThread {

		public SubCalcThread(int i, Config con) {
			super(i, con);
			this.setPriority(MID_PRIORITY);
		}
		
		@Override
		protected synchronized void drawPixel(int x, int y, RawData.DataChunk dc) {}
		
	}

}
