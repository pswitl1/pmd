#!/usr/bin/env bash
pmd-dist/target/pmd-bin-6.13.0-SNAPSHOT/bin/run.sh pmd -R $1 -d $2 -f text
