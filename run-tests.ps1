# 
# Copyright © 2025 CDAP
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
#

# Run-Tests.ps1
# Script to run tests for the wrangler project from any directory

param (
    [Parameter(Mandatory=$false)]
    [string]$TestName = "",
    
    [Parameter(Mandatory=$false)]
    [string]$Module = ""
)

if ($TestName -eq "") {
    Write-Host "Running all tests..."
    mvn test
    exit 0
}

# Check if TestName contains commas (multiple tests)
if ($TestName -like "*,*") {
    # Process each test separately
    $TestNames = $TestName -split ","
    $ProcessedTests = @()
    
    foreach ($Test in $TestNames) {
        # Add common package prefixes if not provided
        if (-not $Test.Contains(".")) {
            # Check if this is a parser test
            if ($Test -match "ByteSize|TimeDuration") {
                $ProcessedTests += "io.cdap.wrangler.parser.$Test"
            } else {
                $ProcessedTests += $Test
            }
        } else {
            $ProcessedTests += $Test
        }
    }
    
    # Join the processed tests back with commas
    $TestName = $ProcessedTests -join ","
} else {
    # Add common package prefixes if not provided
    if (-not $TestName.Contains(".")) {
        # Check if this is a parser test
        if ($TestName -match "ByteSize|TimeDuration") {
            $TestName = "io.cdap.wrangler.parser.$TestName"
        }
    }
}

# Run tests in specific module or all modules
if ($Module -ne "") {
    Write-Host "Running test $TestName in module $Module..."
    cd $Module
    mvn test "-Dtest=$TestName"
    cd ..
} else {
    Write-Host "Running test $TestName in all modules..."
    mvn test "-Dtest=$TestName"
}
