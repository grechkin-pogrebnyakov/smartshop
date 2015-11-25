from django.db import models
from django.contrib.auth.models import User
from storeManage.models import Shop
# Create your models here.

class UserProfile(models.Model):
    user = models.OneToOneField(User)
    registrationType = models.CharField(default="inner",max_length=255)
    accountType = models.CharField(default='worker',max_length=255)
    father_name = models.CharField(default='',max_length=255)
    first_name = models.CharField(max_length=255)
    last_name = models.CharField(max_length=255)

class OwnerProfile(UserProfile):
    shop = models.OneToOneField(Shop,related_name='owner')

class WorkerProfile(UserProfile):
    defaultPassword = models.BooleanField(default=True)
    shop = models.ForeignKey(Shop,related_name='workers')
