from django.db import models
from django.contrib.auth.models import User
from storeManage.models import Shop
# Create your models here.

class UserProfile(models.Model):
    registrationType = models.CharField(default="inner",max_length=255)
    user = models.OneToOneField(User,unique=True)
    accountType = models.CharField(default='worker',max_length=255)

class OwnerProfile(UserProfile):
    shop = models.OneToOnefield(Shop,related_name='owner')

class WorkerProfile(UserProfile):
    defaultPassword = models.BooleanField(default=False)
    shop = models.ForeignKey(Shop,related_name='workers')
