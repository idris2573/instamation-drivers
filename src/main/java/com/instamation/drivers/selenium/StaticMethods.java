package com.instamation.drivers.selenium;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StaticMethods {

    public static List<String> checkChromeProcessPIDList(){
        String line;
        String[] info;
        String pid;
        List<String> processlist = new ArrayList<>();

        try {
            Process p;

            if(System.getProperty("os.name").equals("Linux")) {
                p = Runtime.getRuntime().exec("ps -e");
            } else {
                p = Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe");

            }

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {

                if(System.getProperty("os.name").equals("Linux")) {
                    if(line.contains("chrome")) {
                        pid = line.substring(0, line.indexOf(" pts"));
                        pid = pid.replace(" ", "");
                        if(!pid.isEmpty()) {
                            processlist.add(pid);
                        }
                    }
                } else {
                    if(line.contains("chrome.exe")){
                        info = line.split("[ ]{2,}");
                        processlist.add(info[1].split(" ")[0]);
                    }
                }

            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }

        return processlist;
    }

}
