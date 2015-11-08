# coding: utf-8
from django.contrib.auth import authenticate,login,logout
from django.http import HttpResponse
from django.http import HttpResponseRedirect
from django.contrib.auth.models import User
from rest_framework import authentication, permissions
import json
from rest_framework.response import Response
from rest_framework.generics import UpdateAPIView,ListCreateAPIView
from userManage.seryzalizer import UserSerializer
from userManage.models import UserProfile
class UserProfileView(ListCreateAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = UserSerializer
    def get(self,request,format=None):
        users = User.objects.all()
        profiles = UserProfile.objects.all()
        serializer = self.get_serializer(profiles,many=True)
        return Response(serializer.data)
    def post(self, request, *args, **kwargs):
        return