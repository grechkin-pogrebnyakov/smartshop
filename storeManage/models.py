from django.db import models
#from userManage.models import User
#from userManage.models import Shop
from  django.contrib.auth.models import User
# Create your models here.

class Shop(models.Model):
    name = models.CharField(max_length=255)

class Item(models.Model):
    productName = models.CharField(max_length=255)
    descriptionProduct = models.CharField(max_length=255)
    priceSellingProduct = models.FloatField()
    pricePurchaseProduct = models.FloatField()
    productBarcode = models.CharField(max_length=255)
    count = models.IntegerField()
    shop = models.ForeignKey(Shop)

class Check(models.Model):
    author = models.ForeignKey(User)
    creation_time = models.DateTimeField(auto_now_add=True)
    #type = 0 -- sell, 1 -- sypply, 2 -- discard
    type = models.IntegerField()

class CheckPosition(models.Model):
    relatedCheck = models.ForeignKey(Check, related_name='check_positions')
    item = models.ForeignKey(Item)
    count = models.IntegerField()
    priceSellingProduct = models.FloatField()
    pricePurchaseProduct = models.FloatField()
