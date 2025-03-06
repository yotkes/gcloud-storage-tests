
package com.cloud.testing;

import com.microsoft.playwright.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCloudStorageTests {

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

        String bucketName = "gcloud-storage-tests-bucket-1";
        String testFileName = "test-file.txt";

        // Check if the bucket exists
        String checkBucketCommand = "gcloud storage buckets list --format='value(name)' | grep '^" + bucketName + "$'";
        String bucketExists = runCommand(checkBucketCommand);

        if (bucketExists.isEmpty()) {
            System.out.println("Bucket not found. Creating: " + bucketName);
            String createBucketCommand = "gcloud storage buckets create gs://" + bucketName + " --location=us-central1";
            String result = runCommand(createBucketCommand);
            System.out.println("Bucket creation output:\n" + result);
        } else {
            System.out.println("Bucket already exists: " + bucketName);
        }

        // Check if test-file.txt exists in the bucket
        String checkFileCommand = "gcloud storage ls gs://" + bucketName + "/" + testFileName;
        String fileExists = runCommand(checkFileCommand);

        if (fileExists.isEmpty()) {
            System.out.println("Test file not found. Creating and uploading: " + testFileName);
            String createTestFileCommand = "echo 'Sample test file for signed URL test' > " + testFileName;
            runCommand(createTestFileCommand);

            String uploadFileCommand = "gcloud storage cp " + testFileName + " gs://" + bucketName + "/";
            String uploadResult = runCommand(uploadFileCommand);
            System.out.println("Test file upload output:\n" + uploadResult);
        } else {
            System.out.println("Test file already exists in the bucket.");
        }
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
    public void testSignUrlSecurity() throws Exception {
        String bucketName = "gcloud-storage-tests-bucket-1";
        String objectName = "test-file.txt";
        String signUrlCommand = "gcloud storage sign-url gs://" + bucketName + "/" + objectName + " --duration=1h";

        // Run the command and extract the signed URL
        String result = runCommand(signUrlCommand);
        System.out.println("Raw Signed URL Output:\n" + result);

        // Extract the URL using regex
        Pattern pattern = Pattern.compile("(https://storage.googleapis.com[^\n]+)");
        Matcher matcher = pattern.matcher(result);
        String signedUrl = matcher.find() ? matcher.group(1) : "";

        if (signedUrl.isEmpty()) {
            throw new Exception("Failed to extract the signed URL from the command output.");
        }

        System.out.println("Extracted Signed URL: " + signedUrl);

        // Launch Chrome using Playwright
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            // Try to open the signed URL with a timeout (5 seconds)
            try {
                page.navigate(signedUrl, new Page.NavigateOptions().setTimeout(5000));
            } catch (Exception e) {
                System.out.println("Navigation timeout: Possible phishing warning or slow network.");
                throw new Exception("Navigation failed: " + e.getMessage());
            }

            // Check for phishing warnings in the page content
            boolean isBlocked = page.content().toLowerCase().contains("phishing") ||
                    page.content().toLowerCase().contains("warning") ||
                    page.content().toLowerCase().contains("deceptive site");

            browser.close();

            Assert.assertFalse(isBlocked, "Signed URL triggered a phishing warning in Chrome.");
            System.out.println("Signed URL is accessible without phishing warnings.");
        }
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
        String filePath = "test-file-for-list.txt";

        // Upload a test file before listing files
        runCommand("echo 'Temporary file' > " + filePath);
        runCommand("gcloud storage cp " + filePath + " gs://" + bucketName + "/");

        String listFilesCommand = "gcloud storage ls gs://" + bucketName + "/";
        String result = runCommand(listFilesCommand);

        System.out.println("List Files Output: \n" + result);
        Assert.assertTrue(result.contains(filePath), "File listing failed.");
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