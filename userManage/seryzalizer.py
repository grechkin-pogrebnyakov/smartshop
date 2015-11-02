__author__ = 'mid'
import rest_framework.serializers
from rest_framework.serializers import ModelSerializer,PrimaryKeyRelatedField
from models import UserProfile


class UserSerializer(ModelSerializer):
    class Meta:
        model = UserProfile
        fields = ('id', 'username')