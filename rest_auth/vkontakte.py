__author__ = 'mid'

from allauth.socialaccount.providers.vk.provider import VKProvider
from rest_auth.registration.views import SocialLoginView

class VkLogin(SocialLoginView):
    adapter_class = VKProvider



