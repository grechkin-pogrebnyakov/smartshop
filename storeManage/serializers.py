# coding: utf-8
__author__ = 'mid'

from rest_framework import serializers
from storeManage.models import Shop,Item, Check,CheckPosition, Price
from userManage.models import UserProfile
from userManage.utils import send_push_to_workers
import time,datetime
from django.core.exceptions import ObjectDoesNotExist
import logging
from my_utils import get_client_ip
from userManage.utils import send_push_to_other_workers

log = logging.getLogger('smartshop.log')


#TODO: remove this
class ShopSerializer(serializers.ModelSerializer):
    owner = serializers.PrimaryKeyRelatedField(many=False, read_only=False,queryset=UserProfile.objects.all())
    class Meta:
        model = Shop
        fields = ('name', 'owner',)
        read_only_fields = ('accountType',)


def change_price(price, pricePurchaseProduct, priceSellingProduct):
    new_price = Price()
    new_price.itemInfo = price.itemInfo
    new_price.pricePurchaseProduct=pricePurchaseProduct
    new_price.priceSellingProduct=priceSellingProduct
    new_price.save()
    return new_price


def above_zero(num):
    if num < 0:
        raise serializers.ValidationError("value {0} is below zero".format(num))


class ItemConfirmPriceUpdateSerializer(serializers.Serializer):
    item_id=serializers.IntegerField()

    def validate(self, data):
        request = self.context['request']
        user = request.user
        try:
            item = Item.objects.get(id=data['item_id'])
            if item.shop != user.profile.shop and item.shop != user.profile.oShop:
                log.warn("attempt to confirm update another shop item: '{0}' user '{1}' ip {2}".format(
                    item.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"item_id": "item is not yours"})
            new_price = item.new_price
            if new_price is None:
                log.warn("attempt to confirm update item without new price: '{0}' user '{1}' ip {2}".format(
                    item.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"item_id": "item has no new price"})
            data['item'] = item
            data['new_price'] = new_price
        except ObjectDoesNotExist:
            log.warn("attempt to confirm update of not existed item: '{0}' user '{1}' ip {2}".format(
                    data['item_id'], user.username, get_client_ip(request)))
            raise serializers.ValidationError({'item_id': 'no item existing'})
        return data

    def create(self, validated_data):
        request = self.context['request']
        item = validated_data['item']
        new_price = validated_data['new_price']
        old_price = item.price
        old_price.is_deleted = True
        old_price.save()
        user = validated_data['user']
        new_price.changer = user
        new_price.startDate = datetime.datetime.now()
        new_price.save()
        item.price = new_price
        item.new_price = None
        item.save()
        push_res = send_push_to_other_workers(user, "Внимание! В магазине были поменяны ценники. "
                                                    "Цена в приложении изменится автоматически.")
        if push_res is not None:
            log.debug(push_res)
        log.info("confirm price change: item_id '{0}' price_id '{1}' user '{2}' ip {3}".format(
            item.id, new_price.id, user.username, get_client_ip(request)))
        return new_price


class ShopItemUpdateSerializer(serializers.Serializer):
    productName = serializers.CharField(max_length=255)
    descriptionProduct = serializers.CharField(max_length=255)
    priceSellingProduct = serializers.FloatField(write_only=True, validators=[above_zero])
    pricePurchaseProduct = serializers.FloatField(write_only=True, validators=[above_zero])
    productBarcode = serializers.CharField(max_length=255)
    id = serializers.IntegerField()
    price_id = serializers.IntegerField(read_only=True)

    def validate(self, data):
        request = self.context['request']
        user = request.user
        try:
            item = Item.objects.get(id=data['id'])
            if item.shop != user.profile.shop and item.shop != user.profile.oShop:
                log.warn("attempt to update another shop item: '{0}' user '{1}' ip {2}".format(
                    item.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"id": "item is not yours"})
            data['item'] = item
        except ObjectDoesNotExist:
            raise serializers.ValidationError({'error': 'no item existing'})
        return data

    def create(self,validated_data):
        item = validated_data.get("item")
        item.productName=validated_data.get("productName")
        item.descriptionProduct=validated_data.get("descriptionProduct")
        item.productBarcode=validated_data.get("productBarcode")
        pricePurchaseProduct = validated_data.get("pricePurchaseProduct")
        priceSellingProduct = validated_data.get("priceSellingProduct")
        price = item.price
        if price.pricePurchaseProduct != pricePurchaseProduct or price.priceSellingProduct != priceSellingProduct:
            new_price = change_price(price, pricePurchaseProduct, priceSellingProduct)
            # хак: если хозяин сначала изменил цену продажи, а потом -- закупки,
            # цена закупки у неподтверждённого ценника тоже меняется, при этом пуши не шлются.
            if priceSellingProduct == price.priceSellingProduct:
                user = self.context['request'].user
                new_price.changer = user
                new_price.startDate = datetime.datetime.now()
                new_price.save()
                item_new_price = item.new_price
                if item_new_price is not None:
                    item_new_price.pricePurchaseProduct = pricePurchaseProduct
                    item_new_price.save()
                item.price.is_deleted = True
                item.price.save()
                item.price = new_price
            else:
                item.new_price = new_price
                push_res = send_push_to_workers(item.shop, 'Внимание! Цены некоторых товаров обновились!')
                if push_res is not None:
                    log.debug(push_res)
        item.save()
        return item


class PriceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Price
        fields = ('priceSellingProduct', 'pricePurchaseProduct')


class ShopItemSerializer(serializers.Serializer):
    productName = serializers.CharField(max_length=255)
    descriptionProduct = serializers.CharField(max_length=255)
    price = PriceSerializer(read_only=True)
    priceSellingProduct = serializers.FloatField(write_only=True, validators=[above_zero])
    pricePurchaseProduct = serializers.FloatField(write_only=True, validators=[above_zero])
    productBarcode = serializers.CharField(max_length=255)
    count = serializers.IntegerField(validators=[above_zero])
    id = serializers.IntegerField(read_only=True)
    price_id = serializers.IntegerField(read_only=True)
    new_price = PriceSerializer(read_only=True)

    def validate(self, data):
        oShop = self.context['request'].user.profile.oShop
        if oShop==None:
            raise serializers.ValidationError({"user": "not shop owner"})
        data['shop']=oShop
        return data

    def create(self, validated_data):
        owner = validated_data.get('owner')
        item = Item()
        item.count=validated_data.get("count")
        item.productName=validated_data.get("productName")
        price = Price()
        price.pricePurchaseProduct = validated_data.get("pricePurchaseProduct")
        price.priceSellingProduct = validated_data.get("priceSellingProduct")
        price.changer = owner
        price.startDate = datetime.datetime.now()
        price.save()
        self.price_id = price.id
        item.descriptionProduct=validated_data.get("descriptionProduct")
        item.productBarcode=validated_data.get("productBarcode")
        item.shop=validated_data.get('shop')
        item.price = price
        item.save()
        price.itemInfo = item
        price.save()
        return item


class CheckPositionSerizlizer(serializers.Serializer):
    price_id = serializers.IntegerField()
    item_id = serializers.IntegerField(read_only=True)
    count = serializers.IntegerField(validators=[above_zero])
    price = PriceSerializer(read_only=True)


class CheckSerializer(serializers.Serializer):
    check_positions = CheckPositionSerizlizer(many=True)
    id = serializers.IntegerField(read_only=True)
    type = serializers.IntegerField()
    author = serializers.CharField(max_length=255,read_only=True)
    shop_id = serializers.IntegerField(read_only=True)
    creation_time=serializers.DateTimeField(read_only=True)

    def validate(self, data):
        if int(data['type']) not in [0, 1, 2]:
            raise serializers.ValidationError({'type': 'not allowed'})
        shop = self.context['request'].user.profile.shop
        if shop is None :
            shop = self.context['request'].user.profile.oShop
        if not shop:
            raise serializers.ValidationError("no shop found")
        try:
            for pos in data.get('check_positions'):
                price = Price.objects.get(id=pos.get('price_id'))
                if price.is_deleted:
                    raise serializers.ValidationError({'check_positions': 'price deleted'})
                item = price.item
                if item.shop != shop:
                    raise serializers.ValidationError({'check_positions': 'not your item'})
                pos['price'] = price
                pos['item'] = item
        except ObjectDoesNotExist:
            raise serializers.ValidationError({'check_positions': 'item not exist'})
        data['shop'] = shop
        return data

    def create(self, validated_data):
        user = validated_data.get('user')
        type = validated_data.get('type')
        shop = validated_data.get('shop')
        check = Check(author=user, type=type, shop=shop)
        check.time = datetime.datetime.now()
        check.save()
        for pos in validated_data.get('check_positions'):
            price = pos['price']
            item = pos['item']
            db_count = pos.get('count')
            position = CheckPosition(item=item, count=db_count, relatedCheck=check, price=price)
            position.save()
            if type == 1:
                db_count = -db_count
            item.count = item.count-db_count
            item.save()
        return check
