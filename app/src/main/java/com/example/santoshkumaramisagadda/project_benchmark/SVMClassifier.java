package com.example.santoshkumaramisagadda.project_benchmark;

import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import weka.core.Instances;

import static com.example.santoshkumaramisagadda.project_benchmark.First_screen.percent_value;

public class SVMClassifier extends AppCompatActivity {

    static int percentage;

    String commandFile="command.txt";
    String command_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +commandFile;

    String modelFile="output.model";
    String model_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +modelFile;

    String test_name="test.arff";
    String test_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +test_name;

    String log_name="log";
    String log_location= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Project" + File.separator +log_name;

    static Button train_button;
    static Button test_button;
    static TextView noOfValidations;
    static TextView seed;

    Instances splitTest;

    private Spinner spinner;
    private static final String[] kernels = {"PolyKernel", "RBFKernel", "NormalizedPolyKernel"};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_svmclassifier);

        percentage= getIntent().getExtras().getInt("percentage");

        //noOfValidations = (TextView) findViewById(R.id.editText3);
        seed = (TextView) findViewById(R.id.editText2);
        noOfValidations = (TextView) findViewById(R.id.editText);

        spinner =(Spinner)findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SVMClassifier.this,
                android.R.layout.simple_spinner_item, kernels);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        train_button = (Button) findViewById(R.id.button4);
        test_button=(Button) findViewById(R.id.button6);

        test_button.setEnabled(false);

        train_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                try
                {
                    train_button.setEnabled(false);
                    Toast.makeText(SVMClassifier.this,"Model training, Please wait.",Toast.LENGTH_SHORT).show();
                    Thread.sleep(1000);
                    String cmd = "java -cp wekaSTRIPPED.jar weka.classifiers.functions.SMO -V "+ noOfValidations.getText()+" -W "+seed.getText()+" -K weka.classifiers.functions.supportVector."+spinner.getSelectedItem().toString()+" -t train.arff -d output.model";


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

                    //upload command text file
                    UploadService upload=new UploadService(SVMClassifier.this, getApplicationContext(),command_location,commandFile,train_button,0);
                    upload.startUpload();

                    test_button.setEnabled(true);

                }
                catch (Exception e)
                {
                    Toast.makeText(view.getContext(), "Error in storing the command file : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

        });


        test_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                DownloadService downloadService=new DownloadService(SVMClassifier.this,getApplicationContext(),test_button);
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

                        String output = "Classifier used: SVM , Input file: Breast.arff" + ",\nPercentage for training: " + percentage + ",\nAccuracy: " + eval.pctCorrect() + ",\nTime taken: " + test_time + ", milliseconds\nTrue positive: " + TPR + ",\nTrue negative: " + TNR + ",\nFalse Postive: " + FPR + ",\nFalse negative: " + FNR + ",\nHTER: " + HTER + ",\nRMSE: " + eval.rootMeanSquaredError();

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

                        Intent intent = new Intent(SVMClassifier.this, log.class);
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
