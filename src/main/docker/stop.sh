#!/usr/bin/env bash
docker stop site-monitor && docker rm site-monitor && docker rmi -f developer23/site-monitor:0.0.1