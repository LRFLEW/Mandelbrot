import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorTab extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private final Settings set;
	private final SettingsPanel sp;
	private final TreeMap<Float, Color> map;
	
	private float key;
	
	public ColorTab(SettingsPanel sp) {
		this.sp = sp;
		this.set = sp.getSettings();
		this.map = set.getMap();
		key = map.lastKey();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.add(new GradientPreview());
		AddNodes an = new AddNodes();
		PlacementPicker pp = new PlacementPicker();
		SecondOpt so = new SecondOpt(an, pp);
		Navigation nv = new Navigation(pp, an, so);
		an.setNavigation(nv);
		so.setNavigation(nv);
		this.add(nv);
		this.add(an);
		this.add(so);
		this.add(pp);
		this.add(new ColorPreview());
		this.add(new CompoundSliders());
		
		JButton rd = new JButton("Redraw");
		this.add(rd);
		rd.setAlignmentX(Component.CENTER_ALIGNMENT);
		rd.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		Mandelbrot mb = sp.getMandelbrot();
		mb.setSettings(set);
		mb.forceRerdraw();
	}
	
	private class GradientPreview extends JComponent {
		private static final long serialVersionUID = 1L;
		
		private static final int width = 200;
		private static final int height = 55;
		private static final int ind = 5;
		private static final int ind2 = ind*2;
		
		private BufferedImage image = new BufferedImage(width, 1, BufferedImage.TYPE_INT_RGB);
		
		public GradientPreview() {
			this.setPreferredSize(new Dimension(width, height));
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			if (getWidth() != image.getWidth())
				image = new BufferedImage(getWidth() - 2, 1, BufferedImage.TYPE_INT_RGB);
			float d = 1.0f/image.getWidth();
			float t = 1.0f;
			for (int x=0; x < image.getWidth(); x++) {
				image.setRGB(x, 0, set.getColor(t).getRGB());
				t -= d;
			}
			g.drawImage(image, 1, ind, getWidth() - 2, getHeight() - ind2, null);
			if (key < 0.0f) return;
			int w;
			if (key == 0.0f) w = getWidth() - 1;
			else if (key == 1.0f) w = 1;
			else w = (int) (getWidth() * (1.0f - key));
			g.setColor(Color.BLACK);
			g.drawLine(w, 0, w, getHeight());
		}
		
	}
	
	private class ColorPreview extends JComponent {
		private static final long serialVersionUID = 1L;
		
		private static final int width = 50;
		private static final int height = 30;
		
		public ColorPreview() {
			this.setPreferredSize(new Dimension(width, height));
			this.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			if (key == -1.0f) g.setColor(set.getIn());
			else g.setColor(map.get(key));
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, getWidth(), getHeight());
		}
	}
	
	private class Navigation extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		
		private final PlacementPicker pp;
		private final AddNodes an;
		private final SecondOpt so;
		private final JButton prev;
		private final JButton next;
		
		public Navigation(PlacementPicker pp, AddNodes an, SecondOpt so) {
			this.pp = pp;
			this.an = an;
			this.so = so;
			
			this.add(prev = new JButton("<-"));
			this.add(next = new JButton("->"));
			
			prev.addActionListener(this);
			next.addActionListener(this);
			
			prev.setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent e) {
			selectNew(e.getSource() == prev);
		}

		private void selectNew(boolean b) {
			if (key < 0.0f) return;
			if (b) {
				if (key == 1.0f) return;
				key = map.higherKey(key);
			} else {
				if (key == 0.0f) return;
				key = map.lowerKey(key);
			}
			if (key == 0.0f) {
				next.setEnabled(false);
				prev.setEnabled(true);
				pp.setEnabledS(false);
				an.setEnabled(true, false);
				so.setEnabledDel(false);
			} else if (key == 1.0f) {
				next.setEnabled(true);
				prev.setEnabled(false);
				pp.setEnabledS(false);
				an.setEnabled(false, true);
				so.setEnabledDel(false);
			} else {
				next.setEnabled(true);
				prev.setEnabled(true);
				pp.setEnabledS(true);
				an.setEnabled(true, true);
				so.setEnabledDel(true);
			}
			ColorTab.this.repaint();
		}
		
		public void setEnabled(boolean l, boolean r) {
			prev.setEnabled(l);
			next.setEnabled(r);
		}
		
	}
	
	private class ColorSlider extends JPanel implements ChangeListener {
		private static final long serialVersionUID = 1L;
		
		private final int type;
		private final JLabel label;
		private final JSlider slide;
		private final JSpinner field;
		
		private boolean lock = false;
		
		public ColorSlider(int type) {
			this.type = type;
			
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			this.add(label = new JLabel(getChannel()));
			this.add(slide = new JSlider(JSlider.VERTICAL, 0, 255, getField()));
			SpinnerModel model = new SpinnerNumberModel(getField(), 0, 255, 1);
			this.add(field = new JSpinner(model));
			
			slide.addChangeListener(this);
			field.addChangeListener(this);
			
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		
		public void update() {
			int i = getField();
			lock = true;
			field.setValue(i);
			slide.setValue(i);
			lock = false;
		}
		
		int getField() {
			switch(type) {
			case 0:
				if (key == -1.0f) return set.getIn().getRed();
				else return map.get(key).getRed();
			case 1:
				if (key == -1.0f) return set.getIn().getGreen();
				else return map.get(key).getGreen();
			case 2:
				if (key == -1.0f) return set.getIn().getBlue();
				else return map.get(key).getBlue();
			default:
				throw new RuntimeException();
			}
		}
		
		Color getMod(int i) {
			Color c;
			if (key == -1.0f) c = set.getIn();
			else c = map.get(key);
			switch(type) {
			case 0:
				return new Color(i, c.getGreen(), c.getBlue());
			case 1:
				return new Color(c.getRed(), i, c.getBlue());
			case 2:
				return new Color(c.getRed(), c.getGreen(), i);
			default:
				throw new RuntimeException();
			}
		}
		
		String getChannel() {
			switch(type) {
			case 0:
				return "red";
			case 1:
				return "green";
			case 2:
				return "blue";
			default:
				throw new RuntimeException();
			}
		}
		
		public void stateChanged(ChangeEvent e) {
			if (lock) return;
			int i;
			if (e.getSource() == field) {
				i = (Integer) field.getValue();
				slide.setValue(i);
			} else {
				i = slide.getValue();
				field.setValue(i);
			}
			if (key == -1.0f) set.setIn(getMod(i));
			else map.put(key, getMod(i));
			ColorTab.this.repaint();
		}
	}
	
	private class CompoundSliders extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private final ColorSlider red;
		private final ColorSlider green;
		private final ColorSlider blue;
		
		public CompoundSliders() {
			this.add(red = new ColorSlider(0));
			this.add(green = new ColorSlider(1));
			this.add(blue = new ColorSlider(2));
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			red.update();
			green.update();
			blue.update();
			super.paintComponent(g);
		}
	}
	
	private class PlacementPicker extends JPanel implements ChangeListener {
		private static final long serialVersionUID = 1L;
		
		private final JSlider slide;
		private final JSpinner field;
		
		private boolean lock = false;
		
		public PlacementPicker() {
			SpinnerModel model = new SpinnerNumberModel(1.0, 0.0, 1.0, 0.01);
			this.add(field = new JSpinner(model));
			this.add(slide = new JSlider(JSlider.HORIZONTAL, 0, 100, 0));
			((JSpinner.DefaultEditor) field.getEditor()).getTextField().setColumns(3);
			
			field.addChangeListener(this);
			slide.addChangeListener(this);
			
			field.setEnabled(false);
			slide.setEnabled(false);
		}
		
		public void setEnabledS(boolean b) {
			field.setEnabled(b);
			slide.setEnabled(b);
			if (key >= 0.0f) {
				lock = true;
				slide.setValue((int) ((1.0f - key) * 100.0));
				lock = false;
				field.setValue((double) key);
			}
		}
		
		public void stateChanged(ChangeEvent e) {
			if (lock) return;
			if (key < 0.0f) return;
			float f;
			if (e.getSource() == field) {
				f = ((Double) field.getValue()).floatValue();
			} else {
				f = (float) (100 - slide.getValue()) / 100;
			}
			if (f <= 0.0f || f >= 1.0f || map.containsKey(f)) {
				field.setValue((double) key);
				slide.setValue((int) ((1.0f - key) * 100));
				return;
			}
			field.setValue((double) f);
			Color c = map.get(key);
			map.remove(key);
			key = f;
			map.put(key, c);
			ColorTab.this.repaint();
		}
	}
	
	private class AddNodes extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		
		private Navigation nv;
		private final JButton addL;
		private final JButton addR;
		
		public AddNodes() {
			this.add(addL = new JButton("<- Add"));
			this.add(addR = new JButton("Add ->"));
			
			addL.addActionListener(this);
			addR.addActionListener(this);
			
			addL.setEnabled(false);
		}
		
		void setNavigation(Navigation nv) {
			this.nv = nv;
		}
		
		public void actionPerformed(ActionEvent e) {
			if (key < 0.0f) return;
			Object o = e.getSource();
			boolean b = o == addL;
			float f = b ? map.higherKey(key) : map.lowerKey(key);
			f = (key - f)/2.0f + f;
			map.put(f, set.getColor(f));
			nv.selectNew(b);
			ColorTab.this.repaint();
		}
		
		public void setEnabled(boolean l, boolean r) {
			addL.setEnabled(l);
			addR.setEnabled(r);
		}
		
	}
	
	private class SecondOpt extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		
		private Navigation nv;
		private final AddNodes an;
		private final PlacementPicker pp;
		private final JButton del;
		private final JButton in;
		private float okey;
		
		public SecondOpt(AddNodes an, PlacementPicker pp) {
			this.an = an;
			this.pp = pp;
			
			this.add(del = new JButton("Delete"));
			this.add(in = new JButton("In Set"));
			
			del.addActionListener(this);
			in.addActionListener(this);
			
			del.setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == del) {
				map.remove(key);
				boolean b = map.higherKey(key) - key <= key - map.lowerKey(key);
				nv.selectNew(b);
			} else if (key < 0.0f) {
				key = okey;
				okey = -1.0f;
				boolean l = key != 0.0f, r = key != 1.0f, b = l && r;
				del.setEnabled(b);
				an.setEnabled(r, l);
				nv.setEnabled(r, l);
				pp.setEnabledS(b);
			} else {
				okey = key;
				key = -1.0f;
				del.setEnabled(false);
				an.setEnabled(false, false);
				nv.setEnabled(false, false);
				pp.setEnabledS(false);
			}
			ColorTab.this.repaint();
			if (key < 0.0f) in.setText("Out Set");
			else in.setText("In Set");
		}
		
		void setNavigation(Navigation nv) {
			this.nv = nv;
		}
		
		public void setEnabledDel(boolean b) {
			del.setEnabled(b);
		}
		
	}

}
