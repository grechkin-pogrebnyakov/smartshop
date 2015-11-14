__author__ = 'mid'
import rest_framework.serializers
from rest_framework.serializers import ModelSerializer,PrimaryKeyRelatedField
from models import UserProfile
from rest_framework import serializers
from django.contrib.auth.models import User
from django.http import HttpRequest

class UserSerializer(ModelSerializer):
    def _get_request(self):
        request = self.context.get('request')
        if not isinstance(request, HttpRequest):
            request = request._request
        return request
    #user = serializers.PrimaryKeyRelatedField(many=False,read_only=True,queryset=_get_request().user)
    class Meta:
        model = UserProfile
        fields = ('id', 'accountType','registrationType')

