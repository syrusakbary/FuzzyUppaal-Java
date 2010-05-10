

public class View {
	final static int SWT = 1000;
	final static int Swing = 1001;
	public View (int view) {
		

		BasicView v;
		
		switch (view) {
		default:
		case View.SWT:
			v = new SWTView();
			break;
		/*case View.Swing:
			v = new SWTView(world,colors);
			break;*/
		}
		v.setXMLReader(new XMLReader());
		v.setFuzzyLogic(new FuzzyLogic());
		v.viewBasic();
		v.end();
	}
	public static void main(String[] args) {
		new View(View.SWT);
	}
}
