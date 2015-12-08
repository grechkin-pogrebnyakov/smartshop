"""
Django settings for SmartShop project.

Generated by 'django-admin startproject' using Django 1.8.

For more information on this file, see
https://docs.djangoproject.com/en/1.8/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/1.8/ref/settings/
"""

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/1.8/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = 'o*g5f59=9ih19jo_bjy#8-2i-javgg)%%$ro1u^yi5-8@w(*m1'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True

ALLOWED_HOSTS = []


# Application definition

INSTALLED_APPS = (
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.sites',
    'rest_framework.authtoken',
    'django_extensions',
    'rest_framework',
    'rest_auth',
    'rest_auth.registration',
    'storeManage',
    'userManage',
    'allauth',
    'allauth.account',
    'allauth.socialaccount',
    'allauth.socialaccount.providers.vk',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'push_notifications',
)

MIDDLEWARE_CLASSES = (
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.auth.middleware.SessionAuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
    'django.middleware.security.SecurityMiddleware',
    'my_utils.DisableCSRF',
)

ROOT_URLCONF = 'SmartShop.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'SmartShop.wsgi.application'


# Database
# https://docs.djangoproject.com/en/1.8/ref/settings/#databases

DATABASES = {
     'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'SmartShop',
        'USER': 'root',
        'PASSWORD': '1234QWer',
        'HOST': 'localhost',
        'PORT': '3306',
        'OPTIONS': {
                    'charset': 'utf8',
                    'use_unicode': True, },

    }
}

AUTHENTICATION_BACKENDS = (
    'django.contrib.auth.backends.ModelBackend',
)

# Internationalization
# https://docs.djangoproject.com/en/1.8/topics/i18n/

LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_L10N = True

USE_TZ = True


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/1.8/howto/static-files/

STATIC_URL = '/static/'
MEDIA_ROOT = '/home/SmartShopMedia/'
MEDIA_URL = ''

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'filters': {
	'require_debug_true': {
            '()': 'django.utils.log.RequireDebugTrue',
        },
        'require_debug_false': {
            '()': 'django.utils.log.RequireDebugFalse'
        }
    },
    'formatters': {
        'error': {
            'format': '%(asctime)s [%(levelname)s pid:%(process)d tid:%(thread)d] %(message)s (%(pathname)s:%(lineno)d)'
        },
        'access': {
            'format': '%(asctime)s %(message)s'
        },
    },
    'handlers': {
        'console': {
            'level': 'ERROR',
            'class': 'logging.StreamHandler',
        },
        'error_log_file_debug': {
            'level': 'DEBUG',
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': '/var/log/smartshop/error.log',
            'maxBytes': 1024 * 1024 * 5,
            'backupCount': 7,
            'formatter': 'error',
	    'filters': ['require_debug_true'],
        },
        'error_log_file': {
            'level': 'WARNING',
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': '/var/log/smartshop/error.log',
            'maxBytes': 1024 * 1024 * 5,
            'backupCount': 7,
            'formatter': 'error',
	    'filters': ['require_debug_false'],
        },
        'django_log_file_debug': {
            'level': 'DEBUG',
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': '/var/log/smartshop/django_error.log',
            'maxBytes': 1024 * 1024 * 5,
            'backupCount': 7,
            'formatter': 'error',
	    'filters': ['require_debug_true'],
        },
        'django_log_file': {
            'level': 'WARNING',
            'class': 'logging.handlers.RotatingFileHandler',
            'filename': '/var/log/smartshop/django_error.log',
            'maxBytes': 1024 * 1024 * 5,
            'backupCount': 7,
            'formatter': 'error',
	    'filters': ['require_debug_false'],
        }
    },
    'loggers': {
        'django': {
            'handlers': ['console', 'django_log_file', 'django_log_file_debug'],
            'level': 'DEBUG',
            'propagate': True,
        },
        'smartshop.log':{
            'handlers': ['error_log_file', 'error_log_file_debug'],
            'propagate': False,
            'level': 'DEBUG',
        },
    }
}

REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': (
        'rest_framework.authentication.TokenAuthentication',
        #'rest_framework.authentication.BasicAuthentication',
        #'rest_framework.authentication.SessionAuthentication',
    )
}

VKAPP_APP_ID = "5093720"
VKAPP_API_SECRET = "bFCtfRc6NhGBhuFy7CYw"
SITE_ID = 3
ACCOUNT_DEFAULT_HTTP_PROTOCOL = 'https'

TEMPLATE_CONTEXT_PROCESSORS = {
    'allauth.socialaccount.context_processors.socialaccount'
}

PUSH_NOTIFICATIONS_SETTINGS = {
        'GCM_API_KEY': 'AIzaSyAbnX99ai1LJSPWmqPECK_uAhuTFZEYXCI',

}
