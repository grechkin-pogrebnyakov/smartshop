from django.conf.urls import patterns, url, include
from rest_auth.registration.views import RegisterView,RegisterEmployeeView
from rest_auth.views import (
    LoginView, LogoutView, UserDetailsView, PasswordChangeView,
    PasswordResetView, PasswordResetConfirmView,
)

urlpatterns = patterns(
    '',
    url(r'^registration/$',RegisterView.as_view()),
    url(r'^employee/register$',RegisterEmployeeView.as_view()),
    # URLs that do not require a session or valid token
    url(r'^password/reset/$', PasswordResetView.as_view(),
        name='rest_password_reset'),
    url(r'^password/reset/confirm/$', PasswordResetConfirmView.as_view(),
        name='rest_password_reset_confirm'),
    url(r'^login/$', LoginView.as_view(), name='rest_login'),
    # URLs that require a user to be logged in with a valid session / token.
    url(r'^logout/$', LogoutView.as_view(), name='rest_logout'),
    url(r'^user/$', UserDetailsView.as_view(), name='rest_user_details'),
    url(r'^password/change/$', PasswordChangeView.as_view(),
        name='rest_password_change'),
)
