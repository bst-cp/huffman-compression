#!/usr/bin/python3

from heapq import heappush, heappop, heapify
from collections import defaultdict
from sys import getsizeof
import requests
import json
from ast import literal_eval

def codebookImport(fileName):
    f = open(fileName, 'r')
    for line in f:
        char, val = line.split(' ', 1)
        print ("char %s val %s" % (char,val))

def encode(frequencyDict):
    heap = [[wt, [sym, ""]] for sym, wt in frequencyDict.items()]
    heapify(heap)
    while len(heap) > 1:
        lo = heappop(heap)
        hi = heappop(heap)
        for pair in lo[1:]:
            pair[1] = '0' + pair[1]
        for pair in hi[1:]:
            pair[1] = '1' + pair[1]
        heappush(heap, [lo[0] + hi[0]] + lo[1:] + hi[1:])
    return sorted(heappop(heap)[1:], key=lambda p: (len(p[-1]), p))


def decode(stringOfBytes, codebook):
    stringOfBytes = stringOfBytes[1:] #remove the first character ( it is a parity: '1')
    bgn = 0
    end = 1
    resultStr = ""
    if codebook != "nocodebook":
        huffmanTree = codebook
    while bgn < (len(stringOfBytes))-1:
        for p in huffmanTree:
            if stringOfBytes[bgn:end] == p[1]:
                resultStr += p[0]
                bgn = end
                if end < len(stringOfBytes) : end+=1
                break
        if end < len(stringOfBytes) : end+=1
    return resultStr

def compress(messageString):
    global huffmanTree
    stringOfBytes = "1" #initial 1 is a pattern like parity bit, it saves the 0's coming after it
    frequencyDict = defaultdict(int)
    for ch in messageString:
        frequencyDict[ch] += 1
    # in Python 3.1+:
    #frequencyDict = collections.Counter(messageString)

    huffmanTree = encode(frequencyDict)

    print (huffmanTree)

    codebook = ""

    for p in huffmanTree:
        codebook += (p[0] + ' ' + p[1] + '..')

    print ("Symbol\tWeight\tHuffman Code")
    for p in huffmanTree:
        print ("%s\t%s\t%s" % (p[0], frequencyDict[p[0]], p[1]))

    for ch in messageString:
        for p in huffmanTree:
            if ch == p[0]: #character match in the tree
                stringOfBytes += p[1]
            #else: print "No character match on codebook, for \'%c\'" % ch

    print ("Binary representation of message is %s" % stringOfBytes)

    return codebook, hex(int(stringOfBytes, 2)) #Base two representation of binary-looking string's integer form

def decompress(bytesOfMessage, codebook):
    stringOfBytes = bytesOfMessage
    resultStr = decode(stringOfBytes, codebook)
    print ("Your message is : %s" % resultStr)
    return resultStr

def login(ip_port,username):
    address = 'http://'+ip_port
    response = requests.post(address, data={'username':username})
    print ("Logged in as \"" + username + "\"")
    print ("Logged in users are: ", str(response.content)[2:-3])

def check_message(ip_port, username):
    address = 'http://'+ip_port
    response = requests.post(address, data={'checkmessage':'', 'receivername':username})
    message_str = str(response.content)[2:-1]
    message_str = message_str.replace('\\n', '')
    message_dict = literal_eval(message_str)

    if "nomessage" in message_dict:
        message = "No message"

    else:
        sender = message_dict["sender"]
        codebook = message_dict["codebook"]
        message = message_dict["message"]

        heap2 = list(set(codebook.split("..")))
        heap = list()
        for a in heap2:
            print (a)
            sym = a[:1]
            code = a[2:]
            heap.append([sym, code])
        heap.remove(['',''])

        decompressed = decompress(message, heap)
        message = decompressed

    print ("From: ", sender )
    print (message)

def send_message(ip_port, username):
    address = 'http://'+ip_port
    print("Send message to: ", end = '')
    receiver = input()
    print("Message: ", end = '')
    message = input()
    codebook, compressed_hex = compress(message)
    compressed = str(compressed_hex)[2:]
    print("Codebook: ", codebook)
    print("Compressed message: ", compressed)

    response = requests.post(address, data={'sender':username, 'receiver':receiver, 'codebook':codebook, 'message':compressed})
    print(str(response.content)[2:-1])


print("Username: ", end='')
username = input()
print("IP of the server: ", end='')
ip = input()
ip_port = ip+":8080"
login(ip_port,username)
print("Available commands are: check, send, exit")
while True:
    print(">>>", end=' ')
    cmd = input()
    if cmd == "check":
        check_message(ip_port, username)
    elif cmd == "send":
        send_message(ip_port, username)
    else:
        print("ERROR")

