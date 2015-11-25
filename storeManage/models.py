from django.db import models
from userManage.models import UserProfile
# Create your models here.
from pygments.lexers import get_lexer_by_name
from pygments.formatters.html import HtmlFormatter
from pygments import highlight
from django.contrib.auth.models import User


class Item(models.Model):
    id = models.IntegerField(primary_key=True)
    name = models.CharField(max_length=255)
    code = models.CharField(max_length=255)

class Shop(models.Model):
    name = models.CharField(max_length=255)
    items = models.ForeignKey(Item, related_name='shop')




