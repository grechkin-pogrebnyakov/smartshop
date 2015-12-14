from push_notifications.models import GCMDevice
from django.db.models import Q


def send_push_to_devices(devices, message, data, badge):
    if data is not None and badge is not None:
        return devices.send_message(message, extra=data, bagde=badge)
    elif badge is not None:
        return devices.send_message(message, bagde=badge)
    else:
        return devices.send_message(message)


def send_push_to_workers(shop, message, data=None, badge=None):
    devices = GCMDevice.objects.filter(user__profile__shop=shop)
    return send_push_to_devices(devices, message, data, badge)


def send_push_to_other_workers(worker, message, data=None, badge=None):
    tmp_shop = worker.profile.shop
    if tmp_shop is None:
        shop = worker.profile.oShop
    else:
        shop = tmp_shop
    devices = GCMDevice.objects.filter(Q(user__profile__shop=shop) | Q(user__profile__oShop=shop)).exclude(user=worker)
    return send_push_to_devices(devices, message, data, badge)


def send_push_to_owner(worker, message, data=None, badge=None):
    shop = worker.profile.shop
    if shop is None:
        return None
    devices = GCMDevice.objects.filter(user__profile__oShop=shop)
    return send_push_to_devices(devices, message, data, badge)
