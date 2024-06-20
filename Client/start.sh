#!/bin/bash

# installation
apt update
apt install openjdk-21-jdk
mkdir assets
mkdir assets/local
mkdir assets/replicated


# variable input
new_port=$1
## Check if given port value is an usable port
## Allow only numbers: $new_port =~ ^[0-9]+$
## Exclude privileged ports: $new_port -ge 1024
if [[ $new_port =~ ^[0-9]+$ && $new_port -ge 1024 ]]; then
   # echo "${new_port} is a number"
   properties=src/main/resources/application.properties
   # Change the value
   sed -i "s/\(server\.port*=*\).*/\1$new_port/" "$properties"
else
   # echo "${new_port} is not a number"
   echo "start current config"
fi

# start
mvn spring-boot:run