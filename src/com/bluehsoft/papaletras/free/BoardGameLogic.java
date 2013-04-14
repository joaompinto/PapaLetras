package com.bluehsoft.papaletras.free;

import java.util.ArrayList;
import java.util.Random;

import android.util.Log;

public class BoardGameLogic {
	static final String TAG = "BoardGameLogic";
	
	int m_first_clicked_x = -1;
	int m_first_clicked_y = -1;
	int m_last_clicked_x = -1;
	int m_last_clicked_y = -1;
	private PlayBoard m_board;
	public String m_word = "";
    Random m_generator = new Random();
    
	final ArrayList<PlayBoard.Coordinate> m_master_word = new ArrayList<PlayBoard.Coordinate>();

	
	BoardGameLogic() {
	
	}
	
	public ArrayList<PlayBoard.Coordinate> getMasterWord() {
		return m_master_word;
	}
	
	private void placeWord(String word) {
		final String[][] tmp_board = new String[5][5];
		boolean dead_end = true; // flag if placement lead to a dead end		
		
		while(dead_end) { 
			
			m_master_word.clear();
			for(int x=0; x < 5; ++x)
				for(int y=0; y < 5; ++y)
					tmp_board[x][y] = null;
			int x; int y;
			
			x = m_generator.nextInt(4);
			y = m_generator.nextInt(4);
		
				
			int i = -1;
			while(++i < word.length()) {
				// get next letter
				String letter = word.substring(i, i+1);
				if(letter == "Q") {
					i++;
					letter += word.substring(i, i+1);
				}	
				
				// create array of all adjacent positions
				PlayBoard.Coordinate possible_positions[] = new PlayBoard.Coordinate[8];
				int relative[][] = { {-1, -1}, {-1, 0}, {-1, 1},
									 {0, -1}, {0, 1},
									 {1,-1}, {1, 0}, {1, 1}
									};
				
				// determine all possible positions
				// exclude out of the board positions
				// exclude positions already selected
				int possible_count = 0;
				for(int k = 0; k < relative.length; ++k) {
					int pos_x = x - relative[k][0];
					int pos_y = y - relative[k][1];
					if(pos_x < 0 || pos_x > 4 || pos_y < 0 || pos_y > 4)
						continue;
					PlayBoard.Coordinate pos_cord = m_board.new Coordinate(pos_x, pos_y);
					if(tmp_board[pos_x][pos_y] != null)
						continue;
					possible_positions[possible_count++] = pos_cord;
				}
				dead_end = (possible_count == 0); 
				if(dead_end)
					break;
				PlayBoard.Coordinate place_pos;
				if(possible_count > 1)				
					place_pos = possible_positions[m_generator.nextInt(possible_count-1)];
				else
					place_pos = possible_positions[0];

				tmp_board[place_pos.x][place_pos.y] = letter;
				m_master_word.add(place_pos);
				x = place_pos.x;
				y = place_pos.y;
				//p1 = PlayBoard.Coordinate(x-1, y-1)
			}
			
		}
		for(PlayBoard.Coordinate letter_pos: m_master_word) {
			m_board.setTextAtPos(letter_pos.x, letter_pos.y, tmp_board[letter_pos.x][letter_pos.y].toUpperCase());		
		}
		//String letter = word[0];
		//m_board.setTextAtPos(x, y, text)
	}
	
	public void startGame(String startWord) {
		int w = m_board.m_width;
		int h = m_board.m_height;		
        for(int x=0; x < w; ++x)        	
        	for(int y=0; y < h; ++y) {
        		m_board.setTextAtPos(x, y, null);
        	}        		
        placeWord(startWord);
        for(int x=0; x < w; ++x)        	
        	for(int y=0; y < h; ++y) {
        		if(m_board.getTextAtPos(x, y) == null)
        			m_board.setTextAtPos(x, y, getRandomLetter());
        	}        		        
        m_board.clearAll();
	}
	
	public void clearAll() {
		m_board.clearAll();
		m_word = "";
		m_first_clicked_x = m_last_clicked_x = -1;
		m_board.invalidate();
		Log.d(TAG, "clearAll was called");
	}

