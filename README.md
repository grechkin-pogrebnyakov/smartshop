# smartshop

## Install servers
* Nginx

  ```
  sudo apt-get update
  sudo apt-get install nginx
  ```
* Uwsgi

  ```
  sudo apt-get install python-pip python-dev build-essential
  sudo pip install --upgrade pip 
  sudo pip install uwsgi
  ```
  
## Copy configs
* Nginx

  ```
  sudo cp conf/smartshop /etc/nginx/sites-available/
  sudo ln -s /etc/nginx/sites-available/smartshop /etc/nginx/sites-enabled/smartshop
  sudo nginx -t
  sudo nginx -s reload
  ```
* Uwsgi

  ```
  sudo mkdir /etc/uwsgi
  sudo cp conf/smartshop_uwsgi.yaml /etc/uwsgi
  ```
  
## Start server
* Nginx starts automatically by default
* Uwsgi

  ```
  uwsgi -y /etc/uwsgi/smartshop_uwsgi.yaml
  ```
### NB
**Change path in `/etc/uwsgi/smartshop_uwsgi.yaml`**
