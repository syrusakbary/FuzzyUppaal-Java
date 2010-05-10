import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;




public class SWTView implements BasicView {

	Display display;
	Shell loader;
	Shell shell;
	String loaderImagePath = "lib/splash.png";
	String iconImagePath = "lib/icons/icon-16x16.png";
	XMLReader xmlReader;
	Image iconImage;
	private Image openIcon;
	private Image saveIcon;
	private Image saveAsIcon;
	private Image transformIcon;
	final static public String APP_NAME = "FuzzyUppaal";
	private boolean hasUnsavedChanges = false;
	protected double grade;
	protected int angle;
	final static int EXIT = 1;
	final static int LOAD = 2;
	final static int SAVE = 3;
	final static int SAVE_AS = 4;
	final static int ES = 5;
	final static int GS = 6;
	final static int LS = 7;
	protected File file;
	private Text editor;
	  private String lastOpenDirectory;
	private FuzzyLogic fuzzyLogic;
	class MenuItemListener extends SelectionAdapter {
		int id;
		public MenuItemListener (int id) {
			this.id = id;
		}
		public void widgetSelected(SelectionEvent event) {
			SWTView.this.action(this.id,event);
		}
	}
	public void action (int id, SelectionEvent event) {
		switch (id) {
		case SWTView.EXIT:
			this.exit();
			break;
		case SWTView.LOAD:
			if (handleChangesBeforeDiscard())
				this.loadTextFromFile();
			break;
		case SWTView.SAVE:
			//this.saveFile();
			this.saveTextToFile();
			break;
		case SWTView.SAVE_AS:
			this.saveTextToFile(true);
			break;
		}
	}
	boolean loadTextFromFile() {
	    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setFilterNames(new String[] { "Uppaal XML Files", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.xml","*"});
		if (lastOpenDirectory != null)
	      dialog.setFilterPath(lastOpenDirectory);

	    String selectedFile = dialog.open();
	    if (selectedFile == null) {
	      log("Action cancelled: loading the text from a file");
	      return false;
	    }

	    file = new File(selectedFile);
	    lastOpenDirectory = file.getParent();

	    try {
	      BufferedReader reader = new BufferedReader(new FileReader(file));
	      StringBuffer sb = new StringBuffer();
	      String line = null;
	      while ((line = reader.readLine()) != null) {
	        sb.append(line);
	        sb.append("\r\n");
	      }
	      editor.setText(sb.toString());
	      hasUnsavedChanges = false;
	      this.changeTitle();
	      return true;
	    } catch (IOException e) {
	      log("Failed to load the text from file: " + file);
	      log(e.toString());
	    }
	    return false;
	  }
	
	public void setXMLReader(XMLReader xmlReader) {
		this.xmlReader = xmlReader;
	}
	protected void changeTitle () {
		//fileModified;
		shell.setText((this.hasUnsavedChanges?"*":"")+(this.file != null?this.file.getName():"Untitled") + " - " + APP_NAME);
	//	System.out.println(a.getName());
	}
	protected void transform () {
		this.xmlReader.read(this.editor.getText());
		this.xmlReader.modifyNodes(this.xmlReader.getNodes("//label[@kind='guard' or @kind='invariant']"),new XMLModificator() {
			public void modifyNode(Node node) {
				node.setTextContent(fuzzyLogic.replace(XMLReader.unescape(node.getTextContent())));
				//System.out.println(XMLReader.unescape("asdfasdf &lt; asdf"));
			}
		});
		this.editor.setText(this.xmlReader.toString());
	}
	 private void log(String message) {
		    System.out.println(message);
		  }
	 boolean saveTextToFile() {
		 return saveTextToFile(false);
	 }
	 boolean saveTextToFile(boolean saveAs) {
	    if (file == null || saveAs) {
	      FileDialog dialog = new FileDialog(shell, SWT.SAVE);
	      dialog.setFilterNames(new String[] { "Uppaal XML Files"});
			dialog.setFilterExtensions(new String[] { "*.xml"});
	      if (lastOpenDirectory != null)
	        dialog.setFilterPath(lastOpenDirectory);

	      String selectedFile = dialog.open();
	      if (selectedFile == null) {

		        log("Action cancelled: saving the text to a file");
		        return false;
	      }

	      file = new File(selectedFile);

	      lastOpenDirectory = file.getParent();
	    }

	    try {
	      FileWriter writer = new FileWriter(file);
	      writer.write(editor.getText());
	      writer.close();
	      log("The text has been saved to file: " + file);

	      hasUnsavedChanges = false;
	      this.changeTitle();
	      return true;
	    } catch (IOException e) {
	      log("Failed to save the text to file: " + file);
	      log(e.toString());
	    }
	    return false;
	  }
	public SWTView () {
		this.display  = new Display();
		this.iconImage = new Image(display, iconImagePath);
		this.openIcon = new Image(display, "lib/icons/open.png");
		this.saveIcon = new Image(display, "lib/icons/save.png");
		this.saveAsIcon = new Image(display, "lib/icons/save_as.png");
		this.transformIcon = new Image(display, "lib/icons/transform.png");
	}
	protected void showLoader () {
		if (this.loader == null) {
		    final Image image = new Image(display, this.loaderImagePath);
		    final ImageData imageData = image.getImageData();
		    
			this.loader = new Shell(display, SWT.NO_TRIM | SWT.APPLICATION_MODAL);	    
			this.loader.setSize(imageData.width,imageData.height);
			this.loader.addPaintListener(new PaintListener(){
				public void paintControl(PaintEvent e) {
					e.gc.drawImage(image, imageData.x, imageData.y);
				}
			});
			this.loader.setText("Cargando...");
		}
		centerShell(this.loader);
		this.loader.setImage(this.iconImage);
		this.loader.open();
	}
	protected void hideLoader () {
		if (this.loader != null) this.loader.close();
	}
	public void centerShell (Shell shell) {
		Monitor primary = shell.getDisplay().getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = shell.getBounds();
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 2;
	    shell.setLocation(x, y);
	}
	public void exit () {
		for (Shell shell:display.getShells()) shell.dispose();
	}
	  boolean handleChangesBeforeDiscard() {
		    if (!hasUnsavedChanges)
		      return true;

		    MessageBox messageBox =
		      new MessageBox(
		        shell,
		        SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
		    messageBox.setMessage(
		      "Do you want to save the changes to "
		        + (file == null ? "a file?" : file.getName()));
		    messageBox.setText(APP_NAME);
		    int ret = messageBox.open();
		    if (ret == SWT.YES) {
		      return saveTextToFile();
		    } else if (ret == SWT.NO) {
		      return true;
		    } else {
		      return false;
		    }
		  }
	  public  void setMenu (Shell shell) {
		Menu menuBar = new Menu(shell, SWT.BAR);
		MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&Archivo");
		
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);
		MenuItem fileLoadItem = new MenuItem(fileMenu, SWT.PUSH);
		fileLoadItem.setText("&Abrir");
		fileLoadItem.setImage(openIcon);
		fileLoadItem.setAccelerator(SWT.CTRL+'O');
		fileLoadItem.addSelectionListener(new MenuItemListener(SWTView.LOAD));
		new MenuItem(fileMenu, SWT.SEPARATOR);
		MenuItem fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveItem.setText("&Guardar");
		fileSaveItem.setImage(saveIcon);
		fileSaveItem.setAccelerator(SWT.CTRL+'S');
		fileSaveItem.addSelectionListener(new MenuItemListener(SWTView.SAVE));
		MenuItem fileSaveAsItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveAsItem.setText("G&uardar como..");
		fileSaveAsItem.setImage(saveAsIcon);
		fileSaveAsItem.setAccelerator(SWT.CTRL+'A');
		fileSaveAsItem.addSelectionListener(new MenuItemListener(SWTView.SAVE_AS));
		new MenuItem(fileMenu, SWT.SEPARATOR);	 
		MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("&Salir");
		fileExitItem.addSelectionListener(new MenuItemListener(SWTView.EXIT));	    
	    
		shell.setMenuBar(menuBar);
	}
	protected void setGrade (double grade) {
		this.grade = grade;
		this.fuzzyLogic.setGrade((float) grade);
	}
	protected void setAngle (int angle) {
		this.angle = angle;
		this.fuzzyLogic.setAngle(angle);
	}
	public void windowBasic () {
		
		shell = new Shell(display);
		shell.setImage(iconImage);
		shell.setText(APP_NAME);
		shell.setSize(600,400);
		centerShell(shell);
		setMenu(shell);
	    
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		
		shell.setLayout(layout);
		GridData gridData;
		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		toolBar.setLayoutData(gridData);

	    
	    ToolItem openButton = new ToolItem(toolBar, SWT.PUSH);
	    openButton.setImage(openIcon);
	    openButton.setToolTipText("Abrir archivo...");
	    openButton.addSelectionListener(new MenuItemListener(SWTView.LOAD));

	    ToolItem saveButton = new ToolItem(toolBar, SWT.PUSH);
	    saveButton.setImage(saveIcon);
	    saveButton.setToolTipText("Guardar");
	    saveButton.addSelectionListener(new MenuItemListener(SWTView.SAVE));

	    new ToolItem(toolBar, SWT.SEPARATOR);
	    
	    ToolItem ltButton = new ToolItem(toolBar, SWT.PUSH);
	    
	    ltButton.setText("<~");
	    ltButton.setToolTipText("Menor o similar a");
	    ltButton.addSelectionListener(new MenuItemListener(SWTView.LS));
	    
	    ToolItem gtButton = new ToolItem(toolBar, SWT.PUSH);
	    //boldButton.setImage(display.getSystemImage(SWT.ICON_WARNING));
	    gtButton.setText(">~");
	    gtButton.setToolTipText("Mayor o similar a");
	    gtButton.addSelectionListener(new MenuItemListener(SWTView.GS));	    

	    ToolItem eqButton = new ToolItem(toolBar, SWT.PUSH);
	    eqButton.setText("=~");
	    eqButton.setToolTipText("Similar a");
	    eqButton.addSelectionListener(new MenuItemListener(SWTView.ES));
	    
	    Group fuzzyOptions = new Group(shell, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		fuzzyOptions.setLayoutData(gridData);
		fuzzyOptions.setText("Opciones");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		fuzzyOptions.setLayout(gridLayout);
		
		new Label(fuzzyOptions, SWT.NONE).setText("Ángulo:");
		final Spinner spinner = new Spinner (fuzzyOptions, SWT.BORDER);
		spinner.setMinimum(0);
		spinner.setMaximum(90);
		spinner.setSelection(45);
		spinner.setIncrement(1);
		spinner.setPageIncrement(15);
		spinner.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		        int selection = spinner.getSelection();
		        setAngle(selection);
		      }
		    });
		spinner.pack();
		
		new Label(fuzzyOptions, SWT.NONE).setText("Grado:");
		final Spinner spinner2 = new Spinner (fuzzyOptions, SWT.BORDER);
		spinner2.setMinimum(1);
		spinner2.setDigits(2);
		spinner2.setSelection(50);
		spinner2.setMaximum(100);
		spinner2.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		        int selection = spinner2.getSelection();
		        int digits = spinner2.getDigits();
		        double grade = selection / Math.pow(10, digits);
		        setGrade(grade);
		      }
		    });
		spinner2.pack();
		Button transform = new Button(fuzzyOptions, SWT.PUSH);
		transform.setText("Transformar");
		transform.setImage(transformIcon);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.horizontalSpan = 2;
		transform.setLayoutData(gridData);
		transform.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				transform();
			}
		});
		editor = new Text(shell, SWT.MULTI
		          | SWT.WRAP
		          | SWT.BORDER
		          | SWT.H_SCROLL
		          | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		editor.setLayoutData(gridData);
		editor.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				hasUnsavedChanges = true;

			      changeTitle();
			}
		    });
		this.display.timerExec(800, new Runnable() {
			public void run () {
				hideLoader();
			}
		});




		shell.open();
	}
	public void viewBasic() {
		showLoader();
		this.display.asyncExec(new Thread() {
			public void run () {
				windowBasic();
			}
		});
	}
	public void end () {
	    while (display.getShells().length > 0) {
	        if (!display.readAndDispatch()) display.sleep();
	    }
	    display.dispose();		
	}
	@Override
	public void setFuzzyLogic(FuzzyLogic fuzzyLogic) {
		this.fuzzyLogic = fuzzyLogic;
	}
}
