package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import alfonsar_CSCI201L_Assignment4.ServerFileIO;

public class PlayerThread extends Thread {
	private BufferedReader br;
	private PrintWriter pw;
	Scanner scan = new Scanner(System.in);
	public PlayerThread(String host, int port) {
		
		try {
			Socket s = new Socket(host,port);
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
			this.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void run() {
		try {
			while(true) {
				String line = br.readLine();
				int n=0;
				if(line==null) {
					
				}
				else if(line.equals("PLAYERS")){
					System.out.print("\nHow many players will there be? ");
					String num = scan.nextLine();
					n = Integer.parseInt(num);
					for(int i=2;i<=n;i++) {
						System.out.println("\nWaiting for player "+i+".");
					}
					sendMessage(num);
				}else if(line.contains("GAMEWAITING")){
					String[] s=line.split("-");
					int curr=Integer.parseInt(s[1]);
					int tot=Integer.parseInt(s[2]);
					for(int i=curr;i<tot;i++) {
						System.out.println("\nWaiting for player "+(i+1)+".");
					}
					System.out.println("\nThere is a game waiting for you.");
					for(int i=1;i<curr;i++) {
						System.out.println("Player "+ i +" has already joined.\n");

					}
				}else if(line.equals("JOINED")) {
					if(n>1) {
						for(int i=1;i<n;i++) {
							System.out.println("Player "+(i+1)+" has joined from 127.0.0.1");
						}
					}
					
				}else if(line.contains("BOARD")) {
//					displayBoard();
					System.out.println(line);
//					displayQuestions();
				}else if(line.equals("You are correct!")) {
					System.out.println(line);
				} else if(line.equals("You are incorrect!")){
					System.out.println(line);
				}else if(line.contains("turn")){
					System.out.println();
					System.out.println(line);
				}else if(line.equals("QUESTIONS")) {
					System.out.println("Would you like to answer a question across (a) or down (d)?");
					String ab = scan.nextLine();
					while(!ab.equals("a") && !ab.equals("d")) {
						System.out.println("That is not a valid option.");
						System.out.println("Would you like to answer a question across (a) or down (d)?");
						ab=scan.nextLine();
					}
					sendMessage(ab);

				}else if(line.equals("NUMBER")) {
					System.out.println("Which number?");
					String ab=scan.nextLine();
					sendMessage(ab);
				}else if(line.equals("ERRORNUM")){
					System.out.println("That is not a valid option.");
					System.out.println("Which number?");
					String ab=scan.nextLine();
					sendMessage(ab);
				}else if(line.contains("across?")) {
					System.out.println(line);
					String input = scan.nextLine();
					String adder="-ACROSS";
					input+=adder;
					sendMessage(input);
				}else if(line.contains("down?")){
					System.out.println(line);
					String input=scan.nextLine();
					String adder="-DOWN";
					input+=adder;
					sendMessage(input);
				}else if(line.equals("OVER")) {
					System.exit(0);
				}
				else {
					System.out.println(line);
				}

			}
		} catch (IOException ioe) {
			System.out.println("ioe in ChatClient.run(): " + ioe.getMessage());
		}
	}
	public String askQuestions() {
		System.out.println("Would you like to answer a question across (a) or down (d)? ");
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
			for(int i=0;i<ServerFileIO.across.size();i++) {
				if(ServerFileIO.across.get(i).index==num) {
					check=ServerFileIO.across.get(i).index;
				}
			}
			while(check==-1) {
				System.out.println("That is not a valid option.");
				System.out.println("Which number? ");
				num=Integer.parseInt(scan.nextLine());
				for(int i=0;i<ServerFileIO.across.size();i++) {
					if(ServerFileIO.across.get(i).index==num) {
						check=ServerFileIO.across.get(i).index;
					}
				}
				
			}
				String dis=findAnswer(ab,num);
				return dis;
			
		}else if(ab.equalsIgnoreCase("d")) {
			System.out.println("Which number? ");
			num=Integer.parseInt(scan.nextLine());
			int check=-1;
			for(int i=0;i<ServerFileIO.down.size();i++) {
				if(ServerFileIO.down.get(i).index==num) {
					check=ServerFileIO.down.get(i).index;
				}
			}
			while(check==-1) {
				System.out.println("That is not a valid option.");
				System.out.println("Which number? ");
				num=Integer.parseInt(scan.nextLine());
				for(int i=0;i<ServerFileIO.down.size();i++) {
					if(ServerFileIO.down.get(i).index==num) {
						check=ServerFileIO.down.get(i).index;
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
			for(int i=0;i<ServerFileIO.across.size();i++) {
				if(choice==ServerFileIO.across.get(i).index) {
					String word=ServerFileIO.across.get(i).word;
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
			for(int i=0;i<ServerFileIO.down.size();i++) {
				if(choice==ServerFileIO.down.get(i).index) {
					String word = ServerFileIO.down.get(i).word;
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

	public void sendMessage(String message) {
		pw.println(message);
		pw.flush();
	}
	
	public static void main(String []args) {
		ServerFileIO ex = new ServerFileIO();
		ex.fileIO();
		System.out.println("Welcome to 201 Crossword! ");
		System.out.print("Enter the server hostname: ");
		
		Scanner scan = new Scanner(System.in);
		String host = scan.nextLine();
		System.out.print("Enter the server port: ");
		String p = scan.nextLine();
		int port = Integer.parseInt(p);
		new PlayerThread(host, port);
	}
}
