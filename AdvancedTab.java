import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AdvancedTab extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private final Settings set;
	private final SettingsPanel sp;
	
	public AdvancedTab(SettingsPanel sp) {
		this.sp = sp;
		this.set = sp.getSettings();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JComboBox<MasterThread.RenderType> aa = new JComboBox<MasterThread.RenderType>(
				new MasterThread.RenderType[] 
						{
								MasterThread.RenderType.NOAA,
								MasterThread.RenderType.SSAA,
								MasterThread.RenderType.MSAA
						});
		aa.setSelectedItem(set.renderType);
		
		JPanel aaby = new JPanel();
		SpinnerModel model = new SpinnerNumberModel(set.AaX, 1, 255, 1);
		JSpinner spinx = new JSpinner(model);
		spinx.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				set.AaX = (Integer) spinx.getValue();
			}
		});


		model = new SpinnerNumberModel(set.AaY, 1, 255, 1);
		JSpinner spiny = new JSpinner(model);
		spiny.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				set.AaY = (Integer) spiny.getValue();
			}
		});

		aaby.add(new JLabel("Samples: "));
		aaby.add(new JLabel("x ="));
		aaby.add(spinx);
		aaby.add(new JLabel("y ="));
		aaby.add(spiny);
		
		aa.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				set.renderType = (MasterThread.RenderType) aa.getSelectedItem();
				if (aa.getSelectedItem() == MasterThread.RenderType.NOAA) {
					spinx.setEnabled(false);
					spiny.setEnabled(false);
				} else {
					spinx.setEnabled(true);
					spiny.setEnabled(true);
				}
			}
		});
		
		this.add(aa);
		this.add(aaby);
		
		{
			JPanel panel = new JPanel();
			model = new SpinnerNumberModel(set.iters, 5, null, 1);
			JSpinner spin = new JSpinner(model);
			((JSpinner.DefaultEditor) spin.getEditor()).getTextField().setColumns(5);
			spin.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					set.iters = (Integer) spin.getValue();
				}
			});
			panel.add(new JLabel("Iterations:"));
			panel.add(spin);
			this.add(panel);
		}
		
		{
			JPanel panel = new JPanel();
			model = new SpinnerNumberModel(set.zoom, 1.0, null, 1.0);
			JSpinner spin = new JSpinner(model);
			((JSpinner.DefaultEditor) spin.getEditor()).getTextField().setColumns(5);
			spin.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					set.zoom = (Double) spin.getValue();
				}
			});
			panel.add(new JLabel("Zoom:"));
			panel.add(spin);
			this.add(panel);
		}
		
		{
			JPanel panel = new JPanel();
			model = new SpinnerNumberModel(set.xPan, 0.0, 1.0, 0.01);
			JSpinner spin = new JSpinner(model);
			JSlider slide = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (set.xPan * 100));
			((JSpinner.DefaultEditor) spin.getEditor()).getTextField().setColumns(3);
			spin.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (set.xPan == (Double) spin.getValue()) return;
					set.xPan = (Double) spin.getValue();
					slide.setValue((int) (set.xPan * 100));
				}
			});
			slide.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (set.xPan == slide.getValue() / 100.0) return;
					set.xPan = slide.getValue() / 100.0;
					spin.setValue(set.xPan);
				}
			});
			panel.add(new JLabel("x"));
			panel.add(spin);
			panel.add(slide);
			this.add(panel);
		}
		
		{
			JPanel panel = new JPanel();
			model = new SpinnerNumberModel(set.yPan, 0.0, 1.0, 0.01);
			JSpinner spin = new JSpinner(model);
			JSlider slide = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (set.yPan * 100));
			((JSpinner.DefaultEditor) spin.getEditor()).getTextField().setColumns(3);
			spin.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (set.yPan == (Double) spin.getValue()) return;
					set.yPan = (Double) spin.getValue();
					slide.setValue((int) (set.yPan * 100));
				}
			});
			slide.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (set.yPan == slide.getValue() / 100.0) return;
					set.yPan = slide.getValue() / 100.0;
					spin.setValue(set.yPan);
				}
			});
			panel.add(new JLabel("y"));
			panel.add(spin);
			panel.add(slide);
			this.add(panel);
		}
		
		JButton rd = new JButton("Recalc");
		this.add(rd);
		rd.setAlignmentX(Component.CENTER_ALIGNMENT);
		rd.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		Mandelbrot mb = sp.getMandelbrot();
		mb.setSettings(set);
		mb.forceRecalc();
	}

}
