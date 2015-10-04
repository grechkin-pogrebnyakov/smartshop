# coding: utf-8
__author__ = 'mid'

from django.conf.urls import include, url, patterns
from userManage import views
urlpatterns = patterns(
    url(r'^$',views.login),
    url(r'^login',views.login),
    url(r'^registration',views.register),
    url(r'', include('social_auth.urls')),
)