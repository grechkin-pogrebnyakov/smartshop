# coding: utf-8
__author__ = 'mid'

from django.conf.urls import include, url, patterns
from userManage import views
urlpatterns = patterns('',
    url(r'^$',views.UserProfileView.as_view()),

)
