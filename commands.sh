#!/bin/bash

ssh -p 2031 root@6dist.idlab.uantwerpen.be

git clone git@github.com:BerkayYildirimUA/DS_Project.git
git checkout <branch>
git pull
git reset --hard HEAD~1

git fetch && git rebase origin/master

cd NameServer or Client
chmod +x start.sh
./start.sh
