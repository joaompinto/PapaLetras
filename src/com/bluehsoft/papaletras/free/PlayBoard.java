package com.bluehsoft.papaletras.free;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PlayBoard extends View {

	static final String TAG = "PlayBoard";

	public static final int REGULAR = 1;
	public static final int SELECTED = 2;
	public static final int LAST_SELECTED = 3;

	int m_width = 0;
	int m_height = 0;
	int m_tile_width = 0;
	int m_tile_height = 0;
	private Bitmap[] mTileArray;	
	private int[][] mTileGrid;
	String mTextGrid[][];
	BoardGameLogic m_game_logic; 
	private final Paint mPaint = new Paint();
	
    public PlayBoard(Context context, AttributeSet attrs) {    	
        super(context, attrs);
        setPadding(0, 0, 0, 0);        
    	setOnTouchListener(new OnTouchListener() {
    		public boolean onTouch(View v, MotionEvent event) {    			
    		    // TODO Auto-generated method stub

    		    switch(event.getAction()){
	    		    case MotionEvent.ACTION_DOWN:
	    		    	break;
	    		    case MotionEvent.ACTION_UP:
	    		    	if (!v.isClickable())
	    		    		return true;
	    	            final int x = (int)event.getX();
	    	            final int y = (int)event.getY();
	    	            PlayBoard pb = (PlayBoard) v;
	    	            pb.OnClick(x, y);   
	    	            performClick();	            
	    	            break;
	    		    case MotionEvent.ACTION_MOVE:    	
	    		    	break;    		    
    		    }
    		    return true;
    		}
    	});
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {    
    	m_tile_width = w / m_width;
    	m_tile_height = h / m_height;
		Resources r = this.getContext().getResources();
		resetTiles(LAST_SELECTED+1);
		loadTile(REGULAR, r.getDrawable(R.drawable.btn_black));
		loadTile(SELECTED, r.getDrawable(R.drawable.btn_red));
		loadTile(LAST_SELECTED, r.getDrawable(R.drawable.btn_orange));
    }
    
  
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);            	
    	for (int x=0; x < m_width; ++x)
    		for (int y=0; y < m_height; ++y) {
    			if(mTileGrid[x][y] > 0) {
                    canvas.drawBitmap(mTileArray[mTileGrid[x][y]], 
                    		x * m_tile_width,
                    		y * m_tile_height,
                    		mPaint);    				
    				
    			}
    			if(mTextGrid[x][y] != null)
    				drawText(canvas, mTextGrid[x][y], x, y);
    		}
    	/*
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF66AAFF);
        p.setStrokeWidth(5);
        canvas.drawRect(1, 1, this.getWidth(), this.getheightt(), p);*/        
    }


    public void setBoardSize(int w, int h) {    	
    	m_width = w; m_height = h;
    	mTileGrid = new int[w][h];
    	mTextGrid = new String[w][h];
    	clearAll();    	    	    	
    }
    

    public void setTextAtPos(int x, int y, String text) {
    	mTextGrid[x][y] = text;
    }
    
    private void drawText(Canvas canvas, String text, int x, int y)
    {
    	int text_x = x*m_tile_width+m_tile_width/2; 
    	int text_y = y*m_tile_height+m_tile_height/2+15;
		Paint strokePaint = new Paint();
		strokePaint.setARGB(255, 0, 0, 0);
		strokePaint.setTextAlign(Paint.Align.CENTER);
		int textSize;
		if(m_width > 64)
			textSize = 50;
		else
			textSize = 35;
		strokePaint.setTextSize(textSize);
		strokePaint.setTypeface(Typeface.DEFAULT_BOLD);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setStrokeWidth(2);
		
		Paint textPaint = new Paint();
		textPaint.setARGB(255, 238, 232, 170);		
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(textSize);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		
		canvas.drawText(text, text_x, text_y, strokePaint);
		canvas.drawText(text, text_x, text_y, textPaint);
    }
    
    public void setGameLogic(BoardGameLogic game_logic) {
    	m_game_logic = game_logic;
    }
    
    private void OnClick(int x, int y) {
    	int board_x = x / this.m_tile_width;
    	int board_y = y / this.m_tile_height;
    	if (board_x >= m_width || board_y >= m_height) // Out of board
    		return;
    	m_game_logic.Click(board_x, board_y, mTileGrid[board_x][board_y]);
    }   

    public String getTextAtPos(int x, int y) {
    	return mTextGrid[x][y];
    }

    /**
     * Simple class containing two integer values and a comparison function.
     * There's probably something I should use instead, but this was quick and
     * easy to build.
     * 
     */
    public class Coordinate {
        public int x;
        public int y;

        public Coordinate(int newX, int newY) {
            x = newX;
            y = newY;
        }

        public boolean equals(Coordinate other) {
            if (x == other.x && y == other.y) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Coordinate: [" + x + "," + y + "]";
        }
    }    

    /**
     * Function to set the specified Drawable as the tile for a particular
     * integer key.
     * 
     * @param key
     * @param tile
     */
    public void loadTile(int key, Drawable tile) {
        Bitmap bitmap = Bitmap.createBitmap(m_tile_width, m_tile_height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tile.setBounds(0, 0, m_tile_width, m_tile_height);
        tile.draw(canvas);       
        mTileArray[key] = bitmap;
    }
    
    /**
     * Resets all tiles to 0 (empty)
     */
    public void clearAll() {
        for (int x = 0; x < m_width; x++) {
            for (int y = 0; y < m_height; y++) {
                setTile(REGULAR, x, y);
            }
        }
    }  
    
    /**
     * Used to indicate that a particular tile (set with loadTile and referenced
     * by an integer) should be drawn at the given x/y coordinates during the
     * next invalidate/draw cycle.
     * 
     * @param tileindex
     * @param x
     * @param y
     */
    public void setTile(int tileindex, int x, int y) {
    	//Log.d(TAG, "SetTitle "+tileindex+" "+x+" "+y);
        mTileGrid[x][y] = tileindex;        
    }    

    public int getTile(int x, int y) {
        return mTileGrid[x][y];
    }    

    /**
     * Resets the internal array of Bitmaps used for drawing tiles, and
     * sets the maximum index of tiles to be inserted
     * 
     * @param tilecount
     */
    
    public void resetTiles(int tilecount) {
    	mTileArray = new Bitmap[tilecount];
    }
    
    public String getBoardData() {
    	String board_data = "";
    	for (int x=0; x < m_width; ++x)
    		for (int y=0; y < m_height; ++y)
    			board_data += mTextGrid[x][y];   
    	return board_data;
    }
    
    public void setBoarData(String board_data) {
    	int i = 0;
    	for (int x=0; x < m_width; ++x)
    		for (int y=0; y < m_height; ++y) {
    			mTextGrid[x][y] = board_data.substring(i, i+1);
    			++i;
    		}
    }
    
}


