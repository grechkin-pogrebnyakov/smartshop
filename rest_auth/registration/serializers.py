from rest_framework import serializers
from userManage.models import User, UserProfile, Shop
from my_utils import generate_password
import requests
import logging
from django.db import transaction

log = logging.getLogger('smartshop.log')


class UserSerializer(serializers.Serializer):
    username = serializers.CharField(max_length=128)
    password1 = serializers.CharField(max_length=128)
    password2 = serializers.CharField(max_length=128)


class RegisterEmployeeSerializer(serializers.Serializer):
    """
    User model w/o password
    """
    first_name = serializers.CharField(max_length=128)
    last_name = serializers.CharField(max_length=128)
    father_name = serializers.CharField(max_length=128, required=False, allow_blank=True)
    login = ''
    password = ''

    def create(self, validated_data):
        owner = validated_data.get('owner')
        with transaction.atomic():
            oShop = owner.profile.oShop
            number = len(UserProfile.objects.filter(shop=oShop)) + 1
            self.login = "{0}_employee_{1}".format(owner.username,number)
            self.password = generate_password()
            user = User(username = self.login)
            user.set_password(self.password)
            user.last_name = validated_data.get("last_name")
            user.first_name = validated_data.get("first_name")
            user.save()
            profile = UserProfile()
            father_name = validated_data.get("father_name")
            if father_name:
                profile.father_name = father_name
            profile.shop = oShop
            profile.accountType = 'worker'
            profile.defaultPassword = True
            profile.user = user
            profile.save()
        return user


class VkRegisterSerializer(serializers.Serializer):
    user_id = serializers.CharField(max_length=255, write_only=True)
    first_name = serializers.CharField(max_length=255,allow_blank=True,default='')
    last_name = serializers.CharField(max_length=255,allow_blank=True,default='')
    email = serializers.EmailField(allow_blank=True,default='')
    access_token = serializers.CharField(max_length=255,write_only=True)

    def validate(self, attrs):
        try:
            uid = int(attrs['user_id'])
        except:
            log.warn("user_id is not int user_id='{0}'".format(attrs['user_id']))
            raise serializers.ValidationError('user_id is not number')
        url = 'https://api.vk.com/method/users.get?&access_token={0}'.format(attrs['access_token'])
        r = requests.get(url)
        data = r.json()
        resp = data.get('response')
        if resp is None:
            error = data.get('error')
            log.warn("error vk login: '{0}'".format(error))
            code = int(error.get('error_code'))
            if code == 5:
                raise serializers.ValidationError('wrong access token')
            elif code == 10:
                raise serializers.ValidationError('wrong access token: unknown app')
            else:
                raise serializers.ValidationError(error)
        log.info("got data from vk: '{0}'".format(resp))
        if len(resp) == 0:
            log.warn("got zero length response from vk: {0}".format(data))
        user_data = resp[0]
        if user_data.get('uid') != uid:
            raise serializers.ValidationError("wrong access token")
        return attrs

    def create(self, validated_data):
        login = 'vk_user_'+validated_data.get('user_id')
        user = User(username=login,first_name=validated_data.get('first_name'),
                    last_name=validated_data.get('last_name'),email=validated_data.get('email'))
        user.set_password('123456')
        user.save()
        oShop = Shop(name=user.username)
        oShop.save()
        profile = UserProfile(accountType='owner', oShop=oShop, user=user, registrationType='vk',
                              accessToken=validated_data.get('access_token'))
        profile.save()
        self.user = user
        return user
