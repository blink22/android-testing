#!/usr/bin/env bash
# Takes 1 argument:
# $1: file containing tests, one test case per line.

CORES_COUNT=`cat /proc/cpuinfo | grep processor | wc -l`
PARALLEL_JOBS=$((CORES_COUNT > 16 ? CORES_COUNT : 16)) # at least 16 parallel jobs
TESTS_COUNT=`wc -l < $1`
TESTS_PER_JOB=$(( (TESTS_COUNT + PARALLEL_JOBS - 1) / PARALLEL_JOBS )) # round up division

# Pre-process input test file
# Prepend "class " to each line, then concat each "$TESTS_PER_JOB" lines to be comma separated to be executed in 1 run.
cat "$1" | while read line; do echo "class $line"; done | paste -d, $(for i in `seq 1 $TESTS_PER_JOB`; do printf "%b" "- "; done) > intermediate-test-file
sed 's/,*$//' intermediate-test-file > batch-test-file # Remove trailing commas, if any, at the end of  each line

# Run tests simultaneously
echo y | apt-get install parallel	# Install GNU parallel tool
TESTS_RESULT_FILE="result-file"
cat batch-test-file | parallel -j0 "bash firebase-run-test.sh {} $TESTS_RESULT_FILE" # "-j0" run as many jobs as needed

# Check overall result of all tests
OVERALL_TESTS_RESULT=`awk '{s+=$1} END {print s}' $TESTS_RESULT_FILE` # Sum result codes all test cmds
echo "The result of running all tests is $OVERALL_TESTS_RESULT"