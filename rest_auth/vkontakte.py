__author__ = 'mid'

from allauth.socialaccount.providers.vk.provider import VKProvider,OAuth2Provider,VKAccount
from rest_auth.registration.views import SocialLoginView
from allauth.socialaccount.providers.oauth2.client import OAuth2Client

class VkLogin(SocialLoginView):
    adapter_class = VKProvider
    client_class = OAuth2Client
    callback_url = 'https://smartshop1.ddns.net:81/api/auth/vk/'


