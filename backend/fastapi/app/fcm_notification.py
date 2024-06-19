from pyfcm import FCMNotification
from .config import Config

push_service = FCMNotification(Config.fcm_server_key)


async def send_push_notification(token, title, body, data):

    result = push_service.notify_single_device(
        registration_id=token,
        message_title=title,
        message_body=body,
        data_message=data
    )
    print("[INFO] push notification sent")

    return result

