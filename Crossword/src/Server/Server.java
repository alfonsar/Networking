package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import alfonsar_CSCI201L_Assignment4.ServerFileIO;
import alfonsar_CSCI201L_Assignment4.Words;

public class Server {
	public Vector<ServerThread> serverThreads;
	private Vector<Lock>locks;
	private Vector<Condition> conditions;	
	public  boolean firstPlay=false;
	int numPlayers=0;
	int curr=1;
	Scanner scan = new Scanner(System.in);
	Lock gamerLock=null;
	Condition gamerCondition=null;
	Vector<Words> allAcross=new Vector<Words>();
	Vector<Words> allDown=new Vector<Words>();
	Vector<Words> myAcross=new Vector<Words>();
	Vector<Words> myDown=new Vector<Words>();
	ServerSocket sock =null;
	boolean gameOver=false;
	Socket s = null; 
	public  String[][] finalBoard;
	public  String[][] turnBoard;
	public Server(){

	}
	public void constructor2(int port) {
		sock=null;
		try {
			gamerLock = new ReentrantLock();
			gamerCondition = gamerLock.newCondition();
			sock = new ServerSocket(port); 
			System.out.println("Listening on port "+ port);
			serverThreads = new Vector<ServerThread>();
			locks = new Vector<Lock>();
			conditions = new Vector<Condition>();
			System.out.println("Waiting for players...\n");
			System.out.println();
			System.out.println();
			s = sock.accept(); //blocking waiting for players
			Lock lock = new ReentrantLock();
			Condition connectionAccepted = lock.newCondition();
			locks.add(lock);
			conditions.add(connectionAccepted);
			System.out.println("Connection from: " + s.getInetAddress());
			System.out.println();
			numPlayers++;
			firstPlay=false;
			if(serverThreads.size()==0) {
				firstPlay=true;
			}
			ServerThread st = new ServerThread(s,this, lock, connectionAccepted,firstPlay);
			serverThreads.add(st);

		}catch(IOException ioe) {
			System.out.println("ioe:"+ioe.getMessage());
		}
	}
	public void setPlayers(int p) {
		numPlayers=p;
		System.out.println("Number of players: "+numPlayers);
		for(int i=2;i<=numPlayers;i++) {
			System.out.println("Waiting for player "+ i);
		}
		callingBoard();
	}
	public int currentPlayers() {
		curr++;
		return curr;
	}
	public void callingBoard() {
		System.out.println("Reading random game file");
		//call the randomized game file function
		ServerFileIO ex = new ServerFileIO();
		boolean goodFile=ex.fileIO();
		if(goodFile==false) {
			System.out.println("No valid file found. Goodbye.");
			System.exit(0);
		}
		System.out.println("File read successfully\n");
		System.out.println();
		setVectors();
		//now actually read it
		//once you are done, you will call waiting on players
		waitingOnPlayers();
	}
	public void setVectors() {
		allAcross.addAll(ServerFileIO.across);
		allDown.addAll(ServerFileIO.down);
		myAcross.addAll(ServerFileIO.across);
		myDown.addAll(ServerFileIO.down);
	}
	public void waitingOnPlayers() {
		int count=1;
		try {

			while(count<numPlayers) {
				s = sock.accept(); //blocking waiting for players
				Lock lock = new ReentrantLock();
				Condition connectionAccepted = lock.newCondition();
				locks.add(lock);
				conditions.add(connectionAccepted);
				System.out.println("Connection from: " + s.getInetAddress());
				System.out.println();
				count++;
				firstPlay=false;
				ServerThread st = new ServerThread(s,this, lock, connectionAccepted,firstPlay);
				serverThreads.add(st);
			}
			gameStarting();
		}catch(IOException ioe) {
			System.out.println("ioe:"+ioe.getMessage());
		}
	}
	public void gameStarting() {
		hardBoard();
		System.out.println("Game can now begin\n");
		System.out.println("Sending game board");
	}
	public void broadcast(String message, ServerThread st) {
		if (message != null) {
			for(ServerThread threads : serverThreads) {
				if (st != threads) {
					threads.sendMessage(message);
				}
			}
			if(message.equals("START")){

			}else if(message.equals("GAMEWAITING")){

			}else if(message.contains("127.0.0.1")){

			}else {
				System.out.println(message);
			}
		}
	}
	public void broadcastAll(String message) {
		if (message != null) {
			for(ServerThread threads : serverThreads) {
				threads.sendMessage(message);
			}

		}
	}
	public void sendingFinal() {
		System.out.println();
		System.out.println("\nThe game has concluded");
		System.out.println("Sending scores.");
		displayAllQuestions();
		sendScores();
	}
	public void sendScores() {
		broadcastAll("\nFinal Score.");
		int max=-5;
		for(int i=0;i<serverThreads.size();i++) {
			if(max<serverThreads.get(i).score) {
				max=serverThreads.get(i).score;
			}
			broadcastAll("Player "+(i+1)+" - "+serverThreads.get(i).score+" correct answers.");
		}
		int player=0;
		int occurrences=0;
		for(int i=0;i<serverThreads.size();i++) {
			if(max==serverThreads.get(i).score) {
				occurrences++;
				player=(i+1);
			}
		}
		if(occurrences==1) {
			broadcastAll("\nPlayer "+player+" is the winner");

		}else {
			broadcastAll("\nThere is a tie.");
		}
	}
	public void displayAllQuestions() {
		broadcastAll("\nAcross");
		for(int i=0;i<myAcross.size();i++) {
			String que=myAcross.get(i).index + " "+ myAcross.get(i).question;
			broadcastAll(que);
		}
		broadcastAll("Down");
		for(int i=0;i<myDown.size();i++) {
			String que=myDown.get(i).index + " "+ myDown.get(i).question;
			broadcastAll(que);
		}
	}
	public boolean isEmp() {
		boolean empty=false;
		int count=allAcross.size()+allDown.size();
		if(count==0) {
			empty=true;
		}
		return empty;
	}
	public void callSignal (ServerThread st) {
		int serverNumber = 0; 
		for (int i = 0; i < serverThreads.size(); i++) {
			if (serverThreads.get(i) == st) {
				serverNumber = i;
				break;
			}
		}
		if (serverNumber == (serverThreads.size() - 1)) {
			serverNumber = 0;
		}
		else {
			serverNumber++; 
		}
		locks.get(serverNumber).lock();
		conditions.get(serverNumber).signal();
		locks.get(serverNumber).unlock();
	}
	public void switchSignal() {
		gamerLock.lock();
		gamerCondition.signal();
		gamerLock.unlock();
	}
	public void takeTurns(ServerThread st) {
		int serverNumber=0;
		for(int i=0;i<serverThreads.size();i++) {
			if(serverThreads.get(i)==st) {
				serverNumber=i;
			}
		}
		broadcast("\nPlayer "+(serverNumber+1)+"'s turn.",st);
	}
	public void correctPlayer(String mes,ServerThread st) {
		int serverNumber=0;
		for(int i=0;i<serverThreads.size();i++) {
			if(serverThreads.get(i)==st) {
				serverNumber=i;
			}
		}
		broadcast("\nPlayer "+(serverNumber+1)+" "+mes+".",st);
		System.out.println("\nSending game board.");
	}
	public void displayQuestions() {

		if(allAcross.size()>0) {
			broadcastAll("Across");
			for(int i=0;i<allAcross.size();i++) {
				broadcastAll(allAcross.get(i).index + " "+ allAcross.get(i).question);
			}
		}
		if(allDown.size()>0){
			broadcastAll("Down");
			for(int i=0;i<allDown.size();i++) {
				broadcastAll(allDown.get(i).index + " " + allDown.get(i).question);
			}
		}
	}
	public void displayBoard() {
		for(int r=0;r<13;r++) {
			for(int c=0;c<12;c++) {
				broadcastBoard(" "+turnBoard[r][c]+" ");
			}
			broadcastBoard("\n ");
		}
	}
	public void broadcastBoard(String mes) {
		if (mes != null) {
			for(ServerThread threads : serverThreads) {
				threads.sendMessage1(mes);
			}

		}
	}
	public boolean validNum(int index, String dir) {
		if(dir.equals("a")) {
			for(int i=0;i<allAcross.size();i++) {
				if(index==allAcross.get(i).index) {
					return true;
				}
			}
		}else if (dir.equals("d")){
			for(int i=0;i<allDown.size();i++) {
				if(index==allDown.get(i).index) {
					return true;
				}
			}
		}
		return false;
	}
	public String askQuestions(ServerThread st) {
		st.sendMessage("Would you like to answer a question across (a) or down (d)? ");
		String ab = scan.nextLine();
		while(!ab.equalsIgnoreCase("a")&& !ab.equalsIgnoreCase("d")) {
			System.out.println("That is not a valid option");
			System.out.println("Would you like to answer a question across (a) or down (d)? ");
			ab = scan.nextLine();
		}
		int num;
		if(ab.equalsIgnoreCase("a")) {
			System.out.println("Which number? ");
			num=Integer.parseInt(scan.nextLine());
			int check=-1;
			for(int i=0;i<allAcross.size();i++) {
				if(allAcross.get(i).index==num) {
					check=allAcross.get(i).index;
				}
			}
			while(check==-1) {
				System.out.println("That is not a valid option.");
				System.out.println("Which number? ");
				num=Integer.parseInt(scan.nextLine());
				for(int i=0;i<allAcross.size();i++) {
					if(allAcross.get(i).index==num) {
						check=allAcross.get(i).index;
					}
				}

			}
			String dis=findAnswer(ab,num);
			return dis;

		}else if(ab.equalsIgnoreCase("d")) {
			System.out.println("Which number? ");
			num=Integer.parseInt(scan.nextLine());
			int check=-1;
			for(int i=0;i<allDown.size();i++) {
				if(allDown.get(i).index==num) {
					check=allDown.get(i).index;
				}
			}
			while(check==-1) {
				System.out.println("That is not a valid option.");
				System.out.println("Which number? ");
				num=Integer.parseInt(scan.nextLine());
				for(int i=0;i<allDown.size();i++) {
					if(allDown.get(i).index==num) {
						check=allDown.get(i).index;
					}
				}

			}
			String dis=findAnswer(ab,num);
			return dis;
		}
		return "";
	}
	public String findAnswer(String ans, int choice) {
		if(ans.equalsIgnoreCase("a")) {
			System.out.println("What is your guess for "+choice+" across? ");
			String input=scan.nextLine();
			for(int i=0;i<allAcross.size();i++) {
				if(choice==allAcross.get(i).index) {
					String word=allAcross.get(i).word;
					if(checkAnswer(word,input)) {
						return input;
						//remove(word,0)
						//return "guessed '"+input+"' for "+choice+" across. That is correct";
					}
					else {
						return "INCORRECT-"+choice+"-ACROSS-"+input;
						//return "guessed '"+input+"' for "+choice+" across. That is incorrect";
					}
				}
			}
		}else {
			System.out.println("What is your guess for "+choice+" down? ");
			String input=scan.nextLine();
			for(int i=0;i<allDown.size();i++) {
				if(choice==allDown.get(i).index) {
					String word = allDown.get(i).word;
					if(checkAnswer(word,input)) {
						return input;
						//remove(word,1);
						//return "guessed '"+input+"' for "+choice+" down. That is correct";				
					}else {
						return "INCORRECT-"+choice+"-DOWN-"+input;
						//return "guessed '"+input+"' for "+choice+" down. That is incorrect";

					}
				}
			}
		}
		return "";
	}
	public boolean checkAnswer(String word,String input) {
		if(input.equalsIgnoreCase(word)) {
			return true;
		}
		return false;
	}

