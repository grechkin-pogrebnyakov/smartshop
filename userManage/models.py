from django.db import models
from  django.contrib.auth.models import User
from storeManage.models import Shop
#import storeManage.models as m
# Create your models here.

class UserProfile(models.Model):
    user = models.OneToOneField(User, related_name='profile')
    registrationType = models.CharField(default="inner",max_length=20)
    accountType = models.CharField(default='worker',max_length=20)
    father_name = models.CharField(default="",max_length=40)
    shop = models.ForeignKey(Shop,related_name='workers', null=True)
    oShop = models.OneToOneField(Shop,related_name='owner', null=True)
    defaultPassword = models.BooleanField(default=False)
