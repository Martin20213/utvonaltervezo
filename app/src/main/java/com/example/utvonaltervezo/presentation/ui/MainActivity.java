package com.example.utvonaltervezo.presentation.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.utvonaltervezo.R;

public class MainActivity extends AppCompatActivity {

    private EditText startPointEditText, endPointEditText;
    private RadioGroup routeModeRadioGroup;
    private Button planRouteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI elemek
        startPointEditText = findViewById(R.id.startPointEditText);
        endPointEditText = findViewById(R.id.endPointEditText);
        routeModeRadioGroup = findViewById(R.id.routeModeRadioGroup);
        planRouteButton = findViewById(R.id.planRouteButton);

        // Útvonal tervezés gomb eseménykezelője
        planRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String startText = startPointEditText.getText().toString().trim();
                String endText = endPointEditText.getText().toString().trim();
        // Hibakezelés
                if (TextUtils.isEmpty(startText)) {
                    Toast.makeText(MainActivity.this, "Add meg a kiinduló pontot!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(endText)) {
                    Toast.makeText(MainActivity.this, "Add meg a célpontot!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: Térkép integrálása, útvonal kalkuláció és megjelenítés később

                Toast.makeText(MainActivity.this, "Útvonal tervezés indítása...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
