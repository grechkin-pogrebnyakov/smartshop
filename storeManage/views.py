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
from rest_framework.generics import RetrieveUpdateAPIView, ListCreateAPIView
from storeManage.models import Shop, Check, Supply
from rest_framework import authentication, permissions
from django.conf import settings
from storeManage.serializers import ShopSerializer,ShopItemSerializer,CheckSerializer,ShopItemUpdateSerializer,\
    ItemConfirmPriceUpdateSerializer, SupplySerializer, SupplyConfirmSerializer, ItemDeleteSerializer
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


#TODO: remove this
class Store(ListCreateAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = ShopSerializer

    def get(self, request, *args, **kwargs):
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
        shop = request.user.profile.oShop
        if shop is None:
            shop = request.user.profile.shop
        deleted = request.GET.get('deleted')
        if deleted is None:
            deleted = '0'
        if deleted == '1':
            items = models.Item.objects.filter(shop=shop)
        else:
            items = models.Item.objects.filter(shop=shop, is_deleted=False)
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
            serializer.data.get('id'), serializer.price_id, self.request.user.username, get_client_ip(request)))
        return Response({'id': serializer.data.get("id"),'price_id':serializer.price_id,
                         'image_hash': serializer.data.get('image_hash')},status=status.HTTP_201_CREATED)

    def get_serializer_context(self):
        return {'request': self.request}


class ItemUpdate(GenericAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = ShopItemUpdateSerializer

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            log.warn("error updating item: '{0}' user '{1}' ip {2}".format(
                serializer.errors,self.request.user.username, get_client_ip(request)))
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save()
        log.info("updating item: id '{0}' user '{1}' ip {2}".format(
            serializer.validated_data.get('id'), self.request.user.username, get_client_ip(request)))
        return Response({'response': 'success', 'image_hash': serializer.data.get('image_hash')}, status=status.HTTP_200_OK)

    def get_serializer_context(self):
        return {'request': self.request}


class ItemListChangePrice(GenericAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = ShopItemSerializer

    def get(self, request):
        shop = request.user.profile.oShop  # TODO: это говнокод его надо перенести в Item
        if shop is None:
            shop = request.user.profile.shop
        items = models.Item.objects.filter(shop=shop, new_price__isnull=False, is_deleted=False)
        serializer = self.get_serializer(items, many=True)
        return Response({'response': serializer.data})


class ItemConfirmPriceUpdate(GenericAPIView):
    serializer_class = ItemConfirmPriceUpdateSerializer
    authentication_classes = (authentication.TokenAuthentication, authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save(user=request.user)
        return Response({'response': 'success'}, status=status.HTTP_200_OK)

    def get_serializer_context(self):
        return {'request': self.request}


class ItemDeleteView(GenericAPIView):
    serializer_class = ItemDeleteSerializer
    authentication_classes = (authentication.TokenAuthentication, authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save(user=request.user)
        return Response({'response': 'success'}, status=status.HTTP_200_OK)


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
        if request.user.profile.oShop is not None:
            cshop = request.user.profile.oShop
        else:
            cshop = request.user.profile.shop
        checks = Check.objects.filter(creation_time__range=[time1, time2], shop=cshop, type=type)
        serializer = self.get_serializer(checks, many=True)
        return Response({'response': serializer.data})

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            log.warn("error adding check: '{0}' user '{1}' ip {2}".format(
                serializer.errors,self.request.user.username, get_client_ip(request)))
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save(user=self.request.user)
        types = ['check', 'supply', 'discard']
        log.info("adding {0}: id '{1}' user '{2}' ip {3}".format(types[serializer.data.get('type')],
                                                                 serializer.data.get('id'), self.request.user.username,
                                                                 get_client_ip(request)))
        return Response({'id': serializer.data.get('id')}, status=status.HTTP_201_CREATED)

    def get_serializer_context(self):
        return {'request': self.request}


class SupplyView(ListCreateAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = SupplySerializer

    def get(self, request, *args, **kwargs):
        user = request.user
        if user.profile.accountType == 'owner':
            try:
                time1 = datetime.datetime.fromtimestamp(int(request.GET.get('time1')))
                time2 = datetime.datetime.fromtimestamp(int(request.GET.get('time2')))
            except ValueError:
                return Response('no time specified')
            except TypeError:
                return Response('no time specified')
            shop = user.profile.oShop
            supplies = Supply.objects.filter(expected_date__range=[time1, time2], shop=shop)
            serializer = self.get_serializer(supplies, many=True)
            return Response({'response': serializer.data})
        else:
            shop = request.user.profile.shop
            supplies = Supply.objects.filter(done=False, shop=shop)
            serializer = self.get_serializer(supplies, many=True)
            return Response({'response': serializer.data})

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            log.warn("error adding supply: '{0}' user '{1}' ip {2}".format(
                serializer.errors,self.request.user.username, get_client_ip(request)))
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save()
        log.info("adding supply: id '{0}' user '{1}' ip {2}".format(serializer.data.get('id'),
                                                                    self.request.user.username, get_client_ip(request)))
        return Response({'id': serializer.data.get('id')}, status=status.HTTP_201_CREATED)

    def get_serializer_context(self):
        return {'request': self.request}


class SupplyConfirmView(ListCreateAPIView):
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication,)
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = SupplyConfirmSerializer
    allowed_methods = ('POST', 'OPTIONS', 'HEAD')

    def get(self, request, *args, **kwargs):
        return Response("method not allowed", status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            log.warn("error confirm supply: '{0}' user '{1}' ip {2}".format(
                serializer.errors,self.request.user.username, get_client_ip(request)))
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save()
        log.info("confirm supply: id '{0}' user '{1}' ip {2}".format(serializer.data.get('id'),
                                                                     self.request.user.username,
                                                                     get_client_ip(request)))
        return Response({'response': 'success'}, status=status.HTTP_201_CREATED)

    def get_serializer_context(self):
        return {'request': self.request}
