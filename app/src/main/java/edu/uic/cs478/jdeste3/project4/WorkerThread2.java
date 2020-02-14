/*
    Jessica DeStefano - WorkerThread2.java
    Contains move information for thread 2. Depending on which mode the game is in
     will determine what board and messages the thread is updating and sending to.
     When making moves, thread has more intelligence - will update its move to be closer
     to the gopher when it knows that the gopher is near.
*/

package edu.uic.cs478.jdeste3.project4;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.Random;

public class WorkerThread2 extends Thread {
    public Handler t2Handler;
    public Handler uiHandler;

    private int row = 0;
    private int column = 0;
    private int mode;

    private Random rand = new Random();

    public WorkerThread2(Handler h, int m){
        uiHandler = h;
        mode = m;
    }

    // run: Creates a looper and makes a call to the move function, updating board in the parameter
    // depending on the mode
    public void run(){
        Looper.prepare();

        t2Handler = new Handler(){
            public void handleMessage(Message msg){
                int what = msg.what;
                if (mode == 1){
                    switch (what) {
                        case GuessModeActivity.MAKE_MOVE:
                            t2Handler.post(new Runnable() {
                                public void run() {
                                    // make call to move function, and update the GuessMode board
                                    t2Move(GuessModeActivity.hiddenBoard);
                                }
                            });
                            break;
                    } // end switch
                }
                else if (mode == 2) {
                    switch (what) {
                        case ContModeActivity.MAKE_MOVE:
                            t2Handler.post(new Runnable() {
                                public void run() {
                                    // Sleep for 1 second and make a call to move function,
                                    // and update ContMode board
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        Log.i("Worker2", "Sleep over.");
                                    }
                                    t2Move(ContModeActivity.hiddenBoard);
                                }
                            });
                            break;
                    } // end switch
                }
            } // end handleMessage
        }; // end t1Handler

