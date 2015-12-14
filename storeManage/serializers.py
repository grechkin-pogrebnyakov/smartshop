# coding: utf-8
__author__ = 'mid'

from rest_framework import serializers
from storeManage.models import Shop,Item, Check,CheckPosition, Price, Supply
from userManage.models import UserProfile
from userManage.utils import send_push_to_workers
import time,datetime
from django.core.exceptions import ObjectDoesNotExist
import logging
from my_utils import get_client_ip
from userManage.utils import send_push_to_other_workers, send_push_to_owner
import base64
import imghdr
import uuid
from django.core.files.base import ContentFile
import hashlib
from django.db import transaction

ALLOWED_IMAGE_TYPES = (
    "jpeg",
    "jpg",
    "png",
    "gif"
)
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
    new_price.pricePurchaseProduct = pricePurchaseProduct
    new_price.priceSellingProduct = priceSellingProduct
    new_price.save()
    return new_price


def above_zero(num):
    if num < 0:
        raise serializers.ValidationError("value {0} is below zero".format(num))


def get_file_extension(filename, decoded_file):
    extension = imghdr.what(filename, decoded_file)
    extension = "jpg" if extension == "jpeg" else extension
    return extension


class ItemConfirmPriceUpdateSerializer(serializers.Serializer):
    item_id = serializers.IntegerField()

    def validate(self, data):
        request = self.context['request']
        user = request.user
        try:
            item = Item.objects.get(id=data['item_id'])
            if item.shop != user.profile.shop and item.shop != user.profile.oShop:
                log.warn("attempt to confirm update another shop item: '{0}' user '{1}' ip {2}".format(
                    item.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"item_id": "item is not yours"})
            if item.is_deleted is True:
                log.warn("attempt to confirm update of deleted item: '{0}' user '{1}' ip {2}".format(
                    item.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"item_id": "item is deleted"})
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
            raise serializers.ValidationError({'item_id': 'item not exist'})
        return data

    def create(self, validated_data):
        request = self.context['request']
        item = validated_data['item']
        new_price = validated_data['new_price']
        user = validated_data['user']
        with transaction.atomic():
            old_price = item.price
            old_price.is_deleted = True
            old_price.save()
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
    productName = serializers.CharField(max_length=255, required=False)
    descriptionProduct = serializers.CharField(max_length=255, required=False, allow_blank=True)
    priceSellingProduct = serializers.FloatField(write_only=True, validators=[above_zero], required=False)
    pricePurchaseProduct = serializers.FloatField(write_only=True, validators=[above_zero], required=False)
    productBarcode = serializers.CharField(max_length=255, required=False, allow_blank=True)
    id = serializers.IntegerField()
    price_id = serializers.IntegerField(read_only=True)
    image = serializers.CharField(required=False, write_only=True, allow_blank=True)
    image_hash = serializers.CharField(max_length=32, read_only=True, allow_blank=True)

    def validate_image(self, base64_data):
        if base64_data is not None and base64_data != '':
            try:
                decoded_file = base64.b64decode(base64_data)
            except TypeError:
                raise serializers.ValidationError("Please upload a valid image.")
            # Generate file name:
            file_name = str(uuid.uuid4())[:12]  # 12 characters are more than enough.
            # Get the file name extension:
            file_extension = get_file_extension(file_name, decoded_file)
            if file_extension not in ALLOWED_IMAGE_TYPES:
                raise serializers.ValidationError("The type of the image couldn't been determined.")
            complete_file_name = file_name + "." + file_extension
            data = ContentFile(decoded_file, name=complete_file_name)
        else:
            data = None
        return data

    def validate(self, data):
        request = self.context['request']
        user = request.user
        try:
            item = Item.objects.get(id=data['id'])
            if item.shop != user.profile.shop and item.shop != user.profile.oShop:
                log.warn("attempt to update another shop item: '{0}' user '{1}' ip {2}".format(
                    item.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"id": "item is not yours"})
            if item.is_deleted is True:
                log.warn("attempt to update deleted item: '{0}' user '{1}' ip {2}".format(
                    item.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"id": "item is deleted"})
            data['item'] = item
        except ObjectDoesNotExist:
            log.warn("attempt to update not existed item: '{0}' user '{1}' ip {2}".format(
                data['id'], user.username, get_client_ip(request)))
            raise serializers.ValidationError({'id': 'item not exist'})
        return data

    def create(self, validated_data):
        item = validated_data.get("item")
        with transaction.atomic():
            product_name = validated_data.get("productName")
            if product_name is not None and len(product_name) > 0:
                item.productName = product_name
            description = validated_data.get("descriptionProduct")
            if description is not None and len(description) > 0:
                item.descriptionProduct = description
            barcode = validated_data.get("productBarcode")
            if barcode is not None and len(barcode) > 0:
                item.productBarcode = barcode
            price_purchase_product = validated_data.get("pricePurchaseProduct")
            price_selling_product = validated_data.get("priceSellingProduct")
            price = item.price
            if price_purchase_product is None:
                price_purchase_product = price.pricePurchaseProduct
            if price_selling_product is None:
                price_selling_product = price.priceSellingProduct
            if price.pricePurchaseProduct != price_selling_product or price.priceSellingProduct != price_selling_product:
                new_price = change_price(price, price_purchase_product, price_selling_product)
                # хак: если хозяин сначала изменил цену продажи, а потом -- закупки,
                # цена закупки у неподтверждённого ценника тоже меняется, при этом пуши не шлются.
                if price_selling_product == price.priceSellingProduct:
                    user = self.context['request'].user
                    new_price.changer = user
                    new_price.startDate = datetime.datetime.now()
                    new_price.save()
                    item_new_price = item.new_price
                    if item_new_price is not None:
                        item_new_price.pricePurchaseProduct = price_purchase_product
                        item_new_price.save()
                    item.price.is_deleted = True
                    item.price.save()
                    item.price = new_price
                else:
                    item.new_price = new_price
                    push_res = send_push_to_workers(item.shop, 'Внимание! Цены некоторых товаров обновились!')
                    if push_res is not None:
                        log.debug(push_res)
            image = validated_data.get('image')
            if image is not None:
                item.image.delete()
                item.image.save(image.name, image, save=False)
                md5 = hashlib.md5()
                for chunk in image.chunks():
                    md5.update(chunk)
                item.image_hash = md5.hexdigest()
            item.save()
        return item


class PriceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Price
        fields = ('priceSellingProduct', 'pricePurchaseProduct')


class ShopItemSerializer(serializers.Serializer):
    productName = serializers.CharField(max_length=255)
    descriptionProduct = serializers.CharField(max_length=255, required=False, allow_blank=True)
    price = PriceSerializer(read_only=True)
    priceSellingProduct = serializers.FloatField(write_only=True, validators=[above_zero])
    pricePurchaseProduct = serializers.FloatField(write_only=True, validators=[above_zero])
    productBarcode = serializers.CharField(max_length=255, required=False, allow_blank=True)
    count = serializers.IntegerField(validators=[above_zero])
    id = serializers.IntegerField(read_only=True)
    price_id = serializers.IntegerField(read_only=True)
    new_price = PriceSerializer(read_only=True)
    image = serializers.CharField(required=False, write_only=True, allow_blank=True)
    image_url = serializers.SerializerMethodField('get_image_url_blya', read_only=True)
    image_hash = serializers.CharField(max_length=32, read_only=True, allow_blank=True)
    is_deleted = serializers.BooleanField(read_only=True)

    def validate(self, data):
        request = self.context['request']
        user = request.user
        shop = user.profile.oShop
        if shop is None:
            log.warn("worker attempts to create item: user '{0}' ip {1}".format(user.username, get_client_ip(request)))
            raise serializers.ValidationError({"user": "not shop owner"})
        data['shop'] = shop
        return data

# сука, имя get_image_url занято
    def get_image_url_blya(self, obj):
        if obj.image:
            return obj.image.url
        else:
            return ''

    def validate_image(self, base64_data):
        if base64_data is not None and base64_data != '':
            try:
                decoded_file = base64.b64decode(base64_data)
            except TypeError:
                raise serializers.ValidationError("Please upload a valid image.")
            # Generate file name:
            file_name = str(uuid.uuid4())[:12]  # 12 characters are more than enough.
            # Get the file name extension:
            file_extension = get_file_extension(file_name, decoded_file)
            if file_extension not in ALLOWED_IMAGE_TYPES:
                raise serializers.ValidationError("The type of the image couldn't been determined.")
            complete_file_name = file_name + "." + file_extension
            data = ContentFile(decoded_file, name=complete_file_name)
        else:
            data = None
        return data

    def create(self, validated_data):
        owner = validated_data.get('owner')
        with transaction.atomic():
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
            description = validated_data.get("descriptionProduct")
            if description is not None:
                item.descriptionProduct = description
            barcode = validated_data.get("productBarcode")
            if barcode is not None:
                item.productBarcode = barcode
            item.shop = validated_data.get('shop')
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


