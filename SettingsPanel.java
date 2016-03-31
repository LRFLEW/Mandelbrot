import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class SettingsPanel extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final Mandelbrot mb;
	private final Settings set = new Settings();

	public SettingsPanel(Mandelbrot mb) {
		super("Settings");
		this.mb = mb;
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		
		JTabbedPane tp = new JTabbedPane();
		tp.addTab("Colors", new ColorTab(this));
		tp.addTab("Advanced", new AdvancedTab(this));
		add(tp);
	}
	
	public Settings getSettings() {
		return set;
	}
	
	public Mandelbrot getMandelbrot() {
		return mb;
	}

}