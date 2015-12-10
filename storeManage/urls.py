__author__ = 'mid'
from django.conf.urls import include, url,patterns
from django.contrib import admin
from storeManage import views
from rest_framework.authtoken import views as t_v

urlpatterns = patterns('',
    url(r'^/',views.Store.as_view()), #TODO: remove tris
    url(r'^item/add/$',views.Item.as_view()),
    url(r'^item/list/$',views.Item.as_view()),
    url(r'^check/add/$',views.CheckView.as_view()),
    url(r'^check/list/$',views.CheckView.as_view()),
    url(r'^item/update/$',views.ItemUpdate.as_view()),
    url(r'^item/change_price_list/$',views.ItemListChangePrice.as_view()),
    url(r'^item/confirm_price_update/$',views.ItemConfirmPriceUpdate.as_view()),

    url(r'^api-token-auth', t_v.obtain_auth_token),
    #url(r'^items/',include())
)