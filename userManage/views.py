# coding: utf-8
from django.contrib.auth import authenticate,login,logout
from django.http import HttpResponse
from django.http import HttpResponseRedirect
from django.contrib.auth.models import User
from rest_framework import authentication, permissions,  status
import json
from rest_framework.response import Response
from rest_framework.generics import UpdateAPIView,ListCreateAPIView,GenericAPIView
from userManage.serializer import UserSerializer, GcmIdSerializer
from userManage.models import UserProfile

class UserProfileView(ListCreateAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = UserSerializer
    def get(self,request,format=None):
        profile = request.user
        serializer = self.get_serializer(profile,many=False)
        return Response(serializer.data)
    def post(self, request, *args, **kwargs):
        return


class AddDeviceForPushesView(GenericAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication)
    permission_classes = (permissions.IsAuthenticated,)
    allowed_methods = ('POST', 'OPTIONS')
    serializer_class = GcmIdSerializer
    def post(self, request):
        serializer = self.get_serializer(data=self.request.data)
        if serializer.is_valid():
            serializer.save(user=request.user)
            return Response({'response':'success'}, status=status.HTTP_200_OK)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
