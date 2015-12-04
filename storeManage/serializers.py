__author__ = 'mid'

from rest_framework import serializers
from storeManage.models import Shop,Item, Check,CheckPosition
from userManage.models import UserProfile
import json
import time,datetime

class ShopSerializer(serializers.ModelSerializer):
    owner = serializers.PrimaryKeyRelatedField(many=False, read_only=False,queryset=UserProfile.objects.all())
    class Meta:
        model = Shop
        fields = ('name', 'owner',)
        read_only_fields = ('accountType',)


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


