from django.contrib.auth import login, logout
from django.conf import settings

from rest_framework import status, authentication
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.generics import GenericAPIView
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.authtoken.models import Token
from rest_framework.generics import RetrieveUpdateAPIView
import logging
from my_utils import get_client_ip

log = logging.getLogger('smartshop.log')

from .app_settings import (
    TokenSerializer, UserDetailsSerializer, LoginSerializer,
    PasswordResetSerializer, PasswordResetConfirmSerializer,
    PasswordChangeSerializer
)


class LoginView(GenericAPIView):

    """
    Check the credentials and return the REST Token
    if the credentials are valid and authenticated.
    Calls Django Auth login method to register User ID
    in Django session framework

    Accept the following POST parameters: username, password
    Return the REST Framework Token Object's key.
    """
    permission_classes = (AllowAny,)
    serializer_class = LoginSerializer
    token_model = Token
    response_serializer = TokenSerializer

    def login(self):
        self.user = self.serializer.validated_data['user']
        self.token, created = self.token_model.objects.get_or_create(
            user=self.user)
        if getattr(settings, 'REST_SESSION_LOGIN', True):
            login(self.request, self.user)

    def get_response(self):
        resp = self.response_serializer(self.token).data
        resp.update({'is_worker': (self.user.profile.accountType == 'worker'), 'default_password': self.user.profile.defaultPassword})
        return Response(
            resp, status=status.HTTP_200_OK
        )

    def get_error_response(self):
        return Response(
            self.serializer.errors, status=status.HTTP_400_BAD_REQUEST
        )

    def post(self, request, *args, **kwargs):
        self.serializer = self.get_serializer(data=self.request.data)
        if not self.serializer.is_valid():
            log.info('invalid data. client_ip {0}'.format(get_client_ip(self.request)))
            return self.get_error_response()
        self.login()
        return self.get_response()


class LogoutView(APIView):

    """
    Calls Django logout method and delete the Token object
    assigned to the current User object.

    Accepts/Returns nothing.
    """
    permission_classes = (AllowAny,)

    def post(self, request):
        try:
            request.user.auth_token.delete()
        except:
            log.warn('logout whithout auth_token. client_ip {0}, login "{1}"'.format(get_client_ip(self.request), request.user.username))

        logout(request)

        return Response({"success": "Successfully logged out."},
                        status=status.HTTP_200_OK)

class EmpoyeesListView(APIView):
    permission_classes = (IsAuthenticated,)
    authentication_classes = (authentication.TokenAuthentication,authentication.SessionAuthentication)
    allowed_methods = ('GET', 'OPTIONS', 'HEAD')

    def post(self, *args, **kwargs):
        log.warn('reqiest for unsupported method. client_ip {0}'.format(get_client_ip(self.request)))
        return Response({}, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def put(self, *args, **kwargs):
        log.warn('reqiest for unsupported method. client_ip {0}'.format(get_client_ip(self.request)))
        return Response({}, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def get_form_kwargs(self, *args, **kwargs):
        kwargs = super(RegisterView, self).get_form_kwargs(*args, **kwargs)
        kwargs['data'] = self.request.data
        return kwargs

    def get(self, request, *args, **kwargs):
        user = self.request.user
        if( not user.profile.accountType == 'owner' ):
            log.warn('worker list request from worker. client_ip {0}, username {1}'.format(get_client_ip(self.request), user.username))
            return self.get_response_with_errors({'details':'request is not from owner'})
        workers = [{'first_name':worker.user.first_name,'last_name':worker.user.last_name,'login':worker.user.username,'father_name':worker.father_name,'default_password':worker.defaultPassword} for worker in user.profile.oShop.workers.all()]
        return Response({'employees': workers}, status=status.HTTP_200_OK)


    def get_response_with_errors(self, errors):
        return Response(errors, status=status.HTTP_400_BAD_REQUEST)

class UserDetailsView(RetrieveUpdateAPIView):

    """
    Returns User's details in JSON format.

    Accepts the following GET parameters: token
    Accepts the following POST parameters:
        Required: token
        Optional: email, first_name, last_name and UserProfile fields
    Returns the updated UserProfile and/or User object.
    """
    serializer_class = UserDetailsSerializer
    permission_classes = (IsAuthenticated,)

    def get_object(self):
        return self.request.user


class PasswordResetView(GenericAPIView):

    """
    Calls Django Auth PasswordResetForm save method.

    Accepts the following POST parameters: email
    Returns the success/fail message.
    """

    serializer_class = PasswordResetSerializer
    permission_classes = (AllowAny,)

    def post(self, request, *args, **kwargs):
        # Create a serializer with request.data
        serializer = self.get_serializer(data=request.data)

        if not serializer.is_valid():
            log.info('invalid password to reset. client ip {0}, login "{1}"'.format(get_client_ip(self.request), request.user.username))
            return Response(serializer.errors,
                            status=status.HTTP_400_BAD_REQUEST)
        serializer.save()
        # Return the success message with OK HTTP status
        return Response(
            {"success": "Password reset e-mail has been sent."},
            status=status.HTTP_200_OK
        )


class PasswordResetConfirmView(GenericAPIView):

    """
    Password reset e-mail link is confirmed, therefore this resets the user's password.

    Accepts the following POST parameters: new_password1, new_password2
    Accepts the following Django URL arguments: token, uid
    Returns the success/fail message.
    """

    serializer_class = PasswordResetConfirmSerializer
    permission_classes = (AllowAny,)

    def post(self, request):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            log.info('invalid data to confirm reset. client ip {0}, login "{1}"'.format(get_client_ip(self.request), request.user.username))
            return Response(
                serializer.errors, status=status.HTTP_400_BAD_REQUEST
            )
        serializer.save()
        return Response({"success": "Password has been reset with the new password."})


class PasswordChangeView(GenericAPIView):

    """
    Calls Django Auth SetPasswordForm save method.

    Accepts the following POST parameters: new_password1, new_password2
    Returns the success/fail message.
    """

    serializer_class = PasswordChangeSerializer
    permission_classes = (IsAuthenticated,)

    def post(self, request):
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            log.info('invalid password to change. client ip {0}, login "{1}"'.format(get_client_ip(self.request), request.user.username))
            return Response(
                serializer.errors, status=status.HTTP_400_BAD_REQUEST
            )
        serializer.save()
        userProfile = request.user.profile
        userProfile.defaultPassword=False
        userProfile.save()
        return Response({"success": "New password has been saved."})
