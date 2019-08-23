#!/bin/bash
docker build -f Dockerfile -t developer23/site-monitor:0.0.1 . &&
docker run -d --name site-monitor -p 8080:8080 -e "SPRING_PROFILES_ACTIVE=prod" -v $PWD/logs:/var/log/site-monitor/logs developer23/site-monitor:0.0.1 && docker logs -f site-monitor