from django.conf.urls import include, url,patterns
import allauth
from django.contrib import admin
from rest_auth.registration.views import RegisterEmployeeView
from rest_auth.views import EmpoyeesListView
admin.autodiscover()
urlpatterns = patterns('',
    url(r'^accounts/', include('allauth.urls')),
    url(r'^admin/', include(admin.site.urls)),
    url(r'^api/auth/',include('rest_auth.urls')),
    url(r'^api/shop/',include('storeManage.urls')),
    url(r'^api/profile/',include('userManage.urls')),
    url(r'^api/employee/register/$',RegisterEmployeeView.as_view()),
    url(r'^api/employee/list/$',EmpoyeesListView.as_view()),
    )
