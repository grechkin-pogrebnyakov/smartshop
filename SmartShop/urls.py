from django.conf.urls import include, url,patterns
from django.contrib import admin

urlpatterns = patterns('',
    url(r'^admin/', include(admin.site.urls)),
    url(r'^api/auth/',include('rest_auth.urls')),
    url(r'^api/store/',include('storeManage.urls')),
    url(r'^api/profile/',include('userManage.urls')),
    )
