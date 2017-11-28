<?php
 
    $file_path = "/Applications/MAMP/htdocs/Upload/";
    //$classifier=$_POST['Classifier'];
    $file_path = $file_path . basename( $_FILES['uploaded_file']['name']);
    if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
        //if(strcmp(['classifier'],"1"))
        if(file_exists("command.txt"))
        {
            $fh = fopen('command.txt','r');
            while ($line = fgets($fh)) 
            {
                echo $line;
                $output = shell_exec($line);
            }
            fclose($fh);
            //$output = shell_exec("java -cp wekaSTRIPPED.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S 0 -E 20 -H a -t trial1.arff -d output1.model  ");
        }
        echo "success";
    } else{
        echo "fail";
    }
    system("./AppFinder.o");
 ?>