package com.example.santoshkumaramisagadda.project_benchmark;

import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.misc.SerializedClassifier;
import weka.core.Instances;
import weka.classifiers.functions.MultilayerPerceptron;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class RFClassifier extends AppCompatActivity
{

    String commandFile="command.txt";
    String command_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +commandFile;

    String modelFile="output.model";
    String model_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +modelFile;

    String test_name="test.arff";
    String test_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +test_name;

    String log_name="log";
    String log_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +log_name;

    static int percentage;
    static Button train_button;
    static Button test_button;
    static TextView noOfIterations;
    static TextView depth;
    static TextView seed;
    static TextView noOfAttributes;
    
    Instances splitTest;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfclassifier);
        percentage= getIntent().getExtras().getInt("percentage");
        noOfIterations = (TextView) findViewById(R.id.editText3);
        seed = (TextView) findViewById(R.id.editText4);
        depth = (TextView) findViewById(R.id.editText5);
        noOfAttributes = (TextView) findViewById(R.id.editText6);

        train_button = (Button) findViewById(R.id.button3);
        test_button=(Button) findViewById(R.id.button5);

        test_button.setEnabled(false);
        train_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (noOfIterations.getText() != "" && seed.getText() != "" && depth.getText() != "" && noOfAttributes.getText() != "")
                {
                    try
                    {
                        train_button.setEnabled(false);
                        Toast.makeText(RFClassifier.this,"Model training, Please wait.",Toast.LENGTH_SHORT).show();
                        Thread.sleep(1000);
                        String cmd = "java -cp wekaSTRIPPED.jar weka.classifiers.trees.RandomForest -I "+noOfIterations.getText()+" -S "+seed.getText()+" -depth "+depth.getText()+" -K "+noOfAttributes.getText()+" -t train.arff -d output.model";

                        //delete both output.model and command.txt
                        File file1=new File(command_location);
                        if(file1.exists())
                            file1.delete();

                        File file2=new File(model_location);
                        if(file2.exists())
                            file2.delete();

                        BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File(command_location)));
                        writer1.write(cmd);
                        writer1.flush();
                        writer1.close();

                        //upload command.txt
                        UploadService upload=new UploadService(RFClassifier.this,getApplicationContext(),command_location,commandFile,train_button,0);
                        upload.startUpload();

                        test_button.setEnabled(true);

                    } catch (Exception e) {
                        Toast.makeText(view.getContext(), "Error in storing the command file : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(view.getContext(), "Please enter all the fields", Toast.LENGTH_LONG).show();
                }


            }

        });

        test_button=(Button) findViewById(R.id.button5);
        test_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                DownloadService downloadService=new DownloadService(RFClassifier.this,getApplicationContext(),test_button);
                downloadService.startDownload();

                try {
                    File model=new File(model_location);
                    while(!model.exists()){
                        model=new File(model_location);
                    }
                    testData();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void testData() throws IOException {
        BufferedReader inputReader=new BufferedReader(new FileReader(test_location));
        splitTest=new Instances(inputReader);
        try
        {
            long start=System.currentTimeMillis();
            splitTest.setClassIndex(splitTest.numAttributes() - 1 );
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(model_location));
            Classifier cls = (Classifier) ois.readObject();
            ois.close();
            final Evaluation eval = new Evaluation(splitTest);
            eval.evaluateModel(cls, splitTest);
            final long test_time= System.currentTimeMillis()-start;


            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        //calculating TPR, TNR etc
                        double TPR = eval.truePositiveRate(0);
                        double TNR = eval.trueNegativeRate(0);
                        double FPR = eval.falsePositiveRate(0);
                        double FNR = eval.falseNegativeRate(0);
                        double HTER = (FPR + FNR) / 2;

                        String output = "Classifier used: RF Classifier, Input file: Breast.arff" + ",\nPercentage for training: " + percentage + ",\nAccuracy: " + eval.pctCorrect() + ",\nTime taken: " + test_time + ", milliseconds\nTrue positive: " + TPR + ",\nTrue negative: " + TNR + ",\nFalse Postive: " + FPR + ",\nFalse negative: " + FNR + ",\nHTER: " + HTER + ",\nRMSE: " + eval.rootMeanSquaredError();

                        while(output==null)
                        {
                            Thread.sleep(500);
                        }

                        BufferedReader br = new BufferedReader(new FileReader(new File(log_location)));
                        String line;
                        StringBuilder builder=new StringBuilder();
                        while((line=br.readLine())!=null)
                        {
                            builder.append(line);
                            builder.append("\n");
                        }

                        builder.append(output);

                        BufferedWriter out = new BufferedWriter(new FileWriter(new File(log_location)));
                        out.write(String.valueOf(builder));
                        out.newLine();
                        out.newLine();
                        out.close();

                        Intent intent = new Intent(RFClassifier.this, log.class);
                        intent.putExtra("output",output);
                        startActivity(intent);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
