# coding: utf-8
from django.contrib.auth import authenticate,login,logout
from django.http import HttpResponse
from django.http import HttpResponseRedirect
from django.contrib.auth.models import User
import json
# Create your views here.

#пока говнокод, потом пофиксю
def login(request):
    if request.method == 'POST':
        try:
            data=request.body
            data = json.loads(data)
            user = authenticate(username=data['email'],password=data['password'])
            if user is None:
                return HttpResponse('{"status":"400","body":"login fail"}')
            else:
                login(request,user)
                answer = json.dumps(user)
                return('{"status":"200","body":"login success"}')
        except Exception:
            return HttpResponse('{"status":"500","body":"internal server error"}')
    else:
        return HttpResponse('{"status":"400","body":"wrong parameters"}')

def register(request):
    if request.method == 'POST':
        try:
            data=request.body
            data=json.loads(data)
        except Exception:
            return HttpResponse('{"status":"500","body":"internal server error"}')
        try:
            user = User.objects.create_user(username=data['email'],password=data['password'])#email is login, actual email in DB is blank
            return HttpResponse('{"status":"200","body":"registration success"}')
        except Exception:
            return HttpResponse('{"status":"400","body":"registration failed"}')
    else:
        return HttpResponse('{"status":"400","body":"login fail"}')