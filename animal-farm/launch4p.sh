#!/bin/bash
# Launches 4 game instances for 4P testing.
# Run from the animal-farm/ directory: ./launch4p.sh

DIR="$(cd "$(dirname "$0")" && pwd)"
CMD="cd '$DIR' && mvn javafx:run"

for i in 1 2 3 4; do
    osascript -e "tell application \"Terminal\" to do script \"$CMD\""
    sleep 0.3
done
