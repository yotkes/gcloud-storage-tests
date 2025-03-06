# Google Cloud Storage CLI Testing Framework

## 📌 Overview

This project is an **automated testing framework** for the **Google Cloud Storage CLI (`gcloud storage`)**.  
It validates the execution of key storage commands, including **bucket creation, file upload, file listing, file deletion, and signed URLs**.

---

## 🛠️ Prerequisites

Before running the tests, ensure you have the following installed:

1.  **[Google Cloud CLI](https://cloud.google.com/sdk/docs/install)**

    - Verify installation with:
      ```sh
      gcloud --version
      ```
    - If you don’t have a Google Cloud project, create one first:
      ```sh
      gcloud projects create YOUR_PROJECT_ID
      gcloud config set project YOUR_PROJECT_ID
      ```
    - Authenticate and set up your Google Cloud project:
      ```sh
      gcloud auth login
      gcloud config set project YOUR_PROJECT_ID
      ```

2.  **Enable Required Google Cloud APIs:**

    - Run:

    ```sh
    gcloud services enable storage.googleapis.com
    ```

3.  **[Maven](https://maven.apache.org/)** (For dependency management)

    - Verify installation with:
      ```sh
      mvn -version
      ```

4.  **Ensure You Have a Service Account Key** (if required)
    If authentication with a service account is needed:

    - **Move the key to a secure location**:
      ```sh
      mkdir -p ~/.gcp
      mv my-service-account-key.json ~/.gcp/
      ```
    - **Set the environment variable (for current session only):**
      ```sh
      export GOOGLE_APPLICATION_CREDENTIALS=~/.gcp/my-service-account-key.json
      ```
    - **Make the environment variable persistent across terminal sessions:**
      - **For Bash:**
        ```sh
        echo 'export GOOGLE_APPLICATION_CREDENTIALS=~/.gcp/my-service-account-key.json' >> ~/.bashrc
        source ~/.bashrc
        ```
      - **For Zsh ( - default on macOS):**
        ```sh
        echo 'export GOOGLE_APPLICATION_CREDENTIALS=~/.gcp/my-service-account-key.json' >> ~/.zshrc
        source ~/.zshrc
        ```
    - **Verify it’s set correctly:**
      ```sh
      echo $GOOGLE_APPLICATION_CREDENTIALS
      ```

5.  **Install Playwright for Signed URL Security Testing**

    - Install Playwright (required for signed URL security test)
      mvn dependency:resolve

    - Verify Playwright is installed:
      ```sh
      mvn dependency:tree | grep playwright|| echo "Playwright is missing! Add it to pom.xml."
      ```
    - If missing, add this to `pom.xml`:
      ```xml
      <dependency>
          <groupId>com.microsoft.playwright</groupId>
          <artifactId>playwright</artifactId>
          <version>1.39.0</version>
          <scope>test</scope>
      </dependency>
      ```
    - Reload Maven dependencies:
      `sh
  mvn clean install
  `

6.  **Ensure you are working in the correct Google Cloud project**

    ```sh
    gcloud config list --format="value(core.project)"
    ```

7.  **Ensure the Test Bucket and File Exist**

    ```sh
    gcloud storage buckets create gs://gcloud-storage-tests-bucket-1 --location=us-central1 || echo "Bucket already exists"
    echo "Sample test file for signed URL test" > test-file.txt
    gcloud storage cp test-file.txt gs://gcloud-storage-tests-bucket-1/
    ```

8.  **Clone the Repository**
    ```sh
    git clone https://github.com/yotkes/gcloud-storage-tests.git
    cd gcloud-storage-tests
    ```

---

## 🚀 Running Tests

### **Ensure the Test Bucket and File Exist**

Before running the tests, the required **Google Cloud Storage bucket** and `test-file.txt` must exist.
The test framework **automatically sets these up**, but you can create them manually if needed:

````sh
# Create the test bucket (if it doesn't exist)
gcloud storage buckets create gs://gcloud-storage-tests-bucket-1 --location=us-central1 || echo "Bucket already exists"

# Create and upload test-file.txt (for Signed URL test)
echo "Sample test file for signed URL test" > test-file.txt
gcloud storage cp test-file.txt gs://gcloud-storage-tests-bucket-1/

Once everything is set up, you can **run all tests** with:

```sh
mvn test
````

---

## 📂 Test Structure

The following tests validate key `gcloud storage` commands:

| **Test Name**           | **Command Tested**                                                           |
| ----------------------- | ---------------------------------------------------------------------------- |
| **Create Bucket**       | `gcloud storage buckets create gs://your-bucket-name --location=us-central1` |
| **Upload File**         | `gcloud storage cp local-file.txt gs://your-bucket-name/`                    |
| **List Files**          | `gcloud storage ls gs://your-bucket-name/`                                   |
| **Sign URL**            | `gcloud storage sign-url gs://your-bucket-name/object-name --duration=1h`    |
| **Signed URL Security** | Opens signed URL in Chrome to detect phishing warnings.                      |
| **Delete File**         | `gcloud storage rm gs://your-bucket-name/object-name`                        |

Tests are located in:  
📁 **`src/test/java/com/cloud/testing/GCloudStorageTests.java`**

---

## 🛠️ Expanding the Framework

- Add new test cases under **`src/test/java/com/cloud/testing/`**.
- Modify **`pom.xml`** to add additional dependencies if required.

---

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

---

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

**Important:**

- `testListFiles()` expects **a file to exist** before listing.
- `testDeleteFile()` **uploads a file before deleting it.**

---

### **3️⃣ Signed URL Security Test Fails**

#### Issue: The test gets stuck or fails with `"Cannot navigate to invalid URL"`

✅ **Fix:** Run Playwright in **headed mode** to debug:

```java
Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
```

✅ **Check if Chrome flags the URL as phishing:**

- Run `gcloud storage sign-url gs://your-bucket-name/test-file.txt --duration=1h`
- Open the **signed URL manually** in Chrome.
- If Chrome blocks the URL, **your test will fail**.

---

### **4️⃣ Viewing Test Results**

Test results are automatically generated in:

```
target/surefire-reports/
```

- View in **terminal**:
  ```sh
  cat target/surefire-reports/testng-results.xml
  ```
- View in a **browser** (if using an HTML plugin):
  ```sh
  open target/surefire-reports/index.html
  ```

---

## 📜 License

This project is for testing purposes and follows **Google Cloud's best practices**.

---
