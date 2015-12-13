# coding: utf-8
from django.db import models
#from userManage.models import User
#from userManage.models import Shop
from  django.contrib.auth.models import User
# Create your models here.


class Shop(models.Model):
    name = models.CharField(max_length=255)


class Item(models.Model):
    productName = models.CharField(max_length=255)
    descriptionProduct = models.CharField(max_length=255, default='')
    productBarcode = models.CharField(max_length=255, default='')
    count = models.IntegerField()
    shop = models.ForeignKey(Shop)
    price = models.OneToOneField('Price', related_name='item')
    new_price = models.OneToOneField('Price', null=True, related_name='i_hate_this_fucking_django')
    image = models.ImageField(upload_to='media/items')
    image_hash = models.CharField(max_length=32, blank=True, default='')


class Price(models.Model):
    # на самом деле, null быть не может, но это нужно для того, что бы создать цену до итема
    itemInfo = models.ForeignKey(Item, related_name='prices', null=True)
    priceSellingProduct = models.FloatField()
    pricePurchaseProduct = models.FloatField()
    is_deleted = models.BooleanField(default=False)
    startDate = models.DateTimeField(null=True)
    changer = models.ForeignKey(User, null=True)


class Check(models.Model):
    author = models.ForeignKey(User)
    creation_time = models.DateTimeField(auto_now_add=True)
    # type = 0 -- sell, 2 -- discard, 1 -- supply (DEPRECATED)
    type = models.IntegerField()
    shop = models.ForeignKey(Shop)


class CheckPosition(models.Model):
    relatedCheck = models.ForeignKey(Check, related_name='check_positions')
    item = models.ForeignKey(Item)
    count = models.IntegerField()
    price = models.ForeignKey(Price)


class Supply(models.Model):
    price = models.ForeignKey(Price)
    worker = models.ForeignKey(User, null=True)
    expected_date = models.DateTimeField()
    real_date = models.DateTimeField(null=True)
    expected_count = models.IntegerField()
    real_count = models.IntegerField(default=0)
    done = models.BooleanField(default=False)
    shop = models.ForeignKey(Shop)
