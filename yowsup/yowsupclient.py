# -*- coding: utf-8 -*-

import os
import sys
import time
import json
import pickle
from multiprocessing import Process, Queue
from Queue import Empty
from select import select

def _getcredentials(path):
    try:
        f = open(path)
        out = {}
        for l in f:
            line = l.strip()
            if len(line) and line[0] not in ('#',';'):
                prep    = line.split('#', 1)[0].split(';', 1)[0].split('=', 1)
                varname = prep[0].strip()
                val     = prep[1].strip()
                out[varname.replace('-', '_')] = val
        return out["phone"], out["password"]
    except IOError:
        print("Invalid config path: %s" % path)
        sys.exit(1)

class NotCredentialException(Exception): pass

_pathconfig  = os.path.join(os.path.dirname(os.path.abspath(__file__)), "yowsup-cli.config")
_pathmessag  = os.path.join(os.path.dirname(os.path.abspath(__file__)), '.backmessages')
_plisten     = None
_credentials = _getcredentials(_pathconfig)
_messages    = []
_queue   = Queue()
_timeout = 1

def sendmessage(action, to, message):
    '''

    '''
    global _plisten

    from yowsupstacks import sendclient

    kill_lprocess()

    if not _credentials:
        raise NotCredentialException()

    stack = sendclient.YowsupSendStack(_credentials, [([to, "\n".join(message.split("newline"))])], encryptionEnabled=False)

    try:
        stack.start()
    except KeyboardInterrupt:
        return '{"status":"success"}'

def listen():
    global _plisten

    from yowsupstacks import listenclient

    if not _credentials:
        raise NotCredentialException()

    stack = listenclient.YowsupListenStack(_credentials, _queue, encryptionEnabled=False)
    _plisten = Process(target=stack.start)
    _plisten.start()
    return True

def save():
    pickle.dump(_messages, open(_pathmessag, 'wb'))

def restore():
    global _messages
    if os.path.isfile(_pathmessag):
        _messages = pickle.load(open(_pathmessag, 'rb'))

def kill_lprocess():
    global _plisten
    if _plisten:
        _plisten.terminate()
    _plisten = None

def killprogram(action):
    sys.exit(1)

def getmessages(action):
    toret = {}
    i = 0

    while _messages:
        m = _messages.pop(0)
        toret[i] = {'from': m[0], 'message': m[2]}
        i += 1

    return json.dumps(toret)

_cmds = {
    'send': sendmessage,
    'get' : getmessages,
    'kill': killprogram
}

_javacmds = []
def hearjava():
    global _javacmds
    rlist, _, _ = select([sys.stdin], [], [], _timeout)
    if rlist:
        l = sys.stdin.readline().strip(' \n')
        _javacmds.append(l)

def checkcmd(cmd):

    try:
        cmd = json.loads(cmd)
        action = cmd['action']

        if action == 'send':
            cmd['to']
            cmd['message']
        else:
            if action not in ['get', 'kill']:
                return None
    except ValueError:
        return None

    return cmd

def write(m):
    sys.stdout.write(m + '\n')
    sys.stdout.flush()

def talkjava():

    if _javacmds:

        cmd = checkcmd(_javacmds.pop())

        if cmd:
            f = _cmds.get(cmd['action'])
            write(f(**cmd))
        else:
            write('{"ERROR": "BAD REQUEST"}')

def loop():

    restore()

    while 1:

        try:
            hearjava()
            if not _plisten:
                listen()
            time.sleep(1)

            try:
                _messages.append(_queue.get(timeout=_timeout))
            except Empty:
                pass

            talkjava()

        except KeyboardInterrupt:
            kill_lprocess()
            save()
            break
        except SystemExit:
            kill_lprocess()
            save()
            break

if __name__ == "__main__":
    loop()
