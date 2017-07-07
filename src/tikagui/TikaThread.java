package tikagui;

import tikagui.TikaGUI;
import java.io.File;
import javax.swing.WindowConstants;

public class TikaThread implements Runnable{
	
	private File file;
	
	TikaThread(File file)
	{
		this.file = file;
	}
	
	@Override
	public void run()
	{
		TikaGUI f = new TikaGUI();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.open(file);
		
	}
	
}
