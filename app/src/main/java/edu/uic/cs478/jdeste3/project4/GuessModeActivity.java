/*
    Jessica DeStefano - GuessModeActivity.java
    Contains two threads which run the game as a button is pressed, which
     alternates players. User has the option to go back to the main menu after
     a thread wins. Mode is signaled by "1".
*/

package edu.uic.cs478.jdeste3.project4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class GuessModeActivity extends Activity {

    // Create a visible board which the user sees, and a hidden board
    // used for determining what is at each location
    private ImageView visibleBoard[][] = new ImageView[10][10];
    public static int hiddenBoard[][] = new int[10][10];
    public ImageView iv;

    private TextView text;
    private Button turnButton;
    private Button mainMenuButton;

    private int gopherPositionRow;
    private int gopherPositionColumn;
    private Random rand = new Random();

    private int turnNum = 1;
    private int mode = 1;

    // Used by UI threads
    public static final int SUCCESS = 0;
    public static final int NEAR_MISS = 1;
    public static final int CLOSE_GUESS = 2;
    public static final int COMPLETE_MISS = 3;
    public static final int DISASTER = 4;

    // Used by worker threads
    public static final int MAKE_MOVE = 5;

    // UI handler
    // Receives messages from t1 and t2 and determines what message to print out
    // based on the current turn number and message received
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            int what = msg.what;

            switch(what){
                case SUCCESS:
                    declareWinner();
                    break;

                case DISASTER:
                    if (turnNum % 2 == 1) {
                        Toast.makeText(GuessModeActivity.this, "Thread 1: SPOT TAKEN", Toast.LENGTH_SHORT).show();
                    }
                    else if (turnNum % 2 == 0){
                        Toast.makeText(GuessModeActivity.this, "Thread 2: SPOT TAKEN", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case NEAR_MISS:
                    updateBoard();
                    if (turnNum % 2 == 1) {
                        Toast.makeText(GuessModeActivity.this, "Thread 1: GOPHER IS 1 SPOT AWAY", Toast.LENGTH_SHORT).show();
                    }
                    else if (turnNum % 2 == 0){
                        Toast.makeText(GuessModeActivity.this, "Thread 2: GOPHER 1 SPOT AWAY", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case CLOSE_GUESS:
                    updateBoard();
                    if (turnNum % 2 == 1) {
                        Toast.makeText(GuessModeActivity.this, "Thread 1: GOPHER IS 2 SPOTS AWAY", Toast.LENGTH_SHORT).show();
                    }
                    else if (turnNum % 2 == 0){
                        Toast.makeText(GuessModeActivity.this, "Thread 2: GOPHER 2 SPOTS AWAY", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case COMPLETE_MISS:
                    updateBoard();
                    if (turnNum % 2 == 1) {
                        Toast.makeText(GuessModeActivity.this, "Thread 1: TOO FAR", Toast.LENGTH_SHORT).show();
                    }
                    else if (turnNum % 2 == 0){
                        Toast.makeText(GuessModeActivity.this, "Thread 2: TOO FAR", Toast.LENGTH_SHORT).show();
                    }
                    break;

            } // end switch
            turnNum++; // increment turn number
        } // end handleMessage
    }; // end anon class

    public WorkerThread1 t1;
    public WorkerThread2 t2;

    // onCreate: Initializes all buttons and both game boards
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ImageView image = findViewById(R.id.gopher2);
        image.setImageResource(R.drawable.gopher);

        text = (TextView) findViewById(R.id.winnerText);
        turnButton = (Button) findViewById(R.id.nextButton);
        mainMenuButton = (Button) findViewById(R.id.mainMenuButton);

        turnButton.setVisibility(View.VISIBLE);

        initializeBoards();

        t1 = new WorkerThread1(mHandler, mode);
        t1.start();

        t2 = new WorkerThread2(mHandler, mode);
        t2.start();
    }

    // nextGuess: On click listener for nextGuess button. Determines which thread will
     // make their move based on the turn number. Sends message to that thread
    public void nextGuess(View v){
        if (turnNum % 2 == 1) {
            Message m = t1.t1Handler.obtainMessage(MAKE_MOVE);
            t1.t1Handler.sendMessage(m);
        }
        else if (turnNum % 2 == 0){
            Message m = t2.t2Handler.obtainMessage(MAKE_MOVE);
            t2.t2Handler.sendMessage(m);
        }
    }

    // mainMenu: on click listener for the mainMenu button. Restarts main activity
     // when a thread wins
    public void mainMenu(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // initializeBoards: Creates a visible board of ImageViews and initializes the hiddenBoard to
    // be -1 in each spot. The gopher is also places randomly, signaled by a 0.
    private void initializeBoards(){
        final GridLayout view = (GridLayout) findViewById(R.id.gridlayout);
        for (int r = 0; r < 10; r++){
            for (int c = 0; c < 10; c++) {

                // Dynamically create an ImageView for each of the 100 spots
                  // They will be 95x95 in dimension and will be a light gray
                  // color when no thread has used the spot
                iv = new ImageView(getApplicationContext());
                iv.setBackgroundColor(Color.parseColor("#DCDCDC"));
                view.addView(iv, r);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.height = 95;
                lp.width = 95;
                lp.setMargins(5, 5, 5, 5);
                lp.setGravity(Gravity.CENTER_HORIZONTAL);
                lp.columnSpec = GridLayout.spec(c);
                lp.rowSpec = GridLayout.spec(r);
                iv.setLayoutParams(lp);

                // add this imageView to the visible board array
                visibleBoard[r][c] = iv;

                // Set each hiddenBoard piece to be -1 to signal it is empty
                hiddenBoard[r][c] = -1;
            }
        }

        // Randomly place the gopher in the hiddenBoard - signaled by a 0
        gopherPositionRow = rand.nextInt(10);
        gopherPositionColumn = rand.nextInt(10);

        hiddenBoard[gopherPositionRow][gopherPositionColumn] = 0;
    }

    // updateBoard: called after each move is done. Depending on the changes done
    // in the threads class, this will change the color of the square.
    private void updateBoard(){
        for (int r = 0; r < 10; r++){
            for (int c = 0; c < 10; c++){

                // No one has gotten here yet
                if (hiddenBoard[r][c] == -1){
                    visibleBoard[r][c].setBackgroundColor(Color.parseColor("#DCDCDC"));
                }

                // Thread 1 is there - change color to blue
                else if (hiddenBoard[r][c] == 1){
                    visibleBoard[r][c].setBackgroundColor(Color.parseColor("#ADD8E6"));
                }

                // Thread 2 is there - change color to green
                else if (hiddenBoard[r][c] == 2){
                    visibleBoard[r][c].setBackgroundColor(Color.parseColor("#3CB371"));
                }
            }
        }
    }

    // declareWinner: called if "SUCCESS" is sent. Updates the square to be a small gopher image,
    // and displays a textView at the bottom of the screen displaying the winner.
    private void declareWinner(){
        visibleBoard[gopherPositionRow][gopherPositionColumn].setImageResource(R.drawable.gopherwinner);

        if (hiddenBoard[gopherPositionRow][gopherPositionColumn] == 1){
            text.setText(R.string.t1Winner);
        }
        else if (hiddenBoard[gopherPositionRow][gopherPositionColumn] == 2){
            text.setText(R.string.t2Winner);
        }

        turnButton.setVisibility(View.GONE);
        mainMenuButton.setVisibility(View.VISIBLE);
    }

    // onDestroy: stops the threads from looping when activity is destroyed
    protected void onDestroy(){
        t1.t1Handler.getLooper().quit();
        t2.t2Handler.getLooper().quit();

        super.onDestroy();
    }
}


