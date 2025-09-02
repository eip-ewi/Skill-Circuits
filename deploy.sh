#!/bin/sh

./gradlew clean
cd frontend
npm run build
cd ..
./gradlew assemble
scp build/libs/Skill\ Circuits-2526.0.0.jar tudelft-bastion:.
