#!/usr/bin/env bash

sbt compile coverage test it/test coverageReport dependencyUpdates