        Looper.loop();
    }

    // t2Move: Handles the moves made by the thread. Depending on the move, sends appropriate
    // message back to the main thread
    private void t2Move(int hiddenBoard[][]){
        Message message;

        // Piece is already in use, send DISASTER
        if (hiddenBoard[row][column] == 1 || hiddenBoard[row][column] == 2){
            if (mode == 1) {
                message = uiHandler.obtainMessage(GuessModeActivity.DISASTER);
                uiHandler.sendMessage(message);
            }
            if (mode == 2) {
                message = uiHandler.obtainMessage(ContModeActivity.DISASTER);
                uiHandler.sendMessage(message);
            }

            // Update row and column and return so we don't continue
            updateRowAndCol();
            return;
        }

        // Piece is the gopher, send SUCCESS
        if (hiddenBoard[row][column] == 0){
            hiddenBoard[row][column] = 2; // flags this spot was t2

            if (mode == 1) {
                message = uiHandler.obtainMessage(GuessModeActivity.SUCCESS);
                uiHandler.sendMessage(message);
            }
            if (mode == 2) {
                message = uiHandler.obtainMessage(ContModeActivity.SUCCESS);
                uiHandler.sendMessage(message);
            }

            // return so we don't continue
            return;
        }

        // update the board piece
        hiddenBoard[row][column] = 2;

        // if thread is within one spot, send CLOSE_GUESS
        if (checkForGopherOneSpot(hiddenBoard)){
            if (mode == 1) {
                message = uiHandler.obtainMessage(GuessModeActivity.NEAR_MISS);
                uiHandler.sendMessage(message);
            }
            if (mode == 2) {
                message = uiHandler.obtainMessage(ContModeActivity.NEAR_MISS);
                uiHandler.sendMessage(message);
            }
        }

        // if thread is within two spots, send CLOSE_GUESS
        else if (checkForGopherTwoSpots(hiddenBoard)){
            if (mode == 1) {
                message = uiHandler.obtainMessage(GuessModeActivity.CLOSE_GUESS);
                uiHandler.sendMessage(message);
            }
            if (mode == 2) {
                message = uiHandler.obtainMessage(ContModeActivity.CLOSE_GUESS);
                uiHandler.sendMessage(message);
            }
        }

        // otherwise, thread is somewhere else, send COMPLETE_MISS
        else{
            if (mode == 1) {
                message = uiHandler.obtainMessage(GuessModeActivity.COMPLETE_MISS);
                uiHandler.sendMessage(message);
            }
            if (mode == 2) {
                message = uiHandler.obtainMessage(ContModeActivity.COMPLETE_MISS);
                uiHandler.sendMessage(message);
            }

            // change the row and column
            updateRowAndCol();
        }
    }

    // checkForGopherOneSpot: determines if the current row, column is one spot
    // away from the gopher. If it is, the row and column is updated to be that spot
    private boolean checkForGopherOneSpot(int hiddenBoard[][]){
        if (row <= 9 && row != 0){
            if (column <= 9 && column != 0){
                if (hiddenBoard[row-1][column-1] == 0) {
                    row--;
                    column--;
                    return true;
                }
                if (hiddenBoard[row-1][column] == 0) {
                    row--;
                    return true;
                }
                if (hiddenBoard[row][column-1] == 0) {
                    column--;
                    return true;
                }
            }
            if (column >= 0 && column != 9){
                if (hiddenBoard[row-1][column] == 0) {
                    row--;
                    return true;
                }
                if (hiddenBoard[row-1][column+1] == 0) {
                    row--;
                    column++;
                    return true;
                }
                if (hiddenBoard[row][column+1] == 0) {
                    column++;
                    return true;
                }
            }
        }

        if (row >= 0 && row != 9){
            if (column <= 9 && column != 0){
                if (hiddenBoard[row+1][column-1] == 0) {
                    row++;
                    column--;
                    return true;
                }
                if (hiddenBoard[row+1][column] == 0) {
                    row++;
                    return true;
                }
                if (hiddenBoard[row][column-1] == 0) {
                    column--;
                    return true;
                }
            }

            if (column >= 0 && column != 9){
                if (hiddenBoard[row+1][column] == 0) {
                    row++;
                    return true;
                }
                if (hiddenBoard[row+1][column+1] == 0) {
                    row++;
                    column++;
                    return true;
                }
                if (hiddenBoard[row][column+1] == 0) {
                    column++;
                    return true;
                }
            }
        }
        return false;
    }

    // checkForGopherOneSpot: determines if the current row, column is two spots
    // away from the gopher. If it is, row and column is updated to be one spot
    // away from here.
    private boolean checkForGopherTwoSpots(int hiddenBoard[][]){

        // Ensure we don't go out of bounds above the array
        if (row <= 9 && row != 1 && row != 0){
            // Ensure we don't go out of bounds to the left the array
            if (column <= 9 && column != 1 && column != 0){
                if (hiddenBoard[row-2][column-2] == 0) {
                    row--;
                    column--;
                    return true;
                }
                if (hiddenBoard[row-2][column-1] == 0) {
                    row--;
                    column--;
                    return true;
                }
                if (hiddenBoard[row-2][column] == 0) {
                    row --;
                    return true;
                }
                if (hiddenBoard[row-1][column-2] == 0) {
                    row--;
                    column--;
                    return true;
                }
                if (hiddenBoard[row][column-2] == 0) {
                    row--;
                    column--;
                    return true;
                }
            }

            // Ensure we don't go out of bounds to the right the array
            if (column >= 0 && column != 8 && column != 9){
                // Check around
                if (hiddenBoard[row-2][column+2] == 0) {
                    row--;
                    column++;
                    return true;
                }
                if (hiddenBoard[row-2][column+1] == 0) {
                    row--;
                    column++;
                    return true;
                }
                if (hiddenBoard[row-2][column] == 0) {
                    row--;
                    return true;
                }
                if (hiddenBoard[row-1][column+2] == 0) {
                    row--;
                    column++;
                    return true;
                }
                if (hiddenBoard[row][column+2] == 0) {
                    column++;
                    return true;
                }
            }
        }

        // Ensure we don't go out of bounds below the array
        if (row >= 0 && row != 8 && row != 9){
            // Ensure we don't go out of bounds to the left the array
            if (column <= 9 && column != 1 && column != 0){
                // Check around
                if (hiddenBoard[row+2][column-2] == 0) {
                    row++;
                    column--;
                    return true;
                }
                if (hiddenBoard[row+2][column-1] == 0) {
                    row++;
                    column--;
                    return true;
                }
                if (hiddenBoard[row+2][column] == 0) {
                    row++;
                    return true;
                }
                if (hiddenBoard[row+1][column-2] == 0) {
                    row++;
                    column--;
                    return true;
                }
                if (hiddenBoard[row][column-2] == 0) {
                    column--;
                    return true;
                }
            }

            // Ensure we don't go out of bounds to the right the array
            if (column >= 0 && column != 8 && column != 9){
                // Check around
                if (hiddenBoard[row+2][column+2] == 0) {
                    row++;
                    column++;
                    return true;
                }
                if (hiddenBoard[row+2][column+1] == 0) {
                    row++;
                    column++;
                    return true;
                }
                if (hiddenBoard[row+2][column] == 0) {
                    row++;
                    return true;
                }
                if (hiddenBoard[row+1][column+2] == 0) {
                    row++;
                    column++;
                    return true;
                }
                if (hiddenBoard[row][column+2] == 0) {
                    column++;
                    return true;
                }
            }
        }

        return false;
    }

    // updateRowAndCol: updates row and column in a random fashion
    private void updateRowAndCol(){
        row = rand.nextInt(10);
        column = rand.nextInt(10);
    }
}