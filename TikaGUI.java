package tikagui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.prefs.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.JTextComponent;
//import java.text.NumberFormat;

import org.apache.tika.Tika;

public class TikaGUI extends JFrame{
	
	private static final long serialVersionUID = 1L;
	//private final JFrame frame = new JFrame();
	private final JLabel fileName = new JLabel();
	//private final JLabel mimeType = new JLabel();
	private final JTextArea plain = new JTextArea();
	//private final String saveName = "test.txt";
	
	public TikaGUI()
	{
		this.setTitle("Tika GUI");
		
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		
		fileName.setFont(fileName.getFont().deriveFont(17f));
		fileName.setText("Tika Parser (DRAG one or more files and DROP to this box!)");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(fileName, c);
		//this.add(c);
		
		plain.setEditable(false);
		plain.setLineWrap(true);
		c.gridx = 0;		
		c.gridy = 1;
		c.gridwidth = 1;	
		c.gridheight = 1;
		c.weightx = 1.0;	
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		this.add(new JScrollPane(plain), c);
		//this.add(c);
		
		TransferHandler h = new TransferHandler(){
			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean canImport(TransferSupport support) {
				return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}
			
			@Override
			public boolean importData(TransferSupport support) {
				if(! canImport(support)){
					return false;
				}
				Transferable t = support.getTransferable();
				try {
					@SuppressWarnings("unchecked")
					List<File> list = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
					for(File file : list)
					{
						if (file == list.get(0))
						{
							open(file);
						}
						else
						{
							Thread td = new Thread(new TikaThread(file));
							td.start();
						}
					}
					//dispose()
					//open(list);
				} catch(IOException ex){
					alert("Fail!!", ex);
				} catch(UnsupportedFlavorException ex){
					alert("Fail!!", ex);
				}
				return true;
			}

			@Override
			public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
				if(comp instanceof JTextComponent){
					JTextComponent t = (JTextComponent)comp;
					String text = t.getSelectedText();
					Transferable tf = new StringSelection(text);
					clip.setContents(tf, null);
				}
				return;
			}
		};
		
		fileName.setTransferHandler(h);
		//mimeType.setTransferHandler(h);
		plain.setTransferHandler(h);

		this.pack();

		Preferences pref = Preferences.userNodeForPackage(TikaGUI.class);
		Rectangle area = getBounds();
		this.setBounds(
			pref.getInt("window.x", area.x),
			pref.getInt("window.y", area.y),
			pref.getInt("window.width", area.width),
			pref.getInt("window.height", area.height));
		int state = pref.getInt("window.state", NORMAL) & ~ICONIFIED;
		this.setExtendedState(state);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		return;
	}

	private static void saveFile(String content, String saveName) {  
	  
		FileWriter fwriter = null;  
		try {  
			fwriter = new FileWriter(saveName);  
			fwriter.write(content);  
			} catch (IOException ex) {  
				ex.printStackTrace();  
			} finally {  
				try {  
					fwriter.flush();  
					fwriter.close();  
				} catch (IOException ex) {  
					ex.printStackTrace();  
				}  
			}  
	} 
	
	public void open(File file) {

		Tika tika = new Tika();
		tika.setMaxStringLength(999999);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		fileName.setText(file.getName());
		int dot = file.getName().lastIndexOf('.');
		String saveName = file.getName().substring(0, dot) + ".txt";
		try {
			//long start = System.currentTimeMillis();
			String text = tika.parseToString(file);
			//long end = System.currentTimeMillis();
			pw.println(text);
			//mimeType.setText(tika.detect(file) + " (" + NumberFormat.getNumberInstance().format(end-start) + "ms)");
		} catch(Exception ex){
			ex.printStackTrace(pw);
		}
		
		pw.flush();

		plain.setText(sw.toString());
		saveFile(sw.toString(), saveName);
		plain.setCaretPosition(0);
		return ;
	}


	private void exit(){

		int state = getExtendedState();
		Preferences pref = Preferences.userNodeForPackage(TikaGUI.class);
		pref.putInt("window.state", state);

		if((state & MAXIMIZED_BOTH) != 0){
			setExtendedState(state & ~MAXIMIZED_BOTH);
		}
		Rectangle area = getBounds();
		pref.putInt("window.x", area.x);
		pref.putInt("window.y", area.y);
		pref.putInt("window.width", area.width);
		pref.putInt("window.height", area.height);

		try {
			pref.flush();
		} catch(BackingStoreException ex){
			alert("Fail!!", ex);
		}
		return;
	}


	private void alert(String msg, Throwable ex){
		JOptionPane.showMessageDialog(this, msg, "ALERT", JOptionPane.ERROR_MESSAGE);
		return;
	}

	
	public static void main(String[] args) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run(){
				JFrame f = new TikaGUI();
				f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				f.setVisible(true);
				return;
			}
		});
		return;
	}
	
}

