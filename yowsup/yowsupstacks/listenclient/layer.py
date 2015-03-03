
import os
import sys

from yowsup.layers.interface                           import YowInterfaceLayer, ProtocolEntityCallback
from yowsup.layers.protocol_messages.protocolentities  import TextMessageProtocolEntity
from yowsup.layers.protocol_media.protocolentities     import ImageDownloadableMediaMessageProtocolEntity
from yowsup.layers.protocol_receipts.protocolentities  import OutgoingReceiptProtocolEntity
from yowsup.layers.protocol_media.protocolentities     import LocationMediaMessageProtocolEntity
from yowsup.layers.protocol_acks.protocolentities      import OutgoingAckProtocolEntity
from yowsup.layers.protocol_media.protocolentities     import VCardMediaMessageProtocolEntity


class NonQueueException(Exception): pass

class ListenLayer(YowInterfaceLayer):

    @staticmethod
    def setqueue(queue):
        ListenLayer.queue = queue

    @ProtocolEntityCallback("message")
    def onMessage(self, messageProtocolEntity):
        if not messageProtocolEntity.isGroupMessage():
            if messageProtocolEntity.getType() == 'text':
                self.onTextMessage(messageProtocolEntity)

    # Receptor genera ticket azul
    @ProtocolEntityCallback("receipt")
    def onReceipt(self, entity):
        ack = OutgoingAckProtocolEntity(entity.getId(), "receipt", "delivery")
        self.toLower(ack)

    def onTextMessage(self, messageProtocolEntity):
        receipt = OutgoingReceiptProtocolEntity(messageProtocolEntity.getId(), messageProtocolEntity.getFrom())
        self.toLower(receipt)
        line = [messageProtocolEntity.getFrom(), '=', messageProtocolEntity.getBody()]

        if not hasattr(ListenLayer, 'queue'):
            raise NonQueueException()
        ListenLayer.queue.put(line)
