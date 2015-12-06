# coding: utf-8
from django.http import HttpRequest
from rest_framework.views import APIView
from rest_framework.generics import GenericAPIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework import status, authentication, permissions
from rest_framework.authtoken.models import Token

from allauth.account.views import SignupView, ConfirmEmailView
from allauth.account.utils import complete_signup
from allauth.account import app_settings
from django.conf import settings
from django.contrib.auth import login

from rest_auth.app_settings import TokenSerializer, LoginSerializer
from rest_auth.registration.serializers import RegisterEmployeeSerializer, UserSerializer, VkRegisterSerializer
from rest_auth.views import LoginView
import logging
from my_utils import get_client_ip
from userManage.models import Shop, UserProfile, User


log = logging.getLogger('smartshop.log')

class RegisterView(APIView, SignupView):
    """
    Accepts the credentials and creates a new user
    if user does not exist already
    Return the REST Token if the credentials are valid and authenticated.
    Calls allauth complete_signup method

    Accept the following POST parameters: username, email, password
    Return the REST Framework Token Object's key.
    """

    permission_classes = (AllowAny,)
    allowed_methods = ('POST', 'OPTIONS', 'HEAD')
    token_model = Token
    serializer_class = UserSerializer

    def get(self, *args, **kwargs):
        log.warn('reqiest for unsupported method. client_ip {0}'.format(get_client_ip(self.request)))
        return Response({}, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def put(self, *args, **kwargs):
        log.warn('reqiest for unsupported method. client_ip {0}'.format(get_client_ip(self.request)))
        return Response({}, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def form_valid(self, form):
        self.user = form.save(self.request)
        self.token, created = self.token_model.objects.get_or_create(
            user=self.user
        )
        oShop = Shop(name=self.user.username)
        oShop.save()
        profile = UserProfile(accountType='owner', oShop=oShop, user=self.user )
        profile.save()
        if isinstance(self.request, HttpRequest):
            request = self.request
        else:
            request = self.request._request
        return complete_signup(request, self.user,
                               app_settings.EMAIL_VERIFICATION,
                               self.get_success_url())

    def get_form_kwargs(self, *args, **kwargs):
        kwargs = super(RegisterView, self).get_form_kwargs(*args, **kwargs)
        kwargs['data'] = self.request.data
        return kwargs

    def post(self, request, *args, **kwargs):
        self.initial = {}
        form_class = self.get_form_class()
        self.form = self.get_form(form_class)
        if self.form.is_valid():
            self.form_valid(self.form)
            log.info("registration: usernanme '{0}' ip {1}".format(
                self.request.data.get('username'), get_client_ip(request)))
            return self.get_response()
        else:
            log.info("registration error: '{0}' ip {1}".format(
                self.form.errors, get_client_ip(request)))
            return self.get_response_with_errors()

    def get_response(self):
        serializer = TokenSerializer(instance=self.token,
                                           context={'request': self.request})
        return Response(serializer.data, status=status.HTTP_201_CREATED)

    def get_response_with_errors(self):
        return Response(self.form.errors, status=status.HTTP_400_BAD_REQUEST)

class RegisterEmployeeView(GenericAPIView):
    """
    Accepts the credentials and creates a new user
    if user does not exist already
    Return the REST Token if the credentials are valid and authenticated.
    Calls allauth complete_signup method

    Accept the following POST parameters: username, email, password
    Return the REST Framework Token Object's key.
    """

    permission_classes = (IsAuthenticated,)
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication)
    allowed_methods = ('POST', 'OPTIONS', 'HEAD')
    serializer_class = RegisterEmployeeSerializer

    def get(self, *args, **kwargs):
        log.warn('reqiest for unsupported method. client_ip {0}'.format(get_client_ip(self.request)))
        return Response({}, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def put(self, *args, **kwargs):
        log.warn('reqiest for unsupported method. client_ip {0}'.format(get_client_ip(self.request)))
        return Response({}, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def post(self, request, *args, **kwargs):
        if( not self.request.user.profile.accountType == 'owner' ):
            log.warn('worker register request from worker. client_ip {0}, username {1}'.format(get_client_ip(self.request), self.request.user.username))
            return self.get_response_with_errors({'details':'request is not from owner'})
        self.serializer = self.get_serializer(data=request.data)
        if not self.serializer.is_valid():
            log.warn("error adding worker: '{0}' user '{1}' ip {2}".format(
                self.serializer.errors,self.request.user.username, get_client_ip(request)))
            return self.get_response_with_errors()
        self.serializer.save(owner=self.request.user)
        log.info("add worker '{0}' user '{1}' ip {2}".format(
            self.serializer.login,self.request.user.username, get_client_ip(request)))
        return Response({'login': self.serializer.login, 'temporary_password': self.serializer.password}, status=status.HTTP_201_CREATED)

    def get_response_with_errors(self, error = None):
        if( error is None ):
            error = self.serializer.errors
        return Response(error, status=status.HTTP_400_BAD_REQUEST)

class VkRegisterView(GenericAPIView):
    """
    Accepts the credentials and creates a new user
    if user does not exist already
    Return the REST Token if the credentials are valid and authenticated.
    Calls allauth complete_signup method

    Accept the following POST parameters: username, email, password
    Return the REST Framework Token Object's key.
    """

    permission_classes = (AllowAny,)
#    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication)
    allowed_methods = ('POST', 'OPTIONS', 'HEAD')
    token_model = Token
    response_serializer = TokenSerializer
    serializer_class = VkRegisterSerializer

    def login(self):
        self.user = self.login_serializer.validated_data['user']
        self.token, created = self.token_model.objects.get_or_create(
            user=self.user)
        if getattr(settings, 'REST_SESSION_LOGIN', True):
            login(self.request, self.user)

    def get(self, *args, **kwargs):
        log.warn('reqiest for unsupported method. client_ip {0}'.format(get_client_ip(self.request)))
        return Response({}, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def put(self, *args, **kwargs):
        log.warn('reqiest for unsupported method. client_ip {0}'.format(get_client_ip(self.request)))
        return Response({}, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def get_response(self):
        resp = self.response_serializer(self.token).data
        resp.update({'is_worker': (self.user.profile.accountType == 'worker'), 'default_password': self.user.profile.defaultPassword})
        return Response(
            resp, status=status.HTTP_200_OK
        )

    def get_error_response(self):
        return Response(
            {'response':self.login_serializer.errors}, status=status.HTTP_400_BAD_REQUEST
        )

    def post(self, request, *args, **kwargs):
        self.serializer = self.get_serializer(data=request.data)
        if not self.serializer.is_valid():
            log.warn("error vk login: '{0}' ip {1}".format(
                self.serializer.errors, get_client_ip(request)))
            return self.get_response_with_errors()
        username = 'vk_user_'+self.serializer.validated_data.get('user_id')
        users = User.objects.filter(username = username)
        if( len(users) == 0 ) :
            self.serializer.save()
        self.login_serializer = LoginSerializer(data={'username':username, 'password':'123456'})
        if ( not self.login_serializer.is_valid() ):
            log.error("mazafaka!!! {0} ip {1}".format(self.login_serializer.errors, get_client_ip(request)))
            return self.get_error_response()
        self.login()
        accessToken = self.serializer.validated_data.get('access_token')
        if (self.user.profile.accessToken != accessToken) :
            self.user.profile.accessToken = accessToken
            self.user.save()
        log.info("vk login: user '{0}' ip {1}".format(
                self.user.username, get_client_ip(request)))
        return self.get_response()

    def get_response_with_errors(self, error = None):
        if( error is None ):
            error = self.serializer.errors
        return Response(error, status=status.HTTP_400_BAD_REQUEST)

class VerifyEmailView(APIView, ConfirmEmailView):

    permission_classes = (AllowAny,)
    allowed_methods = ('POST', 'OPTIONS', 'HEAD')

    def get(self, *args, **kwargs):
        log.warn('reqiest for unsupported method. client_ip {0}'.format(get_client_ip(self.request)))
        return Response({}, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def post(self, request, *args, **kwargs):
        self.kwargs['key'] = self.request.data.get('key', '')
        confirmation = self.get_object()
        confirmation.confirm(self.request)
        return Response({'message': 'ok'}, status=status.HTTP_200_OK)


# class SocialLoginView(LoginView):
#     """
#     class used for social authentications
#     example usage for facebook with access_token
#     -------------
#     from allauth.socialaccount.providers.facebook.views import FacebookOAuth2Adapter
#
#     class FacebookLogin(SocialLoginView):
#         adapter_class = FacebookOAuth2Adapter
#     -------------
#
#     example usage for facebook with code
#
#     -------------
#     from allauth.socialaccount.providers.facebook.views import FacebookOAuth2Adapter
#     from allauth.socialaccount.providers.oauth2.client import OAuth2Client
#
#     class FacebookLogin(SocialLoginView):
#         adapter_class = FacebookOAuth2Adapter
#          client_class = OAuth2Client
#          callback_url = 'localhost:8000'
#     -------------
#     """
#
#     serializer_class = SocialLoginSerializer
