#!/bin/bash

apt update
apt install net-tools lsof

netstat -tulpn | grep LISTEN
kill -9 `lsof -ti :$PORT`