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

class CheckSerializer(serializers.Serializer):
    check_positions = CheckPositionSerizlizer(many=True)
    id = serializers.IntegerField(required=False)
    def create(self, validated_data):
        user = validated_data.get('user')
        check = Check.objects.create(author=user)
        check.time = datetime.datetime.now()
        check.save()
        for item in validated_data.get('check_positions'):
            position = CheckPosition.objects.create(item_id=item.get('item_id'),count=item.get('count'),relatedCheck=check)
            position.save();
        return check
    def get(self,time1,time2):
        return None



