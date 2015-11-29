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
from storeManage.models import Shop,Check
from django.views.decorators.csrf import csrf_exempt
from rest_framework import authentication, permissions
from django.conf import settings
from storeManage.serializers import ShopSerializer,ShopItemSerializer,CheckSerializer
import logging
from my_utils import get_client_ip
import datetime

log = logging.getLogger('smartshop.log')

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


class Item(ListCreateAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = ShopItemSerializer
    def get(self, request, *args, **kwargs):
        curShop = request.user.profile.oShop
        if curShop==None:
            curShop=request.user.profile.shop
        items = models.Item.objects.filter(shop=curShop)
        serializer = self.get_serializer(items, many=True)
        return Response({'response':serializer.data})

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            return Response(status=status.HTTP_400_BAD_REQUEST)
        serializer.save(owner=self.request.user)
        return Response({'id': serializer.data.get("id")},status=status.HTTP_201_CREATED)
    def get_queryset(self):
        pass


class CheckView(ListCreateAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = CheckSerializer
    def get(self, request, *args, **kwargs):
        try:
            time1 = datetime.datetime.fromtimestamp(int(request.GET.get('time1')))
            time2 = datetime.datetime.fromtimestamp(int(request.GET.get('time2')))
        except Exception:
            return Response('no time specified')
        if request.user.profile.oShop!=None:
            shop_id = request.user.profile.oShop.id
        else:
            shop_id = request.user.profile.shop.id
        checks = Check.objects.filter(creation_time__range=[time1,time2])#magic range
        serializer = self.get_serializer(checks, many=True)
        serializer.data
        return Response(serializer.data)

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            log.warn('form is not valid. client_ip {0}'.format(get_client_ip(self.request)))
            return Response('')
        serializer.save(user=self.request.user)
        return Response({'id': serializer.data.get('id')},status=status.HTTP_201_CREATED)
    def get_queryset(self):
        return None
