from push_notifications.models import GCMDevice


def send_push_to_workers(shop, message, dict=None):
    devices = GCMDevice.objects.filter(user__profile__shop=shop)
    if dict is None :
        return devices.send_message(message)
    return devices.send_message(message, dict)


def send_push_to_other_workers(worker, message, dict=None):
    devices = GCMDevice.objects.filter(user__profile__shop=worker.Shop, user__userprofile__oshop=worker.Shop).exclude(user=worker)
    if dict is None :
        return devices.send_message(message)
    return devices.send_message(message, dict)