
package com.cloud.testing;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class GCloudStorageTests {

    @BeforeClass
    public void setup() {
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
    }

    @SuppressWarnings("deprecation")
    private String runCommand(String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        processBuilder.redirectErrorStream(true); // Merges stdout and stderr

        Process process = processBuilder.start();
        process.waitFor(); // Wait for the process to finish before reading output

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        return output.toString().trim();
    }

    @Test
    public void testSignUrl() throws Exception {
        String bucketName = "gcloud-storage-tests-bucket-1";
        String objectName = "test-file.txt";
        String signUrlCommand = "gcloud storage sign-url gs://" + bucketName + "/" + objectName + " --duration=1h";
        String result = runCommand(signUrlCommand);
        System.out.println("Signed URL: " + result);
        Assert.assertTrue(result.contains("https://"), "Signed URL not generated correctly.");
    }

    @Test
    public void testCreateBucket() throws Exception {
        String bucketName = String.format("gcloud-bucket-%d", System.currentTimeMillis() % 1000000);
        System.out.println("Attempting to create bucket: " + bucketName); // Debugging

        String createBucketCommand = "gcloud storage buckets create gs://" + bucketName + " --location=us-central1";
        String result = runCommand(createBucketCommand);

        System.out.println("Create Bucket Command Output:\n" + result); // Debugging line

        Assert.assertTrue(
                result.toLowerCase().contains("creating") || result.toLowerCase().contains("created")
                        || result.toLowerCase().contains("already exists"),
                "Bucket creation failed.");
    }

    @Test
    public void testUploadFile() throws Exception {
        String bucketName = "gcloud-storage-tests-bucket-1";
        String filePath = "test-file-2.txt";

        // Create the test file
        runCommand("echo 'Test content' > " + filePath);
        File testFile = new File(filePath);
        Assert.assertTrue(testFile.exists(), "File creation failed!");

        System.out.println("Created test file: " + filePath);

        // Upload the file
        String uploadCommand = "gcloud storage cp " + filePath + " gs://" + bucketName + "/";

        long startTime = System.currentTimeMillis();
        String result = runCommand(uploadCommand);
        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("Upload File Command Output:\n" + result);
        System.out.println("Upload Time: " + elapsedTime + "ms");

        // **NEW: Check if the file appears in Cloud Storage**
        String listCommand = "gcloud storage ls gs://" + bucketName + "/";
        String listResult = runCommand(listCommand);
        System.out.println("List Files Output:\n" + listResult);

        Assert.assertTrue(listResult.contains("test-file-2.txt"),
                "File upload failed or not visible in Cloud Storage.");
    }

    @Test
    public void testListFiles() throws Exception {
        String bucketName = "gcloud-storage-tests-bucket-1";
        String listFilesCommand = "gcloud storage ls gs://" + bucketName + "/";

        String result = runCommand(listFilesCommand);
        System.out.println("List Files Output: \n" + result);

        Assert.assertTrue(result.contains("test-file.txt"), "File listing failed.");
    }

    @Test
    public void testDeleteFile() throws Exception {
        String bucketName = "gcloud-storage-tests-bucket-1";
        String filePath = "test-file-to-delete.txt";

        // Create a test file and upload it
        runCommand("echo 'Temporary file' > " + filePath);
        runCommand("gcloud storage cp " + filePath + " gs://" + bucketName + "/");
        System.out.println("Uploaded test file: " + filePath);

        // Delete the file
        String deleteCommand = "gcloud storage rm gs://" + bucketName + "/" + filePath;
        String result = runCommand(deleteCommand);

        System.out.println("Delete File Command Output:\n" + result);

        // Verify the file has been deleted
        String listCommand = "gcloud storage ls gs://" + bucketName + "/";
        String listResult = runCommand(listCommand);
        System.out.println("List Files Output After Deletion:\n" + listResult);

        Assert.assertFalse(listResult.contains(filePath),
                "File deletion failed. The file is still present in Cloud Storage.");
    }

}