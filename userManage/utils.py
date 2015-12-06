from push_notifications.models import GCMDevice
from django.db.models import Q


def send_push_to_workers(shop, message, dict=None):
    devices = GCMDevice.objects.filter(user__profile__shop=shop)
    if dict is None :
        return devices.send_message(message)
    return devices.send_message(message, dict)


def send_push_to_other_workers(worker, message, dict=None):
    tmp_shop = worker.profile.shop
    if tmp_shop is None:
        shop = worker.profile.oShop
    else:
        shop = tmp_shop
    devices = GCMDevice.objects.filter(Q(user__profile__shop=shop) | Q(user__profile__oShop=shop)).exclude(user = worker)
    if dict is None :
        return devices.send_message(message)
    return devices.send_message(message, dict)