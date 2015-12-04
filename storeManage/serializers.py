__author__ = 'mid'

from rest_framework import serializers
from storeManage.models import Shop,Item, Check,CheckPosition
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


class ShopItem_updateSerializer(serializers.Serializer):
    productName = serializers.CharField(max_length=255)
    descriptionProduct = serializers.CharField(max_length=255)
    priceSellingProduct = serializers.FloatField()
    pricePurchaseProduct = serializers.FloatField()
    productBarcode = serializers.CharField(max_length=255)
    count = serializers.IntegerField()
    id = serializers.IntegerField()
    def create(self,validated_data):
        items = Item.objects.filter(id=validated_data.get('id'))
        if( len(items) is 0):
            raise serializers.ValidationError
        item = items[0]
        item.count=validated_data.get("count")
        item.productName=validated_data.get("productName")
        item.pricePurchaseProduct=validated_data.get("pricePurchaseProduct")
        item.priceSellingProduct=validated_data.get("priceSellingProduct")
        item.descriptionProduct=validated_data.get("descriptionProduct")
        item.productBarcode=validated_data.get("productBarcode")
        item.save()
        return item


class ShopItemUpdatePriceSerializer(serializers.Serializer):
    id = serializers.IntegerField()
    pricePurchaseProduct = serializers.FloatField(required=False)
    priceSellingProduct = serializers.FloatField(required=False)

    def create(self,validated_data):
        items = Item.objects.filter(id=validated_data.get('id'))
        if( len(items) is 0):
            raise serializers.ValidationError
        item = items[0]
        kwargs = model_to_dict(item, exclude=['id','new_item'])
        new_item = Item()
        new_item.count=item.count
        new_item.productName=item.productName
        new_item.pricePurchaseProduct=item.pricePurchaseProduct
        new_item.priceSellingProduct=item.priceSellingProduct
        new_item.descriptionProduct=item.descriptionProduct
        new_item.productBarcode=item.productBarcode
        new_item.shop=item.shop
        pricePurchaseProduct=validated_data.get("pricePurchaseProduct")
        if pricePurchaseProduct is not None:
            new_item.pricePurchaseProduct=pricePurchaseProduct
        priceSellingProduct=validated_data.get("priceSellingProduct")
        if priceSellingProduct is not None:
            new_item.priceSellingProduct=priceSellingProduct
        if priceSellingProduct is None and pricePurchaseProduct is None:
            raise serializers.ValidationError
        item.is_deleted = 1
        new_item.save()
        item.new_item = new_item
        item.save()
        log.warn(send_push_to_workers(new_item.shop, 'Price updated!'))
        return new_item


class ShopItemSerializer(serializers.Serializer):
    productName = serializers.CharField(max_length=255)
    descriptionProduct = serializers.CharField(max_length=255)
    priceSellingProduct = serializers.FloatField()
    pricePurchaseProduct = serializers.FloatField()
    productBarcode = serializers.CharField(max_length=255)
    count = serializers.IntegerField()
    id = serializers.ReadOnlyField()
    def create(self, validated_data):
        owner = validated_data.get('owner')
        oShop = owner.profile.oShop
        if oShop==None:
            oShop = owner.profile.shop
        item = Item()
        item.count=validated_data.get("count")
        item.productName=validated_data.get("productName")
        item.pricePurchaseProduct=validated_data.get("pricePurchaseProduct")
        item.priceSellingProduct=validated_data.get("priceSellingProduct")
        item.descriptionProduct=validated_data.get("descriptionProduct")
        item.productBarcode=validated_data.get("productBarcode")
        item.shop=oShop
        item.save()
        return item


class CheckPositionSerizlizer(serializers.Serializer):
    item_id = serializers.IntegerField()
    count = serializers.IntegerField()
    priceSellingProduct = serializers.FloatField(read_only=True)
    pricePurchaseProduct = serializers.FloatField(read_only=True)

class CheckSerializer(serializers.Serializer):
    check_positions = CheckPositionSerizlizer(many=True)
    id = serializers.IntegerField(required=False)
    type = serializers.IntegerField()
    author = serializers.CharField(max_length=255,read_only=True)
    shop_id = serializers.IntegerField(read_only=True)
    creation_time=serializers.DateTimeField(read_only=True)

    def create(self, validated_data):
        #TODO validate item_id
        user = validated_data.get('user')
        type = validated_data.get('type')
        check = Check.objects.create(author=user, type=type)
        check.time = datetime.datetime.now()
        check.save()
        for pos in validated_data.get('check_positions'):
            position = CheckPosition.objects.create(item_id=pos.get('item_id'),count=pos.get('count'),relatedCheck=check,
                                                    priceSellingProduct=Item.objects.filter(id=pos.get('item_id'))[0].priceSellingProduct,
                                                    pricePurchaseProduct=Item.objects.filter(id=pos.get('item_id'))[0].pricePurchaseProduct,
                                                    )
            position.save()
            item = Item.objects.filter(id=pos.get('item_id'))[0]
            db_count = pos.get('count')
            if( type == 1 ):
                db_count = -db_count
            item.count = item.count-db_count
            item.save()
            
        return check


