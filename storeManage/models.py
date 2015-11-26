from django.db import models
# Create your models here.
from pygments.lexers import get_lexer_by_name
from pygments.formatters.html import HtmlFormatter
from pygments import highlight


class Shop(models.Model):
    name = models.CharField(max_length=255)



class Item(models.Model):
    id = models.IntegerField(primary_key=True)
    name = models.CharField(max_length=255)
    code = models.CharField(max_length=255)
    shop = models.ForeignKey(Shop, related_name='items')



