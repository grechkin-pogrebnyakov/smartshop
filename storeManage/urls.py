__author__ = 'mid'
from django.conf.urls import include, url,patterns
from django.contrib import admin
from storeManage import views

urlpatterns = patterns('',
    url(r'^/',views.Store.as_view())
    #url(r'^items/',include())
)