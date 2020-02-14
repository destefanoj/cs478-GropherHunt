/*
    Jessica DeStefano - WorkerThread1.java
    Contains move information for thread 1. Depending on which mode the game is in
     will determine what board and messages the thread is updating and sending to.
*/

package edu.uic.cs478.jdeste3.project4;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class WorkerThread1 extends Thread {
    public Handler t1Handler;
    public Handler uiHandler;

    private int row = 0;
    private int column = 0;
    private int mode;

    public WorkerThread1(Handler h, int m){
        uiHandler = h;
        mode = m;
    }

    // run: Creates a looper and makes a call to the move function, updating board in the parameter
     // depending on the mode
    public void run(){
        Looper.prepare();

        t1Handler = new Handler() {
            public void handleMessage(Message msg) {
                int what = msg.what;
                if (mode == 1){
                    switch (what) {
                        case GuessModeActivity.MAKE_MOVE:
                            t1Handler.post(new Runnable() {
                                public void run() {
                                    // make call to move function, and update the GuessMode board
                                    t1Move(GuessModeActivity.hiddenBoard);
                                }
                            });
                            break;
                    } // end switch
                }
                else if (mode == 2) {
                    switch (what) {
                        case ContModeActivity.MAKE_MOVE:
                            t1Handler.post(new Runnable() {
                                public void run() {
                                    // Sleep for 1 second and make a call to move function,
                                     // and update ContMode board
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        Log.i("Worker1", "Sleep over.");
                                    }
                                    t1Move(ContModeActivity.hiddenBoard);
                                }
                            });
                            break;
                    } // end switch
                }
            } // end handleMessage
        }; // end t1Handler

        Looper.loop();
    }

    // t1Move: Handles the moves made by the thread. Depending on the move, sends appropriate
     // message back to the main thread
    private void t1Move(int hiddenBoard[][]){
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

            // update row and col and return so we don't continue
            updateRowAndCol();
            return;
        }

        // Piece is the gopher, send SUCCESS
        if (hiddenBoard[row][column] == 0){
            hiddenBoard[row][column] = 1; // flags that this spot was t1

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

        // Update the board piece
        hiddenBoard[row][column] = 1;

        // if thread is within one spot, send NEAR_MISS
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
        }

        // change the row and column
        updateRowAndCol();
    }

    // checkForGopherOneSpot: determines if the current row, column is one spot
     // away from the gopher
    private boolean checkForGopherOneSpot(int hiddenBoard[][]){
        int spaceCount = 0;

        // Ensure we don't go out of bounds above the array
        if (row <= 9 && row != 0){
            // Ensure we don't go out of bounds to the left of the array
            if (column <= 9 && column != 0){
                // Check around
                if (hiddenBoard[row-1][column-1] == 0) { spaceCount ++; }
                if (hiddenBoard[row-1][column] == 0) { spaceCount++; }
                if (hiddenBoard[row][column-1] == 0) { spaceCount ++; }
            }
            // Ensure we don't go out of bounds to the right of the array
            if (column >= 0 && column != 9){
                // Check around
                if (hiddenBoard[row-1][column] == 0) { spaceCount ++; }
                if (hiddenBoard[row-1][column+1] == 0) { spaceCount++; }
                if (hiddenBoard[row][column+1] == 0) { spaceCount++; }
            }
        }

        // Ensure we don't go out of bounds below the array
        if (row >= 0 && row != 9){
            // Ensure we don't go out of bounds to the left of the array
            if (column <= 9 && column != 0){
                // Check around
                if (hiddenBoard[row+1][column-1] == 0) { spaceCount ++; }
                if (hiddenBoard[row+1][column] == 0) { spaceCount++; }
                if (hiddenBoard[row][column-1] == 0) { spaceCount++; }
            }
            // Ensure we don't go out of bounds to the right of the array
            if (column >= 0 && column != 9){
                // Check around
                if (hiddenBoard[row+1][column] == 0) { spaceCount ++; }
                if (hiddenBoard[row+1][column+1] == 0) { spaceCount++; }
                if (hiddenBoard[row][column+1] == 0) { spaceCount++; }
            }
        }

        if (spaceCount > 0) { return true; } // within one spot
        else { return false; } // not within one spot
    }

    // checkForGopherOneSpot: determines if the current row, column is two spots
    // away from the gopher
    private boolean checkForGopherTwoSpots(int hiddenBoard[][]){
        int spaceCount = 0;

        // Ensure we don't go out of bounds above the array
        if (row <= 9 && row != 1 && row != 0){
            // Ensure we don't go out of bounds to the left the array
            if (column <= 9 && column != 1 && column != 0){
                if (hiddenBoard[row-2][column-2] == 0) { spaceCount ++; }
                if (hiddenBoard[row-2][column-1] == 0) { spaceCount++; }
                if (hiddenBoard[row-2][column] == 0) { spaceCount++; }
                if (hiddenBoard[row-1][column-2] == 0) { spaceCount++; }
                if (hiddenBoard[row][column-2] == 0) { spaceCount ++; }
            }
            // Ensure we don't go out of bounds to the right the array
            if (column >= 0 && column != 8 && column != 9){
                if (hiddenBoard[row-2][column+2] == 0) { spaceCount ++; }
                if (hiddenBoard[row-2][column+1] == 0) { spaceCount++; }
                if (hiddenBoard[row-2][column] == 0) { spaceCount++; }
                if (hiddenBoard[row-1][column+2] == 0) { spaceCount++; }
                if (hiddenBoard[row][column+2] == 0) { spaceCount ++; }
            }
        }

        // Ensure we don't go out of bounds below the array
        if (row >= 0 && row != 8 && row != 9){
            // Ensure we don't go out of bounds to the left the array
            if (column <= 9 && column != 1 && column != 0){
                if (hiddenBoard[row+2][column-2] == 0) { spaceCount ++; }
                if (hiddenBoard[row+2][column-1] == 0) { spaceCount++; }
                if (hiddenBoard[row+2][column] == 0) { spaceCount++; }
                if (hiddenBoard[row+1][column-2] == 0) { spaceCount++; }
                if (hiddenBoard[row][column-2] == 0) { spaceCount ++; }
            }
            // Ensure we don't go out of bounds to the right the array
            if (column >= 0 && column != 8 && column != 9){
                if (hiddenBoard[row+2][column+2] == 0) { spaceCount ++; }
                if (hiddenBoard[row+2][column+1] == 0) { spaceCount++; }
                if (hiddenBoard[row+2][column] == 0) { spaceCount++; }
                if (hiddenBoard[row+1][column+2] == 0) { spaceCount++; }
                if (hiddenBoard[row][column+2] == 0) { spaceCount ++; }
            }
        }

        if (spaceCount > 0) { return true; } // within two spots
        else { return false; } // not within two spots
    }

    // updateRowAndCol: updates the row and column in a linear fashion
    private void updateRowAndCol(){
        if (column < 9) {
            column++;
        } else {
            column = 0;
            row++;
        }
    }

}