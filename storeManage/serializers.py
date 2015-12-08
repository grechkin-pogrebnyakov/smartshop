# coding: utf-8
__author__ = 'mid'

from rest_framework import serializers
from storeManage.models import Shop,Item, Check,CheckPosition, Price
from userManage.models import UserProfile
from userManage.utils import send_push_to_workers
import time,datetime
from django.forms import model_to_dict
import logging

log = logging.getLogger('smartshop.log')



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


class ItemConfirmPriceUpdateSerializer(serializers.Serializer):
    item_id=serializers.IntegerField()
    def validate(self, data):
        try:
            items = Item.objects.filter(id=self.context['request'].data.get("id"))
            if len(items) == 0 or len(items) > 1:
                raise Exception
            item = items[0]
            data['item']=item
        except:
            raise serializers.ValidationError('no item existing')
        return data



class ShopItemUpdateSerializer(serializers.Serializer):
    productName = serializers.CharField(max_length=255)
    descriptionProduct = serializers.CharField(max_length=255)
    priceSellingProduct = serializers.FloatField(write_only=True)
    pricePurchaseProduct = serializers.FloatField(write_only=True)
    productBarcode = serializers.CharField(max_length=255)
    id = serializers.IntegerField()
    price_id = serializers.IntegerField(read_only=True)

    def create(self,validated_data):
        item = validated_data.get("item")
        item.productName=validated_data.get("productName")
        item.descriptionProduct=validated_data.get("descriptionProduct")
        item.productBarcode=validated_data.get("productBarcode")
        pricePurchaseProduct = validated_data.get("pricePurchaseProduct")
        priceSellingProduct = validated_data.get("priceSellingProduct")
        price = item.price
        if price.pricePurchaseProduct != pricePurchaseProduct or price.priceSellingProduct != priceSellingProduct:
            item.new_price = change_price(price, pricePurchaseProduct, priceSellingProduct)
            push_res = send_push_to_workers(item.shop, 'Внимание! Цены некоторых товаров обновились!')
            if push_res is not None:
                log.debug(push_res)
        item.save()
        return item
    def validate(self, data):
        try:
            items = Item.objects.filter(id=self.context['request'].data.get("id"))
            if len(items) == 0 or len(items) > 1:
                raise Exception
            item = items[0]
            data['item']=item
        except:
            raise serializers.ValidationError('no item existing')
        return data

class PriceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Price
        fields = ('priceSellingProduct', 'pricePurchaseProduct')


class ShopItemSerializer(serializers.Serializer):
    productName = serializers.CharField(max_length=255)
    descriptionProduct = serializers.CharField(max_length=255)
    productBarcode = serializers.CharField(max_length=255)
    count = serializers.IntegerField()

    priceSellingProduct = serializers.FloatField(write_only=True)
    pricePurchaseProduct = serializers.FloatField(write_only=True)

    price = PriceSerializer(read_only=True)
    id = serializers.IntegerField(read_only=True)
    price_id = serializers.IntegerField(read_only=True)
    new_price = PriceSerializer(read_only=True)

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
    def validate(self, data):
        oShop = self.context['request'].user.profile.oShop
        if oShop==None:
            oShop = self.context['request'].user.profile.shop
        if not oShop:
            raise serializers.ValidationError("no shop")
        data['shop']=oShop

        return data

class CheckPositionSerizlizer(serializers.Serializer):
    price_id = serializers.IntegerField()
    item_id = serializers.IntegerField(read_only=True)
    count = serializers.IntegerField()
    price = PriceSerializer(read_only=True)


class CheckSerializer(serializers.Serializer):
    check_positions = CheckPositionSerizlizer(many=True)
    id = serializers.IntegerField(read_only=True)
    type = serializers.IntegerField()
    author = serializers.CharField(max_length=255,read_only=True)
    shop_id = serializers.IntegerField(read_only=True)
    creation_time=serializers.DateTimeField(read_only=True)

    def create(self, validated_data):
        user = validated_data.get('user')
        type = validated_data.get('type')
        shop = validated_data.get('shop')

        check = Check.objects.create(author=user, type=type, shop=shop)
        check.time = datetime.datetime.now()
        check.save()
        for position in validated_data.get('check_positions'):
            price = Price.objects.get(id=position.get('price_id'))
            item = price.item
            db_count = position.get('count')
            position = CheckPosition.objects.create(item=item,count=db_count,relatedCheck=check,
                                                    price=price
                                                    )
            position.save()
            if( type == 1 ):
                db_count = -db_count
            item.count = item.count-db_count
            item.save()
        return check

    def validate(self, data):#не валилируем позиции чека ибо нафиг
        shop = self.context['request'].user.profile.shop
        if shop is None :
            cshop = self.context['request'].user.profile.oShop
        else:
            cshop = shop
        data['shop'] = cshop
        if not cshop:
            raise serializers.ValidationError("no shop found")
        return data

