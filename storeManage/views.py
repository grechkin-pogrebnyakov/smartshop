from django.shortcuts import render
from rest_framework import serializers
from storeManage import models
# Create your views here.
from django.http import HttpResponse
from django.http import HttpResponseRedirect
from rest_framework.renderers import JSONRenderer
from rest_framework import status
from rest_framework.parsers import JSONParser
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.generics import GenericAPIView
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.authtoken.models import Token
from rest_framework.generics import RetrieveUpdateAPIView,ListCreateAPIView
from storeManage.serializers import ShopSerializer
from storeManage.models import Shop
from django.views.decorators.csrf import csrf_exempt
from rest_framework import authentication, permissions
from django.conf import settings
from storeManage.serializers import ShopSerializer


class JSONResponse(HttpResponse):
    """
    An HttpResponse that renders its content into JSON.
    """
    def __init__(self, data, **kwargs):
        content = JSONRenderer().render(data)
        kwargs['content_type'] = 'application/json'
        super(JSONResponse, self).__init__(content, **kwargs)


class Store(ListCreateAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = ShopSerializer
    def get(self,request,format=None):
        shops = Shop.objects.all()
        serializer = self.get_serializer(shops,many=True)
        return Response(serializer.data)
    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            return Response(status=status.HTTP_400_BAD_REQUEST)
        serializer.save()
        return Response(serializer.data)


