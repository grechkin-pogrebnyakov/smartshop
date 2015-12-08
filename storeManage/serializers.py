# coding: utf-8
__author__ = 'mid'

from rest_framework import serializers
from storeManage.models import Shop,Item, Check,CheckPosition, Price
from userManage.models import UserProfile
from userManage.utils import send_push_to_workers
import time,datetime
from django.forms import model_to_dict
import logging
import base64
import imghdr
import uuid
from django.core.files.base import ContentFile
import hashlib

ALLOWED_IMAGE_TYPES = (
    "jpeg",
    "jpg",
    "png",
    "gif"
)
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


class ShopItemUpdateSerializer(serializers.Serializer):
    productName = serializers.CharField(max_length=255)
    descriptionProduct = serializers.CharField(max_length=255)
    priceSellingProduct = serializers.FloatField(write_only=True)
    pricePurchaseProduct = serializers.FloatField(write_only=True)
    productBarcode = serializers.CharField(max_length=255)
    id = serializers.IntegerField()
    price_id = serializers.IntegerField(read_only=True)

    def create(self,validated_data):
        items = Item.objects.filter(id=validated_data.get('id'))
        if len(items) == 0:
            raise serializers.ValidationError
        item = items[0]
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

class PriceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Price
        fields = ('priceSellingProduct', 'pricePurchaseProduct')


class ShopItemSerializer(serializers.Serializer):
    productName = serializers.CharField(max_length=255)
    descriptionProduct = serializers.CharField(max_length=255)
    price = PriceSerializer(read_only=True)
    priceSellingProduct = serializers.FloatField(write_only=True)
    pricePurchaseProduct = serializers.FloatField(write_only=True)
    productBarcode = serializers.CharField(max_length=255)
    count = serializers.IntegerField()
    id = serializers.IntegerField(read_only=True)
    price_id=serializers.IntegerField(read_only=True)
    new_price=PriceSerializer(read_only=True)
    image = serializers.CharField(required=False, write_only=True)
    image_url = serializers.SerializerMethodField('get_image_url2', required=False, read_only=True)
    image_hash = serializers.CharField(max_length=32, read_only=True, allow_blank=True)

    def validate(self, attrs):
        base64_data = attrs.get('image')
        if base64_data is not None:
            try:
                decoded_file = base64.b64decode(base64_data)
            except TypeError:
                raise serializers.ValidationError("Please upload a valid image.")
            # Generate file name:
            file_name = str(uuid.uuid4())[:12]  # 12 characters are more than enough.
            # Get the file name extension:
            file_extension = self.get_file_extension(file_name, decoded_file)
            if file_extension not in ALLOWED_IMAGE_TYPES:
                raise serializers.ValidationError("The type of the image couldn't been determined.")
            complete_file_name = file_name + "." + file_extension
            data = ContentFile(decoded_file, name=complete_file_name)
            attrs['image'] = data
        return attrs

    def get_file_extension(self, filename, decoded_file):
        extension = imghdr.what(filename, decoded_file)
        extension = "jpg" if extension == "jpeg" else extension
        return extension

    def get_image_url2(self, obj):
        if obj.image:
            return obj.image.url
        else:
            return ''

    def create(self, validated_data):
        owner = validated_data.get('owner')
        oShop = owner.profile.oShop
        if oShop==None:
            oShop = owner.profile.shop
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
        item.shop=oShop
        item.price = price
        image = validated_data.get('image')
        if image is not None:
            item.image.save(image.name, image, save=False)
            md5 = hashlib.md5()
            for chunk in image.chunks():
                md5.update(chunk)
            item.image_hash = md5.hexdigest()
        item.save()
        price.itemInfo = item
        price.save()
        return item


class CheckPositionSerizlizer(serializers.Serializer):
    price_id = serializers.IntegerField()
    item_id = serializers.IntegerField(read_only=True)
    count = serializers.IntegerField()
    #serializers.IntegerField(write_only=True)
    price = PriceSerializer(read_only=True)


class CheckSerializer(serializers.Serializer):
    check_positions = CheckPositionSerizlizer(many=True)
    id = serializers.IntegerField(read_only=True)
    type = serializers.IntegerField()
    author = serializers.CharField(max_length=255,read_only=True)
    shop_id = serializers.IntegerField(read_only=True)
    creation_time=serializers.DateTimeField(read_only=True)

    def create(self, validated_data):
        #TODO validate item_id
        user = validated_data.get('user')
        type = validated_data.get('type')
        shop = user.profile.shop
        if shop is None :
            cshop = user.profile.oShop
        else:
            cshop = shop
        check = Check.objects.create(author=user, type=type, shop=cshop)
        check.time = datetime.datetime.now()
        check.save()
        for pos in validated_data.get('check_positions'):
            price = Price.objects.get(id=pos.get('price_id'))
            item = price.item
            db_count = pos.get('count')
            position = CheckPosition.objects.create(item=item,count=db_count,relatedCheck=check,
                                                    price=price
                                                    )
            position.save()
            if( type == 1 ):
                db_count = -db_count
            item.count = item.count-db_count
            item.save()
        return check


