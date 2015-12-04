__author__ = 'mid'
import rest_framework.serializers
from rest_framework.serializers import ModelSerializer,PrimaryKeyRelatedField
from models import UserProfile
from rest_framework import serializers
from django.contrib.auth.models import User
from django.http import HttpRequest
from push_notifications.models import GCMDevice


class UserSerializer(ModelSerializer):
    #accountType = serializers.CharField(source='UserProfile.accountType')
    class Meta:
        model = User
        fields = ('username',)


class GcmIdSerializer(serializers.Serializer):
    gsm_registration_id = serializers.CharField(max_length=255, write_only=True)

    def create(self, validated_data):
        user = validated_data.get('user')
        registration_id = validated_data.get('gsm_registration_id')
        device = None
        devices=GCMDevice.objects.filter(user=user.id)
        if( len(devices) == 0 ):
            device = GCMDevice(user=user)
        else:
            device = devices[0]
        device.registration_id = registration_id
        device.save()
        return device