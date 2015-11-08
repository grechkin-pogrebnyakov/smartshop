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














