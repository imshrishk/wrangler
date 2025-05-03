@echo off
::
:: Copyright © 2025 CDAP
::
:: Licensed under the Apache License, Version 2.0 (the "License"); you may not
:: use this file except in compliance with the License. You may obtain a copy of
:: the License at
::
:: http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
:: WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
:: License for the specific language governing permissions and limitations under
:: the License.
::

:: run-tests.bat
:: Script to run tests for the wrangler project from any directory

setlocal enabledelayedexpansion

set TEST_NAME=%1
set MODULE=%2

if "%TEST_NAME%"=="" (
    echo Running all tests...
    mvn test
    exit /b 0
)

:: Add common package prefixes if not provided
echo %TEST_NAME% | findstr /C:"." > nul
if errorlevel 1 (
    echo %TEST_NAME% | findstr /C:"ByteSize" /C:"TimeDuration" > nul
    if errorlevel 0 (
        set TEST_NAME=io.cdap.wrangler.parser.%TEST_NAME%
    )
)

:: Run tests in specific module or all modules
if not "%MODULE%"=="" (
    echo Running test %TEST_NAME% in module %MODULE%...
    cd %MODULE%
    mvn test "-Dtest=%TEST_NAME%"
    cd ..
) else (
    echo Running test %TEST_NAME% in all modules...
    mvn test "-Dtest=%TEST_NAME%"
)

endlocal
