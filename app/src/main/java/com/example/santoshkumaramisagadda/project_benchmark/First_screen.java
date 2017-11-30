package com.example.santoshkumaramisagadda.project_benchmark;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class First_screen extends AppCompatActivity {

    static SeekBar percent;
    static TextView percent_val;
    static Button btn_next;
    static int percent_value;
    static int classifier=0;

    String arff_name="breast.arff";
    String arff_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +arff_name;

    String test_name="test.arff";
    String test_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +test_name;

    String train_name="train.arff";
    String train_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +train_name;

    String log_name="log";
    String log_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project"+ File.separator +log_name;

    Instances splitTrain,splitTest,data;

    Intent intent;

    private Spinner spinner;
    private static final String[] paths = {"SVM Classifier", "Random forest Classifier", "Bayes Network Classifier","MLP Classifier"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);

        if(!new File(log_location).exists())
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(new File(log_location)));
                out.write("Log File for experiments");
                out.newLine();

                //adding initial headers to indicate form of output experiments
                out.write("Output format for SVM - Classifier Type,Dataset, Training Size %, Accuracy, Time taken for testing, TAR, TRR, FAR, FRR, HTER, Execution Time, RMSE");
                out.newLine();
                out.newLine();
                out.write("Output format for MLP - Classifier Type,Dataset,Training Size %, Accuracy, Time taken for testing, TAR, TRR, FAR, FRR, HTER, Execution Time, RMSE");
                out.newLine();
                out.newLine();
                out.write("Output format for Random Forests - Classifier Type,Dataset,Training Size %,Accuracy, Time taken for testing, TAR, TRR, FAR, FRR, HTER, Execution Time, RMSE");
                out.newLine();
                out.newLine();
                out.write("Output format for Bayesian Network - Classifier Type,Dataset,Training Size %,Accuracy, Time taken for testing, TAR, TRR, FAR, FRR, HTER, Execution Time, RMSE");
                out.newLine();
                out.newLine();
                out.write("***********----------***********");
                out.newLine();
                out.close();
            }catch (Exception e)
            {
                e.printStackTrace();
            }


        percent = (SeekBar) findViewById(R.id.seekBar);
        percent_val = (TextView) findViewById(R.id.textView2);
        percent.setMax(99);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            percent.setMin(1);
        }

        percent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                // TODO Auto-generated method stub
                if(progress==0)
                    progress=1;
                percent_val.setText(String.valueOf(progress) + "%");
                percent_value= progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                // TODO Auto-generated method stub
            }
        });

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(First_screen.this,
                android.R.layout.simple_spinner_item, paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        classifier=1;
                        break;
                    case 1:
                        classifier=2;
                        break;
                    case 2:
                        classifier=3;
                        break;
                    case 3:
                        classifier=4;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });


        btn_next=(Button)findViewById(R.id.button);
        btn_next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    //split the database
                    splitData(percent_value);

                    //upoad the train.arff file
                    UploadService upload=new UploadService(First_screen.this,getApplicationContext(),train_location,train_name,btn_next,0);
                    upload.startUpload();

                    switch (classifier)
                    {
                        case 1:
                            intent= new Intent(First_screen.this,SVMClassifier.class);
                            intent.putExtra("percentage",percent_value);
                            startActivity(intent);
                            break;
                        case 2:
                            intent= new Intent(First_screen.this,RFClassifier.class);
                            intent.putExtra("percentage",percent_value);
                            startActivity(intent);
                            break;
                        case 3:
                            intent= new Intent(First_screen.this,BNClassifier.class);
                            intent.putExtra("percentage",percent_value);
                            startActivity(intent);
                            break;
                        case 4:
                            intent= new Intent(First_screen.this,MLPClassifier.class);
                            intent.putExtra("percentage",percent_value);
                            startActivity(intent);
                            break;

                    }


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void splitData(int x) throws IOException
    {
        BufferedReader inputReader=new BufferedReader(new FileReader(arff_location));
        data=new Instances(inputReader);
        data.setClassIndex(data.numAttributes() - 1);
        data.randomize(new java.util.Random());

        int dataSize=data.numInstances();

        splitTrain = new Instances(data,0);
        splitTrain.setClassIndex(splitTrain.numAttributes() - 1);

        splitTest = new Instances(data,0);
        splitTest.setClassIndex(splitTest.numAttributes() - 1);

        int trainingSize=x*dataSize/100;

        for(int i=0;i<trainingSize;i++)
        {
            splitTrain.add(data.instance(i));
        }

        for(int i=trainingSize;i<dataSize;i++)
        {
            splitTest.add(data.instance(i));
        }


        BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File(train_location)));
        writer1.write(splitTrain.toString());
        writer1.flush();
        writer1.close();

        BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File(test_location)));
        writer2.write(splitTest.toString());
        writer2.flush();
        writer2.close();
    }
}
