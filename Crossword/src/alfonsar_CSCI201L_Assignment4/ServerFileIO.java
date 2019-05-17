package alfonsar_CSCI201L_Assignment4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;



public class ServerFileIO {
	public  static Vector<Words> across = new Vector<Words>();
	public  static Vector<Words> down = new Vector<Words>();
	public boolean parse(String dir,String line) {
		StringTokenizer s1 = new StringTokenizer(line,"|");
		String reader = s1.nextToken();
		int i;
		String w;
		String q;
		try {
			
			i = Integer.parseInt(reader);
			w = s1.nextToken();
			q = s1.nextToken();
		}catch(Exception e) {
			return false;
		}

		Words word = new Words();
		word.index=i;
		word.word=w;
		word.question=q;
		if(dir.equals("ACROSS")) {
			word.dir=0;
			across.add(word);
		}else if(dir.equals("DOWN")) {
			word.dir=1;
			down.add(word);
		}
		return true;
	}
	public boolean fileIO() {
		try {
			File dir = new File("/Users/alfonsorojas/Desktop/csci201/alfonsar_CSCI201L_Assignment4/gamedata");
	        String[] fi = dir.list();
	        Collections.shuffle(Arrays.asList(fi));
			String filename = "/Users/alfonsorojas/Desktop/csci201/alfonsar_CSCI201L_Assignment4/gamedata/"+fi[0];
			FileReader fr = new FileReader(filename);
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			if(line==null) {
				return false;
			}else if(!line.equals("DOWN")&&!line.equals("ACROSS")) {
				return false;
			}else {
				if(line.equals("ACROSS")){
					line=br.readLine();
					while(line!=null) {
						if(line.equals("DOWN")) break;
						if(parse("ACROSS",line)==false) {
							return false;
						}
						line=br.readLine();
					}
				
				}
				 if(line.equals("DOWN")) {
					line=br.readLine();
					while(line!=null) {
						if(line.equals("ACROSS")) break;
						if(parse("DOWN",line)==false) {
							return false;
						}
						line=br.readLine();
	 				}
				}			
			}
			
		}catch (FileNotFoundException fnfe) {
			//System.out.println("File not found");
			return false;
		}  
		catch (IOException ioe) {
			//System.out.println("Format of file is incorrect");
			return false;
		} catch(NullPointerException npe) {
			//System.out.println("Null pointer");
			return false;
		} catch(NoSuchElementException nse) {
			//System.out.println("No such element");
			return false;

		} 
		return true;
	}
//	public static void main (String args[]) {
//		ServerFileIO ex = new ServerFileIO();
//		System.out.println(ex.fileIO());
//		System.out.println("ACROSS");
//		for(int i=0;i<ex.across.size();i++) {
//			System.out.println(ex.across.get(i).word);
//		}
//		System.out.println("\nDOWN");
//		for(int i=0;i<ex.down.size();i++) {
//			System.out.println(ex.down.get(i).word);
//		}
//	}

}
