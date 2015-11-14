__author__ = 'mid'

from allauth.socialaccount.providers.vk.provider import VKProvider,OAuth2Provider
from rest_auth.registration.views import SocialLoginView

class VkLogin(SocialLoginView):
    adapter_class = OAuth2Provider



