import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

public class Board extends JPanel{
	static final int WIDTH = 800;
	static final int HEIGHT = 600;
	private int leftMargin = 200;
	private int topMargin = 100;
	private int square = 50; //マスのサイズ
	int turn = 1; //手番(1:黒, 2:白)
	int winner = 0; //勝者(0:未定, 1:黒, 2:白)
	int screen = 0; //画面モード(0:タイトル, 1:プレイ, 2:終了)
	int[][] pieces = new int[8][8]; //コマの配置
	
	public Board() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		addMouseListener(new Mouse());
		
		for (int i=0; i<8; i++) {
			for (int j=0; j<8; j++) {
				pieces[i][j] = 0;
			}
		}
		pieces[3][3] = 1;
		pieces[4][4] = 1;
		pieces[3][4] = 2;
		pieces[4][3] = 2;
	}
	
	//画面描画
	public void paintComponent(Graphics g) {
		if (screen==0) {
			g.setColor(new Color(255,204,153));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(new Color(51, 51, 51));
			g.setFont(new Font("HGP創英角ポップ体",Font.BOLD,80));
			g.drawString("REVERSI GAME", 100, 250);
			g.setFont(new Font("HGP創英角ポップ体",Font.ITALIC,50));
			g.drawString("~Click to Start~", 200, 350);
		}
		else {
			//背景
			g.setColor(new Color(255,204,153));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			
			//盤面
			((Graphics2D)g).setStroke(new BasicStroke(2));
			for (int i=0; i<8; i++) {
				int y = topMargin + square * i;
				for (int j=0; j<8; j++) {
					int x = leftMargin + square * j;
					g.setColor(new Color(62, 179, 112));
					g.fillRect(x, y, square, square);
					g.setColor(Color.white);
					g.drawRect(x, y, square, square);
					
					if (pieces[i][j]!=0) {
						if (pieces[i][j]==1) g.setColor(Color.black);
						else g.setColor(Color.white);
						g.fillOval(x+square/10, y+square/10, square/10*8, square/10*8);
						g.setColor(Color.black);
						g.drawOval(x+square/10, y+square/10, square/10*8, square/10*8);
					}
					
					if (canPut(i, j, turn)) {
						g.setColor(new Color(255, 153, 102));
						g.drawOval(x+square/3, y+square/3, square/3, square/3);
					}
				}
			}
			
			//手番
			g.setColor(new Color(51, 51, 51));
			g.setFont(new Font("HGP創英角ポップ体",Font.BOLD,30));
			if (winner==0) {
				if (turn==1) g.drawString("黒の番です", 330, 60);
				else g.drawString("白の番です", 330, 60);
			}
			else if (winner==1) g.drawString("黒の勝ちです", 330, 60);
			else if (winner==2) g.drawString("白の勝ちです", 330, 60);
			else g.drawString("引き分けです", 330, 60);
			
			//コマの数
			g.setFont(new Font("HGP創英角ポップ体",Font.BOLD,25));
			g.drawString("黒:"+countOwnPieces(1), 670, 65);
			g.drawString("白:"+countOwnPieces(2), 670, 100);
		}
	}
	
	class Mouse extends MouseAdapter{
		public void mouseClicked(MouseEvent e) {
			if (screen==0) {
				screen = 1;
				startGame();
			}
			else if (screen==1) {
				int mx = e.getX();
				int my = e.getY();
				
				//決着がついていたら何もしない
				if (winner!=0) return;
				
				//盤の外側をクリック
				if (mx<leftMargin || mx>=leftMargin+square*8 || my<topMargin || my >=topMargin+square*8) return;
				
				//クリックされたマスを取得
				int row = (my - topMargin) / square;
				int col = (mx - leftMargin) / square;
				if (canPut(row, col, turn)) {
					put(row, col, turn);
					turn = opponentTurn(turn);
					
					//パス判定
					if (countCanPut(turn)==0) turn = opponentTurn(turn);
					
					//終了判定
					winner= judge();
					if (winner!=0) screen = 2;
				}
			}
			else screen = 0;
			
			repaint();
		}
	}
	
	public void startGame() {
		turn = 1;
		winner = 0;
		for (int i=0; i<8; i++) {
			for (int j=0; j<8; j++) {
				pieces[i][j] = 0;
			}
		}
		pieces[3][3] = 1;
		pieces[4][4] = 1;
		pieces[3][4] = 2;
		pieces[4][3] = 2;
	}
	
	public int opponentTurn(int turn) {
		if (turn==1) return 2;
		else return 1;
	}
	
	public int countSandwich(int row, int col, int turn, int dx, int dy) {
		int count = 0;
		int x = col + dx;
		int y = row + dy;
		
		while (0<=x && x<8 && 0<=y && y<8) {
			if (pieces[y][x]==opponentTurn(turn)) count++;
			else if (pieces[y][x]==turn) return count;
			else break;
			
			x += dx;
			y += dy;
		}
		
		return 0;
	}
	
	public int countCanPut(int turn) {
		int count = 0;
		for (int i=0; i<8; i++) {
			for (int j=0; j<8; j++) {
				if (canPut(i, j, turn)) count++;
			}
		}
		
		return count;
	}
	
	public int judge() {
		if (countCanPut(1)==0 && countCanPut(2)==0) {
			int numB = countOwnPieces(1);
			int numW = countOwnPieces(2);
			if (numB>numW) return 1;
			else if (numB<numW) return 2;
			else return 3; //引き分け
		}
		
		return 0; //未決着
	}
	
	public boolean canPut(int row, int col, int turn) {
		if (pieces[row][col]!=0) return false;
		
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				if (x==0 && y==0) continue;
				if (countSandwich(row, col, turn, x, y)>0) return true;
			}
		}
		
		return false;
	}
	
	public void put(int row, int col, int turn) {
		pieces[row][col] = turn;
		for (int i=0; i<8; i++) {
			for (int x=-1; x<2; x++) {
				for (int y=-1; y<2; y++) {
					if (x==0 && y==0) continue;
					int count = countSandwich(row, col, turn, x, y);
					if (count>0) flip(row, col, x, y, count);
				}
			}
		}
	}
	
	public void flip(int row, int col, int dx, int dy, int count) {
		int x = col + dx;
		int y = row + dy;
		
		for (int i=0; i<count; i++) {
			pieces[y][x] = opponentTurn(pieces[y][x]);
			x += dx;
			y += dy;
		}
	}
	
	public int countOwnPieces(int turn) {
		int count = 0;
		
		for (int x=0; x<8; x++) {
			for (int y=0; y<8; y++) {
				if (pieces[y][x]==turn) count++;
			}
		}
		
		return count;
	}
}
