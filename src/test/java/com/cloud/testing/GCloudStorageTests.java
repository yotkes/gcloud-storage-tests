
package com.cloud.testing;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GCloudStorageTests {

    @BeforeClass
    public void setup() {
        // Setup any required configurations (e.g., auth login)
    }

    @SuppressWarnings("deprecation")
    private String runCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        process.waitFor();
        return output.toString();
    }

    @Test
    public void testSignUrl() throws Exception {
        String bucketName = "your-bucket-name";
        String objectName = "test-file.txt";
        String signUrlCommand = "gcloud storage sign-url gs://" + bucketName + "/" + objectName + " --duration=1h";
        String result = runCommand(signUrlCommand);
        System.out.println("Signed URL: " + result);
        Assert.assertTrue(result.contains("https://"), "Signed URL not generated correctly.");
    }
}