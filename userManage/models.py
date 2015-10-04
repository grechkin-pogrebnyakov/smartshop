from django.db import models
from django.contrib.auth.models import User
# Create your models here.

class UserProfile(models.Model):
    registrationType = models.CharField(default="inner",max_length=255)
    user = models.ForeignKey(User,unique=True)