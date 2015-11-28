__author__ = 'mid'

from rest_framework import serializers
from storeManage.models import Shop,Item
from userManage.models import UserProfile

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










