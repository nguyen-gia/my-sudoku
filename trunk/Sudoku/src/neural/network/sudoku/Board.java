package neural.network.sudoku;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;

public class Board {
	private JFrame window;
	private Container mainPanel;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem newMenuItem;
	private JMenuItem solveMenuItem;
	private JMenuItem saveMenuItem;
	private JMenuItem loadMenuItem;
	private JTextField[][] input;
	private int[][][] Q;
	private boolean[][][] clamp;
	/*Qijk is output of neural ijk.
	  Qijk = 1 means if and only if the cell in ith row and jth column has the value of k 
	  In the otherwise, Qikj = 0.
    */
	private boolean[][] fixed;
	private int[][][] noise;
			
	public Board(){
		window = new JFrame("Sudoku - Heuristic Computing");
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		menuBar.add(fileMenu);
		window.setJMenuBar(menuBar);
		
		newMenuItem = new JMenuItem("New");
		newMenuItem.setMnemonic(KeyEvent.VK_N);
		fileMenu.add(newMenuItem);
		newMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				for(int i=1;i<=9;i++)
					for(int j=1;j<=9;j++){
						input[i][j].setText(null);
						input[i][j].setForeground(Color.black);
					}
			}
		});
		
		solveMenuItem = new JMenuItem("Solve");
		solveMenuItem.setMnemonic(KeyEvent.VK_O);
		fileMenu.add(solveMenuItem);
		solveMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				init();
				process();
				for(int i=1;i<=9;i++)
					for(int j=1;j<=9;j++){
						String s="";
						for(int k=1;k<=9;k++)
							if(Q[i][j][k]==1){
								if(fixed[i][j]==true){
									input[i][j].setForeground(Color.blue);
								}
							s=s+Integer.toString(k);
							}
							input[i][j].setText(s);
						}
				}
		});
		
		saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setMnemonic(KeyEvent.VK_S);
		saveMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// Write game's status to a file
				 	JFileChooser chooser = new JFileChooser();
				 	FileOutputStream fos = null;
				 	DataOutputStream dos;
				    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
				    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
				    String path = null;
				    int returnVal = chooser.showSaveDialog(null);
				    if(returnVal == JFileChooser.APPROVE_OPTION) {
				            path = chooser.getSelectedFile().getPath();
				            System.out.println(path);
				    	    File f = new File(path);
				    	    try {
				    	    	fos = new FileOutputStream(f);
				    	    	dos = new DataOutputStream(fos);
				    	    	Q = new int[10][10][10];
								fixed = new boolean[10][10];
								for(int i=1;i<=9;i++)
									for(int j=1;j<=9;j++)
										for(int k=1;k<=9;k++){
											Q[i][j][k] = 0;
											fixed[i][j] = false;
										}
				    	    	for(int i=1;i<=9;i++)
									for(int j=1;j<=9;j++)
										if(input[i][j].getText()!=null){
											int k;
											try{
											   k = Integer.parseInt(input[i][j].getText());
											}catch(NumberFormatException s){
											   k = 0;
											}
											if(k>=1 && k<= 9){
												Q[i][j][k]=1;
											}			
										}
				    	    	for(int i=1;i<=9;i++)
				    	    		for(int j=1;j<=9;j++)
				    	    			for(int k=1;k<=9;k++)
				    	    				try {
				    	    					dos.writeInt(Q[i][j][k]);
				    	    				} catch (IOException e1) {
				    	    					// 	TODO Auto-generated catch block
				    	    					e1.printStackTrace();
				    	    				}
				    	    				
				    	    	} catch (FileNotFoundException e1) {
				    	    		// 	TODO Auto-generated catch block
				    	    		e1.printStackTrace();
				    	    	}
				    	    	
				    	}
				}
			});
		fileMenu.add(saveMenuItem);
		
	    loadMenuItem = new JMenuItem("Load");
		loadMenuItem.setMnemonic(KeyEvent.VK_L);
		loadMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//Open and read file, set game's status
				JFileChooser chooser = new JFileChooser();
			 	FileInputStream fis = null;
			 	DataInputStream dis;
			 	String path = null;
			    int returnVal = chooser.showOpenDialog(null);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			            path = chooser.getSelectedFile().getPath();
			            System.out.println(path);
			    	    File f = new File(path);
			    	    try {
			    	    	fis = new FileInputStream(f);
			    	    	dis = new DataInputStream(fis);
			    	    	Q = new int[10][10][10];
			    	    	for(int i=1;i<=9;i++)
								for(int j=1;j<=9;j++){
									input[i][j].setText(null);
									input[i][j].setForeground(Color.black);
									for(int k=1;k<=9;k++){
										Q[i][j][k] = 0;										
									}
								}
			    	    	for(int i=1;i<=9;i++)
			    	    		for(int j=1;j<=9;j++)
			    	    			for(int k=1;k<=9;k++)
			    	    			try {
			    	    				Q[i][j][k]= dis.readInt();
			    	    				if(Q[i][j][k]==1){
			    	    					input[i][j].setText(Integer.toString(k));
			    	    				}
			    	    			} catch (IOException e1) {
			    	    				// 	TODO Auto-generated catch block
			    	    				e1.printStackTrace();
			    	    			}
			    	    			
			    	    } catch (FileNotFoundException e1) {
			    	    	// TODO Auto-generated catch block
			    	    	e1.printStackTrace();
			    	    }
			    }
			}
		});
		fileMenu.add(loadMenuItem);
		
		mainPanel = window.getContentPane();
		mainPanel.setLayout(new GridLayout(9,9));
		input = new JTextField[10][10];
		for(int i=1;i<=9;i++)
			for(int j=1;j<=9;j++){
				input[i][j] = new JTextField(1);
				Font newFont=new Font(input[i][j].getFont().getName(),Font.BOLD,20);
				input[i][j].setFont(newFont);
				mainPanel.add(input[i][j]);
			}
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setSize(600, 600);
		window.setResizable(false);
		window.setLocation(400, 60);
		window.setVisible(true);	
	}
	private void init()
	{
		Q = new int[10][10][10];
		fixed = new boolean[10][10];
		clamp = new boolean[10][10][10];
		for(int i=1;i<=9;i++)
			for(int j=1;j<=9;j++)
				for(int k=1;k<=9;k++){
					Q[i][j][k] = 0;
					fixed[i][j] = false;
					clamp[i][j][k] = false;
				}
		for(int i=1;i<=9;i++)
			for(int j=1;j<=9;j++)
				if(input[i][j].getText()!=null){
					int k;
					try{
					   k = Integer.parseInt(input[i][j].getText());
					}catch(NumberFormatException e){
					   k = 0;
					}
					if(k>=1 && k<= 9){
						Q[i][j][k]=1;
						fixed[i][j]=true;
						setClamp(i,j,k);
					}
			}
	}
	private void setClamp(int p, int q, int r){
		clamp[p][q][r]=true;
		//Clamp column
		for(int i=1;i<=9;i++){			
				clamp[i][q][r]=true;
				}
		
		//Clamp row
		for(int j=1;j<=9;j++){
				clamp[p][j][r]=true;
			}
		
		//Clamp region
		for(int i=1;i<=3;i++)
			for(int j=1;j<=3;j++){
				clamp[(p-1)/3*3+i][(q-1)/3*3+j][r]=true;
			}
		
		//Clamp symbol
		for(int i=1;i<=9;i++){
				clamp[p][q][i]=true;
			}
	}
	private boolean check(){
		int sum_row =0;
		int sum_col =0;
		int sum_rgn =0;
		int sum_sym =0;
		//Check row
		for(int i=1;i<=9;i++)
			for(int k=1;k<=9;k++){
				sum_row=0;
				for(int j=1;j<=9;j++)
					sum_row += Q[i][j][k];
				if(sum_row != 1){
					//System.out.println("Row + i = "+i+" k = "+k);
					return false;
				}
			}
		
		//check column;
		for(int j=1;j<=9;j++)
			for(int k=1;k<=9;k++){
				sum_col=0;
				for(int i=1;i<=9;i++)
					sum_col += Q[i][j][k];
				if(sum_col!=1){
					//System.out.println("Column + j = "+j+" k = "+k);	
					return false;
				}
			}
		
		//check region
		for(int m=0;m<=2;m++)
			for(int n=0;n<=2;n++)
				for(int k=1;k<=9;k++){
					sum_rgn=0;
					for(int i=1;i<=3;i++)
						for(int j=1;j<=3;j++)
							sum_rgn += Q[3*m+i][3*n+j][k];
					if(sum_rgn!=1){
						//System.out.println("Region m = "+m+" n = "+n+" k = "+k);
						return false;
					}
				}
		
		//check symbol
		for(int i=1;i<=9;i++)
			for(int j=1;j<=9;j++){
				sum_sym=0;
				for(int k=1;k<=9;k++)
					sum_sym += Q[i][j][k];
				if(sum_sym != 1) {
					//System.out.println("Symbol + i = "+i+" j = "+j);
					return false;
				}
			}
				
		return true;
	}
	private int kronecker(int i,int j){
		if(i==j)
			return 1;
		else
			return 0;
	}
	
	private int conStrength(int i, int j, int k, int l, int m, int n){
		if(k!=n &&((i!=l)||(j!=m)))
			return 0;
		if(k!=n && (i==l)&&(j==m))
			return -1;
		if(k==n && i==l && j==m) return -4;
		
		return -kronecker(i, l)-kronecker(j, m)-kronecker((i-1)/3, (l-1)/3)*kronecker((j-1)/3,(m-1)/3);//-kronecker(k, n);
		
	}
	private void generateRandomNoise(){
		noise = new int[10][10][10];
		Random randomGenerator = new Random();
		for(int i=1;i<=9;i++)
			for(int j=1;j<=9;j++)
				for(int k=1;k<=9;k++){
					noise[i][j][k] = -2 + randomGenerator.nextInt(5);
					//System.out.println(noise[i][j][k]);
				}
					
	}
	private void printClamp(){
		for(int i= 1;i<=9;i++)
			for(int j=1;j<=9;j++){
				System.out.print("i = "+i+" j = "+j);
				for(int k=1;k<=9;k++)
					if(clamp[i][j][k]==true)
						System.out.print(" "+k);
				System.out.println(" ");
			}
	}
	private void process(){
		int iteration = 1;
		int a=1,q=2,I=4;
		int stimulus;
		boolean end=false;
		while((end==false)&&(check()==false)){
			System.out.println("Iteration "+iteration);
			end=true;
			for(int i=1;i<=9;i++)
				for(int j=1;j<=9;j++)
					for(int k=1;k<=9;k++){
						if(clamp[i][j][k]==false){
							stimulus = 0;	
							for(int l=1;l<=9;l++)
								for(int m=1;m<=9;m++)
									for(int n=1;n<=9;n++){
										stimulus += conStrength(i, j, k, l, m, n)*a*Q[l][m][n];
										}
							stimulus +=I;
							if(stimulus >2 &&(Q[i][j][k]<q-1)){
								Q[i][j][k]++;
								end=false;
							}
							if(stimulus <0 &&(Q[i][j][k]>0)){
								Q[i][j][k]--;
								end=false;
							}
						}				
					}
			iteration++;	
		}
		if(check()==true)System.out.println("Solution was found");
		System.out.println(iteration);			
 	}

}
