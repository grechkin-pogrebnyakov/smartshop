__author__ = 'mid'
import rest_framework.serializers
from rest_framework.serializers import ModelSerializer,PrimaryKeyRelatedField
from models import UserProfile
from rest_framework import serializers
from django.contrib.auth.models import User
from django.http import HttpRequest


class UserSerializer(ModelSerializer):
    #accountType = serializers.CharField(source='UserProfile.accountType')
    class Meta:
        model = User
        fields = ('username',)