class ItemDeleteSerializer(serializers.Serializer):
    id = serializers.IntegerField()

    def validate(self, attrs):
        request = self.context['request']
        user = request.user
        shop = user.profile.oShop
        if shop is None:
            log.warn("worker attempts to delete item: id '{0}' user '{1}' ip {2}".format(attrs[id], user.username,
                                                                                         get_client_ip(request)))
            raise serializers.ValidationError({"user": "not shop owner"})
        try:
            item = Item.objects.get(id=attrs['id'])
            if item.shop != user.profile.oShop:
                log.warn("attempt to delete another shop item: '{0}' user '{1}' ip {2}".format(
                    item.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"id": "item is not yours"})
            if item.is_deleted is True:
                log.warn("attempt to delete deleted item: '{0}' user '{1}' ip {2}".format(
                    item.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"id": "item is deleted"})
            attrs['item'] = item
        except ObjectDoesNotExist:
            log.warn("attempt to delete not existed item: '{0}' user '{1}' ip {2}".format(
                attrs['id'], user.username, get_client_ip(request)))
            raise serializers.ValidationError({'id': 'item not exist'})
        return attrs

    def create(self, validated_data):
        item = validated_data.get("item")
        with transaction.atomic():
            item.is_deleted = True
            item.save()
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
    creation_time = serializers.DateTimeField(format='%Y-%m-%d %H:%M:%S', read_only=True)

    def validate(self, data):
        request = self.context['request']
        user = request.user
        if int(data['type']) not in [0, 1, 2]:
            log.warn("unsupported check type: '{0}' user '{1}' ip {2}".format(
                data['type'], user.username, get_client_ip(request)))
            raise serializers.ValidationError({'type': 'not allowed'})
        shop = user.profile.shop
        if shop is None:
            shop = user.profile.oShop
        if shop is None:
            log.error("user without shop and oShop: id {0} username '{1}' ip {2}".format(
                user.id, user.username, get_client_ip(request)))
            raise serializers.ValidationError({'user': "no shop found"})
        for pos in data.get('check_positions'):
            try:
                price = Price.objects.get(id=pos.get('price_id'))
            except ObjectDoesNotExist:
                log.warn("price not exist: '{0}' user '{1}' ip {2}".format(
                    pos['price_id'], user.username, get_client_ip(request)))
                raise serializers.ValidationError({'check_positions': 'item not exist'})
            if price.is_deleted:
                log.warn("attempt to create check with deleted price: '{0}' user '{1}' ip {2}".format(
                    pos['price_id'], user.username, get_client_ip(request)))
                raise serializers.ValidationError({'check_positions': 'price deleted'})
            item = price.itemInfo
            if item.shop != shop:
                log.warn("attempt to create check with price from other shop: '{0}' user '{1}' ip {2}".format(
                    pos['price_id'], user.username, get_client_ip(request)))
                raise serializers.ValidationError({'check_positions': 'not your item'})
            pos['price'] = price
            pos['item'] = item
        data['shop'] = shop
        return data

    def create(self, validated_data):
        user = validated_data.get('user')
        operation_type = validated_data.get('type')
        shop = validated_data.get('shop')
        with transaction.atomic():
            check = Check(author=user, type=operation_type, shop=shop)
            check.time = datetime.datetime.now()
            check.save()
            for pos in validated_data.get('check_positions'):
                price = pos['price']
                item = pos['item']
                db_count = pos.get('count')
                position = CheckPosition(item=item, count=db_count, relatedCheck=check, price=price)
                position.save()
                if operation_type == 1:
                    log.error("type 1 is deprecated")
                    db_count *= -1
                item.count = item.count - db_count
                item.save()
        return check


class DateSerializer(serializers.CharField):
    def to_representation(self, obj):
        return obj.date()