	public boolean remove(String word,int index) {
		//Pairs p = new Pairs(0,0);
		String[] parts=word.split("-");
		word=parts[0];
		if(parts[1].equals("ACROSS")) {
			for(int i=0;i<allAcross.size();i++) {
				if(index==allAcross.get(i).index) {
					if(word.equalsIgnoreCase(allAcross.get(i).word)) {
						allAcross.remove(i);
						return true;
					}

				}
			}
		}
		else if(parts[1].equals("DOWN")) {
			for(int i=0;i<allDown.size();i++) {
				if(index==allDown.get(i).index) {
					if(word.equalsIgnoreCase(allDown.get(i).word)) {
						allDown.remove(i);
						return true;
					}

				}
			}
		}

		return false;
	}
	public void hardBoard() {
		finalBoard = new String[][]{
			{" "," ", " ", " ", " ", " ", " ", " ", "5", "M", " ", " " },
			{" "," ", " ", " ", " ", " ", " ", " ", " ", "A", " ", " " },
			{" "," ", " ", " ", " ", " ", " ", " ", " ", "R", " ", " " },
			{" "," ", " ", " ", " ", " ", " ", "3", "C", "S", "C", "I" },
			{" "," ", " ", " ", " ", " ", "4", "G", " ", "H", " ", " " },
			{" "," ", " ", " ", "1", "T", "R", "O", "J", "A", "N", "S" },
			{" "," ", " ", " ", " ", "R", " ", "L", " ", "L", " ", " " },
			{" "," ", " ", " ", " ", "A", " ", "D", " ", "L", " ", " " },
			{" "," ", " ", " ", " ", "V", " ", " ", " ", " ", " ", " " },
			{" "," ", " ", " ", " ", "E", " ", " ", " ", " ", " ", " " },
			{" "," ", " ", " ", " ", "L", " ", " ", " ", " ", " ", " " },
			{"2","D", "O", "D", "G", "E", "R", "S", " ", " ", " ", " " },
			{" "," ", " ", " ", " ", "R", " ", " ", " ", " ", " ", " " }
		};

		turnBoard = new String[][] {
			{" "," ", " ", " ", " ", " ", " ", " ", "5", "-", " ", " " },
			{" "," ", " ", " ", " ", " ", " ", " ", " ", "-", " ", " " },
			{" "," ", " ", " ", " ", " ", " ", " ", " ", "-", " ", " " },
			{" "," ", " ", " ", " ", " ", " ", "3", "-", "-", "-", "-" },
			{" "," ", " ", " ", " ", " ", "4", "-", " ", "-", " ", " " },
			{" "," ", " ", " ", "1", "-", "-", "-", "-", "-", "-", "-" },
			{" "," ", " ", " ", " ", "-", " ", "-", " ", "-", " ", " " },
			{" "," ", " ", " ", " ", "-", " ", "-", " ", "-", " ", " " },
			{" "," ", " ", " ", " ", "-", " ", " ", " ", " ", " ", " " },
			{" "," ", " ", " ", " ", "-", " ", " ", " ", " ", " ", " " },
			{" "," ", " ", " ", " ", "-", " ", " ", " ", " ", " ", " " },
			{"2","-", "-", "-", "-", "-", "-", "-", " ", " ", " ", " " },
			{" "," ", " ", " ", " ", "-", " ", " ", " ", " ", " ", " " }
		};

	}
	public void placeWord(String dir, String word, int index) {
		String in=Integer.toString(index);
		for(int r=0;r<13;r++) {
			for(int c=0;c<12;c++) {
				if(turnBoard[r][c].equals(in)) {
					for(int i=0;i<word.length();i++) {
						if(dir.equalsIgnoreCase("a")) {
							turnBoard[r][c+1+i]=Character.toString(word.charAt(i));
						}else if(dir.equalsIgnoreCase("d")) {
							if(turnBoard[r][c+1]!=" ") {
								turnBoard[r+i][c+1]=Character.toString(word.charAt(i));
							}else {
								turnBoard[r+1+i][c]=Character.toString(word.charAt(i));
							}
						}
					}
				}
			}
		}
	}
	public boolean checkNum(String mes, String dir) {
		int n=0;
		try {
			n=Integer.parseInt(mes);
			if(dir.equals("a")) {
				for(int i=0;i<allAcross.size();i++) {
					if(n==allAcross.get(i).index) {
						return true;
					}
				}
			}else if(dir.equals("d")) {
				for(int i=0;i<allDown.size();i++) {
					if(n==allDown.get(i).index) {
						return true;
					}
				}
			}
		}catch(NumberFormatException e) {
			return false;
		}
		return false;
	}
	public static void main(String args[]) {
		//while(true) {
			Server serve = new Server();
			serve.constructor2(3456);
			//while(serve.serverThreads.get(0)!=null) {

			//}
			//serve.sock=null;
			//serve = new Server();
			//serve.constructor2(3456);
		//}

	}
}
