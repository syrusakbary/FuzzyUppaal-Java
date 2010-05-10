import java.awt.Color;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FuzzyLogic {
	/*private HashMap<String,MinMax> vars = new HashMap<String,MinMax>();
	public class MinMax {
		public Double lower=null;
		public Double upper=null;
		public void lowerThan (double lower) {
			this.lower = this.lower==null||lower<this.lower?lower:this.lower;
		}
		public void upperThan (double upper) {
			this.upper = this.upper==null||upper>this.upper?upper:this.upper;
		}
	}*/
	float grade = 0.5f;
	double angle = Math.PI/4;
	public double getVariation () {
		return this.getVariation(this.grade);
	}
	public double getVariation (double grade) {
		return Math.tan(Math.PI/2-angle)*(1d/2)*grade;
	}
	public void setGrade (float grade) {
		this.grade = grade;
	}
	public void setAngle (int angle) {
		this.angle = Math.PI/180*angle;
	}
	public String replace (String str) {
		String newStr = "";
		int init = 0;
		Pattern pattern = Pattern.compile("(\\w+)\\s*(\\<|\\>|=)~\\s*([0-9\\.]+)");
		Matcher patternMatcher = pattern.matcher(str);
		while (patternMatcher.find()) {
			
			String var;
			double restriction;
			char operation;
			var = patternMatcher.group(1);
			operation = patternMatcher.group(2).charAt(0);
			restriction = Double.valueOf(patternMatcher.group(3));
			//if (!this.vars.containsKey(var)) this.vars.put(var, new MinMax());
			//MinMax minMax = this.vars.get(var)?this.vars.;
			/*System.out.println(digitLeft);
			System.out.println(operation);
			System.out.println(digitRight);*/
			//System.out.println(str.substring(init,patternMatcher.start()));
			newStr += str.substring(init,patternMatcher.start());
			//System.out.println(newStr);
			//System.out.println(20+((int)(20/this.getVariation(1d)+1)-1));
			//System.out.println(patternMatcher.group(1));
			switch (operation) {
			case '<':
				newStr += var+"<="+(restriction+this.getVariation());
				//this.vars.get(var).lowerThan(Double.valueOf(restriction));
				break;
			case '>':
				newStr += var+">="+(restriction-this.getVariation());
				//this.vars.get(var).upperThan(Double.valueOf(restriction));
				break;
			case '=':
				newStr += " "+var+"<="+(restriction+this.getVariation())+" && "+var+">="+(restriction-this.getVariation())+" ";
				break;
			}

			init = patternMatcher.end();
		}
		newStr += str.substring(init,str.length());
		return newStr.trim();
	}
	public static void main(String[] args) {
		//FuzzyLogic fz = new FuzzyLogic();
		
		//System.out.println("********"+fz.replace("y <~ 3"));
	}
}