class SupplySerializer(serializers.Serializer):
    price_id = serializers.IntegerField()
    item_id = serializers.SerializerMethodField('get_item_id_blya', read_only=True)
    expected_count = serializers.IntegerField(validators=[above_zero])
    real_count = serializers.IntegerField(validators=[above_zero], read_only=True)
    expected_date = DateSerializer(max_length=20, allow_blank=False)
    real_date = serializers.SerializerMethodField('get_real_date_blya')
    price = PriceSerializer(read_only=True)
    id = serializers.IntegerField(read_only=True)
    worker = serializers.CharField(max_length=255, read_only=True)
    done = serializers.BooleanField(read_only=True)

    def get_real_date_blya(self, obj):
        if obj.real_date:
            return obj.real_date.date()

    def validate_expected_date(self, date_str):
        try:
            expected_date = datetime.datetime.fromtimestamp(int(date_str))
            return expected_date
        except ValueError:
            raise serializers.ValidationError('invalid value')
        except TypeError:
            raise serializers.ValidationError('invalid value')

    def get_item_id_blya(self, obj):
        return obj.price.itemInfo_id

    def validate(self, data):
        request = self.context['request']
        user = request.user
        shop = user.profile.oShop
        if shop is None:
            log.error("attempt to add supply not from owner: id {0} username '{1}' ip {2}".format(
                user.id, user.username, get_client_ip(request)))
            raise serializers.ValidationError({'user': "not owner"})
        try:
            price = Price.objects.get(id=data.get('price_id'))
        except ObjectDoesNotExist:
            log.warn("price not exist: '{0}' user '{1}' ip {2}".format(
                data['price_id'], user.username, get_client_ip(request)))
            raise serializers.ValidationError({'price_id': 'item not exist'})
        if price.is_deleted:
            log.warn("attempt to create supply with deleted price: '{0}' user '{1}' ip {2}".format(
                data['price_id'], user.username, get_client_ip(request)))
            raise serializers.ValidationError({'price_id': 'price deleted'})
        item = price.itemInfo
        if item.shop != shop:
            log.warn("attempt to create supply with price from other shop: '{0}' user '{1}' ip {2}".format(
                data['price_id'], user.username, get_client_ip(request)))
            raise serializers.ValidationError({'price_id': 'not your item'})
        data['price'] = price
        data['shop'] = shop
        return data

    def create(self, validated_data):
        shop = validated_data['shop']
        price = validated_data['price']
        count = validated_data['expected_count']
        date = validated_data['expected_date']
        with transaction.atomic():
            supply = Supply(price=price, shop=shop, expected_count=count, expected_date=date)
            supply.save()
        return supply


class SupplyConfirmSerializer(serializers.Serializer):
    id = serializers.IntegerField()
    real_count = serializers.IntegerField(validators=[above_zero])

    def validate(self, data):
        request = self.context['request']
        user = request.user
        try:
            supply = Supply.objects.get(id=data['id'])
            item = supply.price.itemInfo
            if supply.shop != user.profile.shop and supply.shop != user.profile.oShop:
                log.warn("attempt to confirm supply of another shop item: '{0}' user '{1}' ip {2}".format(
                    supply.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"id": "item is not yours"})
            if supply.done is True:
                log.warn("attempt to confirm already done supply: '{0}' user '{1}' ip {2}".format(
                    supply.id, user.username, get_client_ip(request)))
                raise serializers.ValidationError({"id": "supply is already done"})
            data['item'] = item
            data['supply'] = supply
            data['worker'] = user
        except ObjectDoesNotExist:
            log.warn("attempt to confirm supply of not existed item: '{0}' user '{1}' ip {2}".format(
                data['id'], user.username, get_client_ip(request)))
            raise serializers.ValidationError({'id': 'supply not exist'})
        return data

    def create(self, validated_data):
        request = self.context['request']
        item = validated_data['item']
        supply = validated_data['supply']
        worker = validated_data['worker']
        real_count = validated_data['real_count']
        with transaction.atomic():
            item.count += real_count
            item.save()
            supply.worker = worker
            supply.real_date = datetime.date.today()
            supply.real_count = real_count
            supply.done = True
            supply.save()
        data = {'count': real_count} if real_count != supply.expected_count else None
        push_res = send_push_to_owner(worker, "Была произведена поставка товара.", data)
        if push_res is not None:
            log.debug(push_res)
        log.info("confirm supply: id '{0}' user '{1}' ip {2}".format(
            validated_data['id'], worker.username, get_client_ip(request)))
        return supply
