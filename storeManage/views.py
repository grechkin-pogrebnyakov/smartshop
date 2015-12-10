# coding: utf-8
# Create your views here.
from storeManage import models
from django.http import HttpResponse
from django.http import HttpResponseRedirect
from rest_framework.renderers import JSONRenderer
from rest_framework import status
from django.db.models import Q
from rest_framework.parsers import JSONParser
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.generics import GenericAPIView
from rest_framework.generics import RetrieveUpdateAPIView,ListCreateAPIView
from storeManage.models import Shop,Check
from rest_framework import authentication, permissions
from django.conf import settings
from storeManage.serializers import ShopSerializer,ShopItemSerializer,CheckSerializer,ShopItemUpdateSerializer, ItemConfirmPriceUpdateSerializer
import logging
from my_utils import get_client_ip
from userManage.utils import send_push_to_other_workers
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


class Item(GenericAPIView):
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
            log.warn("error adding item: '{0}' user '{1}' ip {2}".format(
                serializer.errors,self.request.user.username, get_client_ip(request)))
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save(owner=self.request.user)
        log.info("add item: id '{0}' price_id '{1}' user '{2}' ip {3}".format(
            serializer.data.get('id'), serializer.price_id ,self.request.user.username, get_client_ip(request)))
        return Response({'id': serializer.data.get("id"),'price_id':serializer.price_id,
                         'image_hash': serializer.data.get('image_hash')},status=status.HTTP_201_CREATED)
    def get_queryset(self):
        pass


class Item_update(GenericAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = ShopItemUpdateSerializer
    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            log.warn("error updating item: '{0}' user '{1}' ip {2}".format(
                serializer.errors,self.request.user.username, get_client_ip(request)))
            return Response(status=status.HTTP_400_BAD_REQUEST)
        serializer.save()
        log.info("updating item: id '{0}' user '{1}' ip {2}".format(
            serializer.validated_data.get('id'),self.request.user.username, get_client_ip(request)))
        return Response({'response': 'success', 'image_hash': serializer.data.get('image_hash')}, status=status.HTTP_200_OK)
    def get_queryset(self):
        pass


class ItemListChangePrice(GenericAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = ShopItemSerializer
    def get(self,request):
        tmp_shop = request.user.profile.oShop
        if tmp_shop is None :
            shop = request.user.profile.shop
        else:
            shop = tmp_shop
        items = models.Item.objects.filter(shop=shop, new_price__isnull = False)
        serializer = self.get_serializer(items, many=True)
        return Response({'response':serializer.data})


class ItemConfirmPriceUpdate(GenericAPIView):
    serializer_class = ItemConfirmPriceUpdateSerializer
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request, *args, **kwargs):
        user = request.user
        serializer = self.get_serializer(data = request.data)

        if not serializer.is_valid() :
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        items = models.Item.objects.filter(id=serializer.data.get('item_id'))
        item = items[0]
        new_price = item.new_price
        old_price = item.price
        if new_price is None :
            log.warn("attempt to update item without new price: '{0}' user '{1}' ip {2}".format(
                item.id,self.request.user.username, get_client_ip(request)))
            return Response({"error":"item has no new price"}, status=status.HTTP_400_BAD_REQUEST)
        old_price.is_deleted = True
        old_price.save()
        new_price.changer = user
        new_price.startDate = datetime.datetime.now()
        new_price.save()
        item.price = new_price
        item.new_price = None
        item.save()
        push_res = send_push_to_other_workers(user, "Внимание! В магазине были поменяны ценники. Цена в приложении изменится автоматически.")
        if push_res is not None:
            log.debug(push_res)
        log.info("confirm price change: item_id '{0}' price_id '{1}' user '{2}' ip {3}".format(
            item.id, new_price.id,self.request.user.username, get_client_ip(request)))
        return Response({'response': 'success'},status=status.HTTP_200_OK)


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
        try:
            type = int(request.GET.get('type'))
        except Exception:
            type = 0
        if request.user.profile.oShop!=None:
            cshop = request.user.profile.oShop
        else:
            cshop = request.user.profile.shop
        checks = Check.objects.filter(creation_time__range=[time1,time2], shop=cshop, type=type)
        serializer = self.get_serializer(checks, many=True)
        return Response({'response':serializer.data})

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            log.warn("error adding check: '{0}' user '{1}' ip {2}".format(
                serializer.errors,self.request.user.username, get_client_ip(request)))
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save(user=self.request.user)
        types = ['check', 'supply', 'discard']
        log.info("adding {0}: id '{1}' user '{2}' ip {3}".format( types[serializer.data.get('type')],
            serializer.data.get('id'),self.request.user.username, get_client_ip(request)))
        return Response({'id': serializer.data.get('id')},status=status.HTTP_201_CREATED)
    def get_queryset(self):
        return None
