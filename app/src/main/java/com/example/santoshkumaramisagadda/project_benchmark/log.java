package com.example.santoshkumaramisagadda.project_benchmark;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class log extends AppCompatActivity {

    String output;
    String log_name="log";
    String log_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +log_name;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        output=getIntent().getStringExtra("output");
        final TextView tv1=(TextView)findViewById(R.id.textView11);
        tv1.setText(output);

        Button log_button=(Button)findViewById(R.id.button2);

        log_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view) {
                try {
                    //opening a Buffered Reader to read the file line by line.
                    BufferedReader br = new BufferedReader(new FileReader(new File(log_location)));
                    String line;
                    StringBuilder builder = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        builder.append(line);
                        builder.append("\n");
                    }
                    tv1.setText(builder.toString());
                } catch (Exception e) {
                    Toast.makeText(log.this, "Error Reading File", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }
}
