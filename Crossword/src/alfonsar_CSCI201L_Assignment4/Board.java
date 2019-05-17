package alfonsar_CSCI201L_Assignment4;

import java.util.Vector;

public class Board {
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
	public Board(){
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
	public void printBoard() {
		for(int i=0;i<dSize;i++) {
			for(int j=0;j<aSize;j++) {
				System.out.print(matrix[i][j]);
			}
			System.out.println();
		}
	}
	public boolean outOfBoundsD(String word,int row, int col) {
		int len = word.length();
		if(row>=0 && (row+len)<dSize && col>=0 && col<aSize) {
			return true;
		}
		return false;
	}
	public boolean outOfBoundsA(String word,int row, int col) {
		int len = word.length();
		if(row>=0 && row<dSize && col>=0 && (col+len)<aSize) {
			return false;
		}
		return true;
	}
	public void placeDown(Words attempt, int row, int col) {
		String word=attempt.word;
		int len=word.length();
		for(int i=0;i<len;i++) {
			matrix[row+i][col]=word.charAt(i);
		}
	}
	public void placeAcross(Words attempt, int row, int col) {
//		System.out.println("row: "+row +"col: "+col);
//		placedWords.add(attempt);
		String word=attempt.word;
		int len=word.length();
		try {
			for(int i=0;i<len;i++) {
				matrix[row][col+i]=word.charAt(i);
			}
			//findTheOne(attempt);
		}catch(ArrayIndexOutOfBoundsException ioe){
			System.out.println("OUT OF BOUNDS");
		}
		
	}
	//check to see if it has the same coordinates and index
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
	public boolean findTheOne() {
//		if(sameIndex()==true) {
//			placeAcross(possibleW.get(0),possibleW.get(0).row,possibleW.get(0).col);
//			return true;
//		}else {
//			
//		}
		return true;
	}
	public boolean connectedLR(String word, int row, int col) {
		int len=word.length();
		return false;
	}
	public boolean possiblePlace(Words attempt, int dir) {
		String word=attempt.word;
		for(int r=0;r<dSize;r++) {
			for(int c=0;c<aSize;c++) {
				if(matrix[r][c]!='.') {
					for(int i=0;i<word.length();i++) {
						if(matrix[r][c]==word.charAt(i)) {
							//across
							if(dir==0) {
								Pairs p = new Pairs(r,c-i);
								posCo.add(p);
								possibleW.add(attempt);
								if(findTheOne()) {
									return true;
								}
							}
							//down
							else if(dir==1) {
								Pairs p = new Pairs(r-i,c);
								posCo.add(p);
								possibleW.add(attempt);
								if(findTheOne()) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	public boolean makeBoard() {
		
		if(first) {
			Words fWord=mydown.get(0);
			//String fw=fWord.word;
				placeDown(fWord,dSize/2,aSize/2);
			mydown.remove(0);
			fWord.row=dSize/2;
			fWord.col=aSize/2;
			placedWords.add(fWord);
			first=false;
			makeBoard();
		}else {
			if(acrossT==true) {
				if(!myacross.isEmpty()) {
					Words attempt = myacross.get(0);
					if(possiblePlace(attempt,0)) {
					myacross.remove(0);
					posCo.clear();
					possibleW.clear();
					acrossT=false;
					makeBoard();
					}
				}
			}else if(acrossT==false) {
				if(!mydown.isEmpty()) {
					Words attempt = mydown.get(0);
					if(possiblePlace(attempt,1)) {
						mydown.remove(0);
						posCo.clear();
						possibleW.clear();
						acrossT=true;
						makeBoard();
					}
					
				}
			}
		}
		return true;
	}
	public static void main(String args[]) {
		Board b = new Board();
		b.makeBoard();
		b.printBoard();
	}
}

