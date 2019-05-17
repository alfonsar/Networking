package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import alfonsar_CSCI201L_Assignment4.Pairs;



public class ServerThread extends Thread {
	private Server serve;
	private Lock lock;
	private Condition condition;
	public static boolean firstPlayer=false;
	public static boolean firstP=true;
	private PrintWriter pw;
	private BufferedReader br;
	private int numPlay;
	public int score=0;
	@SuppressWarnings("static-access")
	public ServerThread(Socket s, Server serve, Lock lock, Condition condition, boolean firstPlayer) {
		this.serve=serve;
		this.lock=lock;
		this.firstPlayer=firstPlayer;
		this.condition=condition;
		try {
			pw = new PrintWriter(s.getOutputStream());
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void sendMessage(String message) {
		pw.println(message);
		pw.flush();
	}
	public void sendMessage1(String message) {
		pw.print(message);
		pw.flush();
	}
	public void run() {
		boolean play=true;
		int num=0;
		String mes="";
		String checkWord="";
		int n=0;
		boolean good=false;
		boolean empty=false;
		String[]parts = null;
		boolean over=false;
		String ad="";
		while(true) {
			try {
				lock.lock();
				//play is for first turn
				if(play==true) {
					if(firstPlayer==true) {
						sendMessage("PLAYERS");
						firstPlayer=false;
						String line = br.readLine();
						num=Integer.parseInt(line);
						getNumPlayers(line);
						sendMessage("JOINED");
					}else if(firstPlayer==false) {
						int curr=serve.currentPlayers();
						serve.broadcast("Player "+curr+" has joined from 127.0.0.1\n",this);
						sendMessage("GAMEWAITING-"+curr+"-"+serve.numPlayers);
						if(curr==serve.numPlayers) {
							serve.broadcastAll("The game is beginning\n");
						}
						condition.await();
					}
					play=false;
				}else if(play==false){
					while(true) {
						//displays board all of them
						if(serve.numPlayers==1) {
							sendMessage("The game is beginning.");
						}
						serve.displayBoard();
						serve.displayQuestions();
						serve.takeTurns(this);
						boolean again=true;
						while(again==true)
						{
							good=false;
							sendMessage("QUESTIONS");
							ad=br.readLine();
							if(ad.equalsIgnoreCase("a")) {
								if(serve.allAcross.size()==0) {
									sendMessage("That is not a valid option.");
									sendMessage("QUESTIONS");
									ad=br.readLine();
									while(!ad.equalsIgnoreCase("d")) {
										sendMessage("That is not a valid option.");
										sendMessage("QUESTIONS");
										ad=br.readLine();
									}
								}
							}else if(ad.equalsIgnoreCase("d")) {
								if(serve.allDown.size()==0) {
									sendMessage("That is not a valid option.");
									sendMessage("QUESTIONS");
									ad=br.readLine();
									while(!ad.equalsIgnoreCase("a")) {
										sendMessage("That is not a valid option.");
										sendMessage("QUESTIONS");
										ad=br.readLine();
									}
								}
							}
							sendMessage("NUMBER");
							String checker=br.readLine();
							boolean isNumber=serve.checkNum(checker,ad);
							while(isNumber==false) {
								sendMessage("That is not a valid option.");
								sendMessage("NUMBER");
								checker=br.readLine();
								isNumber=serve.checkNum(checker, ad);
							}
							n=Integer.parseInt(checker);
							if(ad.equals("a")) {
								sendMessage("What is your guess for "+n+" across?");
							}else if(ad.equals("d")) {
								sendMessage("What is your guess for "+n+" down?");
							}
							checkWord=br.readLine();
							boolean ansCorrect=serve.remove(checkWord,n);
							parts=checkWord.split("-");
							if(ansCorrect==false) {
								again=false;
							}else {

								if(ad.equalsIgnoreCase("a")) {
									mes="guessed '"+parts[0]+"' for "+ n + " across. That is correct";
									score++;
								}else if(ad.equalsIgnoreCase("d")) {
									mes="guessed '"+parts[0]+"' for "+ n + " down. That is correct";
									score++;
								}
								serve.placeWord(ad,parts[0],n);
								sendMessage("You are correct!");
								empty=serve.isEmp();
								if(empty==true) {
									break;
								}
								serve.correctPlayer(mes,this);
								serve.displayBoard();
								serve.displayQuestions();
								serve.takeTurns(this);
							}

						}

						break;	
					}
					if(empty==false) {
						sendMessage("You are incorrect!");
						if(ad.equalsIgnoreCase("a")) {
							mes="guessed '"+parts[0]+"' for "+ n + " across. That is incorrect";

						}else if(ad.equalsIgnoreCase("d")) {
							mes="guessed '"+parts[0]+"' for "+ n + " down. That is incorrect";

						}
						serve.correctPlayer(mes, this);
						if(num==1) {
							continue;
						}
						serve.displayBoard();
						serve.displayQuestions();
						serve.callSignal(this);
						condition.await();
					}
					//gameover
					serve.displayBoard();
					serve.sendingFinal();
					serve.gameOver=true;
					serve.broadcastAll("OVER");
					over=true;
					//serve.switchSignal();
					
				}
			
			}catch (IOException | InterruptedException ioe) {
				//System.exit(0);
				//System.out.println("ioe in ServerThread.run(): " + ioe.getMessage());
				break;
			} 
			if(over==true) {
				break;
			}
		}
	}

	public void getNumPlayers(String line) {
		numPlay=Integer.parseInt(line);
		serve.setPlayers(numPlay);
	}

}
