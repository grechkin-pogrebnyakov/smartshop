__author__ = 'mid'
from django.conf.urls import include, url,patterns
from django.contrib import admin
from storeManage import views
from rest_framework.authtoken import views as t_v

urlpatterns = patterns('',
    url(r'^/',views.Store.as_view()),
    url(r'^api-token-auth/', t_v.obtain_auth_token)
    #url(r'^items/',include())
)