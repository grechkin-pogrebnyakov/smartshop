from django.db import models
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


