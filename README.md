# Run instrumentation tests concurrently on Firebase test lab
This repository shows how you can severely reduce the time taken to run instrumentation tests on cloud-based app-testing infrastructure like Firebase test lab. It forks an [Android testing codelab](https://github.com/googlecodelabs/android-testing) to act as a sample project to show a demo.

The idea is simply based on:
1) Importing an [annotation processor](https://github.com/blink22/android-testing/wiki/Mechanism-of-the-solution#annotation-processor), in your project, which creates a file with the full names of all the instrumentation test cases at the build time.
2) Run a [bash script](https://github.com/blink22/android-testing/wiki/Mechanism-of-the-solution#parallel-execution-of-shell-scripts) which executes those tests concurrently on Firebase test lab. 

### Getting Started
These are the steps to to integrate the tweak to the sample project [Android testing codelab](https://github.com/googlecodelabs/android-testing):
1. Import the annotation processor module as shown in this [commit](https://github.com/blink22/android-testing/commit/9229584e8b1fffdc9c51d40126f1aeea7181fa8b).
2. Add [these bash scripts](https://github.com/blink22/android-testing/commit/e80e5af8e9f34a21a101f7bc8e1f538ee0c62d92) to the root of the project.
3. Uncomment this [bash code](https://github.com/blink22/android-testing/commit/e80e5af8e9f34a21a101f7bc8e1f538ee0c62d92#diff-6e2bcd2d1f99813f25d89faebd67efb1R12) after doing the necessary substitution. 
4. Make sure you have [configured local gcloud sdk environment](https://firebase.google.com/docs/test-lab/android/command-line#configure_your_local_google_cloud_sdk_environment).
5. Execute the following bash code in the project root directory:
```
./gradlew clean
./gradlew assembleMockDebug # builds app APK
./gradlew assembleMockDebugAndroidTest # builds ui-test APK and generates "ui-tests" file
bash firebase-run-tests-concurrently.sh ui-tests  
```

