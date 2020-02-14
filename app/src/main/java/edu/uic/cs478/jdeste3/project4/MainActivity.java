/*
    Jessica DeStefano - MainActivity.java
    Contains two buttons that launch the different modes of the game
*/

package edu.uic.cs478.jdeste3.project4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView gopher = (ImageView) findViewById(R.id.gopher);
        gopher.setImageResource(R.drawable.gopher);
    }

    // On click listener for Guess Mode
    public void launchGuessMode(View v){
        Intent intent = new Intent(this, GuessModeActivity.class);
        startActivity(intent);
    }

    // On click listener for continuous mode
    public void launchContinuousMode(View v){
        Intent intent = new Intent(this, ContModeActivity.class);
        startActivity(intent);
    }
}
