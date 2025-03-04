# Google Cloud Storage CLI Testing Framework

## Setup

1. Install [Google Cloud CLI](https://cloud.google.com/sdk/docs/install).
2. Authenticate using `gcloud auth login` and set your project: `gcloud config set project YOUR_PROJECT_ID`.
3. Install [Maven](https://maven.apache.org/) if not installed.

## Running Tests

```sh
mvn test
```

```

## Test Structure

- **`GCloudStorageTests.java`** contains automated tests for `gcloud storage` commands.
- Uses TestNG for test execution.
- Uses Playwright for browser-based validation.

## Expanding the Framework

- Add more test cases in `src/test/java/com/cloud/testing/`.
- Update `pom.xml` to include more dependencies if needed.
```
