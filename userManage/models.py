from django.db import models
from django.contrib.auth.models import User
from storeManage.models import Shop
# Create your models here.

class UserProfile(models.Model):
    user = models.OneToOneField(User, related_name='profile')
    registrationType = models.CharField(default="inner",max_length=20)
    accountType = models.CharField(default='worker',max_length=20)
    father_name = models.CharField(default="",max_length=40)
    shop = models.ForeignKey(Shop,related_name='workers')
    defaultPassword = models.BooleanField(default=False)
