# Takes 2 argument:
# $1: the test(s) name.
# $2: the output file where the result code will be written.

# ================= Simulating testing code =================
echo "testing $1 ..."
sleep 5

echo "$?" >> "$2"     # The return code of the `sleep` cmd (simulating test cmd).`
# ============================================================

# ================= Actual testing code =================
#echo "y" | gcloud beta firebase test android run \
#--app app/build/outputs/apk/mock/debug/app-mock-debug.apk \                                # To be filled with actual path
#--test app/build/outputs/apk/androidTest/mock/debug/app-mock-debug-androidTest.apk \       # To be filled with actual path
#--device model=Nexus5,version=19,locale=en,orientation=portrait \
#--device model=Nexus6,version=21,locale=en,orientation=portrait \
#--device model=Nexus6P,version=23,locale=en,orientation=portrait \
#--use-orchestrator \
#--timeout 30m \
#--results-bucket "my-bucket" \                                                             # To be filled with actual argument
#--test-targets="$1";

#echo "$?" >> "$2"     # The return code of Firebase cmd `gcloud beta firebase test...`
# ============================================================
