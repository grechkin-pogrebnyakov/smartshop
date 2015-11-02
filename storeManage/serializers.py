__author__ = 'mid'

from rest_framework import serializers
from storeManage.models import Shop,Item

class ShopSerializer(serializers.ModelSerializer):
    class Meta:
        model = Shop
        fields=('name')
