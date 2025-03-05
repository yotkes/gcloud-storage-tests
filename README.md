# Google Cloud Storage CLI Testing Framework

## 📌 Overview

This project is an **automated testing framework** for the **Google Cloud Storage CLI**.  
It validates the execution of key storage commands, including **bucket creation, file upload, file listing, file deletion, and signed URLs**.

## 🛠️ Prerequisites

Before running the tests, ensure you have the following installed:

1. **[Google Cloud CLI](https://cloud.google.com/sdk/docs/install)**

   - Verify installation with:
     ```sh
     gcloud --version
     ```
   - Authenticate and set up your Google Cloud project:
     ```sh
     gcloud auth login
     gcloud config set project YOUR_PROJECT_ID
     ```

2. **Enable Required Google Cloud APIs:**  
   Run:

   ```sh
   gcloud services enable storage.googleapis.com
   ```

3. **[Maven](https://maven.apache.org/)** (For dependency management)

   - Verify installation with:
     ```sh
     mvn -version
     ```

4. **Ensure You Have a Service Account Key** (if required)  
   If authentication with a service account is needed:
   - **Move the key to a secure location**:
     ```sh
     mkdir -p ~/.gcp
     mv my-service-account-key.json ~/.gcp/
     ```
   - **Set the environment variable**:
     ```sh
     export GOOGLE_APPLICATION_CREDENTIALS=~/.gcp/my-service-account-key.json
     ```

## 🚀 Running Tests

Once everything is set up, you can **run all tests** with:

```sh


























mvn test
```

## 📂 Test Structure

The following tests validate key `gcloud storage` commands:

| **Test Name**     | **Command Tested**                                                           |
| ----------------- | ---------------------------------------------------------------------------- |
| **Create Bucket** | `gcloud storage buckets create gs://your-bucket-name --location=us-central1` |
| **Upload File**   | `gcloud storage cp local-file.txt gs://your-bucket-name/`                    |
| **List Files**    | `gcloud storage ls gs://your-bucket-name/`                                   |
| **Sign URL**      | `gcloud storage sign-url gs://your-bucket-name/object-name --duration=1h`    |
| **Delete File**   | `gcloud storage rm gs://your-bucket-name/object-name`                        |

Tests are located in:  
📁 **`src/test/java/com/cloud/testing/GCloudStorageTests.java`**

## Expanding the Framework

- Add new test cases under **`src/test/java/com/cloud/testing/`**.
- Modify **`pom.xml`** to add additional dependencies if required.

## 🛠️ Troubleshooting

### **1️⃣ Authentication Issues**

**Error:** `You do not have permission to access this resource`  
✅ **Fix:** Make sure you’re authenticated:

```sh
gcloud auth login
```

If using a **service account**, set up authentication:

```sh


export GOOGLE_APPLICATION_CREDENTIALS=~/.gcp/my-service-account-key.json
```

### **2️⃣ Test Failures Due to Missing Resources**

**Error:** `Bucket does not exist`  
✅ **Fix:** Create a test bucket manually:

```sh
gcloud storage buckets create gs://your-bucket-name --location=us-central1
```

**Error:** `File not found in bucket`  
✅ **Fix:** Upload a test file:

```sh


echo "Test content" > test-file.txt
gcloud storage cp test-file.txt gs://your-bucket-name/
```

## 📜 License

This project is for testing purposes and follows **Google Cloud's best practices**.
