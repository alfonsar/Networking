package alfonsar_CSCI201L_Assignment4;

import java.util.Vector;

public class GameBoard {
	public Vector<Words>myacross=new Vector<Words>();
	public Vector<Words>mydown=new Vector<Words>();
	public Vector<Words>placedWords=new Vector<Words>();
	public Vector<Pairs> posCo=new Vector<Pairs>();
	public Vector<Words>possibleW=new Vector<Words>();
	public int dSize;
	public int aSize;
	char [][] matrix;
	boolean first=true;
	boolean acrossT=true;
	public GameBoard(){
		ServerFileIO ex = new ServerFileIO();
		ex.fileIO();
		myacross=ServerFileIO.across;
		mydown=ServerFileIO.down;
		setdSize();
		setaSize();
		matrix = new char[dSize][aSize];
		for(int i=0;i<dSize;i++) {
			for(int j=0;j<aSize;j++) {
				matrix[i][j]='.';
			}
		}
	}
	//set length of board
	public void setdSize() {
		int sum=0;
		for(Words w: mydown) {
			sum+=w.word.length();
		}
		dSize=sum*2;
	}
	//set width of board
	public void setaSize() {
		int sum=0;
		for(Words w: myacross) {
			sum+=w.word.length();
		}
		aSize=sum*2;
	}
	//prints board
	public void printBoard() {
		for(int i=0;i<dSize;i++) {
			for(int j=0;j<aSize;j++) {
				System.out.print(matrix[i][j]);
			}
			System.out.println();
		}
	}
	//passes in a word and checks to see if it shares the same index
	//as one already placed
	@SuppressWarnings("unused")
	public boolean sameIndex(Words w) {
		int r=0;
		int c=0;
		for(int i=0;i<placedWords.size();i++) {
			for(int j=0;j<posCo.size();j++) {
				if((placedWords.get(i).row==posCo.get(j).row) && 
						(placedWords.get(i).col==posCo.get(j).col) 
						&& (placedWords.get(i).index!=w.index)) {
					return true;
				}
			}
		}
		return false;
	}
	//checks to see if the word trying to be placed overlaps
	public boolean overlapping(Words w) {
		int dir=w.dir;
		int r;
		int c;
		int count=0;
		if(dir==0) {
			//count keeps track of how many times word overlaps
			count=0;
			for(int i=0;i<posCo.size();i++) {
				r=posCo.get(i).row;
				c=posCo.get(i).col;
				for(int j=0;j<w.word.length();j++) {
					if(matrix[r][c+j]!='.') {
						count++;
					}
				}
			}
		}else if(dir==1) {
			count=0;
			for(int i=0;i<posCo.size();i++) {
				r=posCo.get(i).row;
				c=posCo.get(i).col;
				for(int j=0;j<w.word.length();j++) {
					if(matrix[r+j][c]!='.') {
						count++;
					}
				}
			}
		}
		//if count is greater than 1, it overlaps twice 
		//if count is 0, it is floating
		if(count!=1) {
			return false;
		}

		return true;
	}
	public boolean doubleLines(Words w, Pairs p) {
		int dir=w.dir;
		int r=0;
		int c=0;
		boolean good=true;
		if(dir==0) {
			r=p.row;
			c=p.col;
			good=true;
			for(int j=0;j<w.word.length();j++) {
				//if the current place we are at matches the same spot as our possible word, 
				//we skip that entire column because we know that this is an intersection spot
				if(matrix[r][c]==w.word.charAt(j)) {
					continue;
				}else {
					if(j==0) {
						if(matrix[r][c+j-1]!='.') {
							good=false;
						}
						if(matrix[r-1][c+j]!='.') {
							good=false;
						}
						if(matrix[r+1][c+j]!='.') {
							good=false;
						}

					}else if(j==w.word.length()-1) {
						if(matrix[r][c+j+1]!='.') {
							good=false;
						}
						if(matrix[r-1][c+j]!='.') {
							good=false;
						}
						if(matrix[r+1][c+j]!='.') {
							good=false;
						}
					}else {
						if(matrix[r-1][c+j]!='.') {
							good=false;
						}
						if(matrix[r+1][c+j]!='.') {
							good=false;
						}
					}
				}
			}
		}else if (dir==1) {
			for(int j=0;j<w.word.length();j++) {
				if(matrix[r][c]==w.word.charAt(j)) {
					continue;
				}else {
					if(j==0) {

					}
					if(matrix[r][c+j-1]!='.') {
						good=false;
					}
					if(matrix[r-1][c+j]!='.') {
						good=false;
					}
					if(matrix[r+1][c+j]!='.') {
						good=false;
					}
					else if(j==w.word.length()-1) {
						if(matrix[r][c+j+1]!='.') {
							good=false;
						}
						if(matrix[r-1][c+j]!='.') {
							good=false;
						}
						if(matrix[r+1][c+j]!='.') {
							good=false;
						}
					}else {
						if(matrix[r-1][c+j]!='.') {
							good=false;
						}
						if(matrix[r+1][c+j]!='.') {
							good=false;
						}
					}
				}
			}
		}
		return good;
	}
	public void findAllPossible(Words w) {
		String posW=w.word;
		int dir=w.dir;
		for(int r=0;r<dSize;r++) {
			for(int c=0;c<aSize;c++) {
				for(int i=0;i<posW.length();i++) {
					if(dir==0) {
						Pairs p = new Pairs(r,c-i);
						posCo.add(p);
						possibleW.add(w);
					}else if(dir==1) {
						Pairs p = new Pairs(r-i,c);
						posCo.add(p);
						possibleW.add(w);
					}
				}
			}
		}
	}
	public static void main(String args[]) {
		Board b = new Board();
		b.printBoard();
	}
}
