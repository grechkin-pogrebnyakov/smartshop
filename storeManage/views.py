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
from rest_framework.generics import RetrieveUpdateAPIView
from storeManage.serializers import ShopSerializer

from django.views.decorators.csrf import csrf_exempt
from rest_framework import authentication, permissions


class JSONResponse(HttpResponse):
    """
    An HttpResponse that renders its content into JSON.
    """
    def __init__(self, data, **kwargs):
        content = JSONRenderer().render(data)
        kwargs['content_type'] = 'application/json'
        super(JSONResponse, self).__init__(content, **kwargs)

class Store(APIView):
    authentication_classes = (authentication.TokenAuthentication,)
    data_serializer = ShopSerializer
    def get(self,request,format=None):
        shop = self.data_serializer.data
        return Response(shop,status=status.HTTP_200_OK)