	void setBoard(PlayBoard board) {
		m_board = board;
		m_board.setGameLogic(this);
	}

	
	void Click(int x, int y, int state) {
		if (m_last_clicked_x != -1) {
			boolean was_selected = (m_board.getTile(x, y) >= PlayBoard.SELECTED); 
	    	boolean is_adjacent = Math.abs(x-m_last_clicked_x) < 2 && Math.abs(y - m_last_clicked_y)  < 2;
			if(was_selected || !is_adjacent) { 
					clearAll();
			} else { // Set previous item to selected
				m_board.setTile(PlayBoard.SELECTED, m_last_clicked_x, m_last_clicked_y);
			}
			
		}
    	m_word += m_board.getTextAtPos(x, y);   
    	if(m_first_clicked_x == -1) {
    		m_first_clicked_x = x;
    		m_first_clicked_y = y;
    	}
    	m_last_clicked_x = x;
    	m_last_clicked_y = y;
    	m_board.setTile(PlayBoard.LAST_SELECTED, x, y);
    	m_board.invalidate();
	}
	
	int countVowels(String text) {
	    int count = 0; // start the count at zero
	    // change the string to lowercase
	    text = text.toLowerCase();
	  
	    for (int i = 0; i < text.length(); i++) {
	        char c = text.charAt(i);
	        if (c=='a' || c=='e' || c=='i' || c=='o' || c=='u') {
	            count++;
	        }
	    }
	    return count;
	}		
	
	void swap2() {
		String letter1 = m_board.getTextAtPos(m_first_clicked_x, m_first_clicked_y);
		String letter2 = m_board.getTextAtPos(m_last_clicked_x, m_last_clicked_y);
		m_board.setTextAtPos(m_first_clicked_x, m_first_clicked_y, letter2);
		m_board.setTextAtPos(m_last_clicked_x, m_last_clicked_y, letter1);
		m_first_clicked_x = m_last_clicked_x = -1;
		m_first_clicked_y = m_last_clicked_y = -1;
		clearAll();
	}
	
	int countSelected() {
		int count = 0;
		int m_width = m_board.m_width;
		int m_height = m_board.m_height;		
		for (int x = 0; x < m_width; x++) {
			for (int y = 0; y < m_height; y++) { 
				if(m_board.getTile(x, y) != PlayBoard.REGULAR) {
					count++;
				}
			}
		}
		return count;
	}
	
	void swapSelected() {
		Log.d(TAG, "swapSelected was called");
		int vowels_count = 0;
		int new_vowels_count = 0;
		String new_letter;
		int m_width = m_board.m_width;
		int m_height = m_board.m_height;		
		
		// First lets count vowels
		for (int x = 0; x < m_width; x++) {
			for (int y = 0; y < m_height; y++) { 
				if(m_board.getTile(x, y) != PlayBoard.REGULAR) {
					vowels_count += countVowels(m_board.getTextAtPos(x, y));
					do {
						new_letter = getRandomLetter();
					} while (vowels_count-(new_vowels_count+countVowels(new_letter))>0);
					new_vowels_count += countVowels(new_letter);				
					m_board.setTextAtPos(x, y, new_letter);
				}
			}
		}
		Log.d(TAG, "First swap: "+vowels_count+","+new_vowels_count);
		
		
		if(vowels_count == new_vowels_count) 
			return;

		for (int x = 0; x < m_width; x++) {
			for (int y = 0; y < m_height; y++) {				 
				if(m_board.getTile(x, y) != PlayBoard.REGULAR) {
					String prev_letter = m_board.getTextAtPos(x, y);
					if(countVowels(prev_letter) == 0) {
						do {
							new_letter = getRandomLetter();
						} while(countVowels(new_letter) == 0);
						new_vowels_count++;
						if(vowels_count == new_vowels_count) 
							return;
					}
				}
			}
		}
	}
	
	String getWord() {
		return m_word;
	}
	
	private String getRandomLetter() {
		String exclude = "KWY";
		String c;
		do {
			int i = m_generator.nextInt('Z'-'A'+1)+'A';
			c = new Character((char) i).toString();
		} while (exclude.contains(c));
		if(c.compareTo("Q") == 0)
			c = "QU";
		return c; 
	}	
}
