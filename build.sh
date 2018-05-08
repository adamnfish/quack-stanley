#!/usr/bin/env bash

# utils
green=`tput setaf 2`
reset=`tput sgr0`

# set CWD to root of repo
cd $(dirname $(realpath $0))

# package zip file
sbt universal:packageBin

echo "${green}Created zip bundle at ${PWD}/target/universal/quack-stanley.zip${reset}"

echo "Deploy function with"
echo "  aws lambda update-function-code --function-name quack-stanley-prod --zip-file" fileb://${PWD}/target/universal/quack-stanley.zip --profile PROFILE
echo "replace prod with the correct stage and PROFILE with your AWS profile"
