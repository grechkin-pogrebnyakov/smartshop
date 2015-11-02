# coding: utf-8
__author__ = 'mid'

from django.conf.urls import include, url, patterns
from userManage import views
urlpatterns = patterns('',
    url(r'^$',views.login_api),
    url(r'^login',views.login_api),
    url(r'^registration',views.register_api),
    url(r'^logout',views.logout_api),
    url(r'^api-auth/', include('rest_framework.urls',
                               namespace='rest_framework')),
)
