#!/usr/bin/env python3

from http.server import HTTPServer, BaseHTTPRequestHandler
from optparse import OptionParser
from socketserver import ThreadingMixIn
from cgi import parse_header, parse_multipart, parse_qs
import threading
import json

userdb_dict = dict()
userlist = ""
messagedb = []

def do_LOGIN(username, ip):
    global userdb_dict
    global userlist
    if username not in userdb_dict:
        userdb_dict[username] = ip
        userlist += username
        userlist += " "
    print("userlist: ", userlist)
    return userlist

def do_send_message(sender, receiver, codebook, message):
    global messagedb
    messagedb.append([sender,receiver,codebook,message])
    print ("sender: " + sender + " receiver: " + receiver
            + " codebook: " + str(codebook) + " message: " + str(message))
    retval = json.dumps({'sent':'true'}, indent=4)
    return retval

def do_receive_message(receivername):
    global messagedb
    json_message = json.dumps({'nomessage':'nomessage' }, indent=4)
    for i in range(0, len(messagedb)):
        if messagedb[i][1] == receivername:
            json_message = json.dumps({'sender': messagedb[i][0], 'receiver': receivername, 'codebook':str(messagedb[i][2]), 'message':str(messagedb[i][3]) }, indent=4)
            print(json_message)
            return json_message
    return json_message

def parse_FORM(form, ip):

    print(form)
    if "username" in form:
        username = form["username"]
        print("Username: " + username + " IP: " + ip)
        retval = do_LOGIN(username, ip)

    elif "checkmessage" in form:
        if "receivername" in form:
            receivername = form["receivername"]
            retval = do_receive_message(receivername)


    elif "sender" in form:
        sender = form["sender"]

        if "receiver" in form:
            receiver = form["receiver"]
        if "codebook" in form:
            codebook = form["codebook"]
        if "message" in form:
            message = form["message"]

        retval = do_send_message(sender, receiver, codebook, message)

    else:
        print("ERROR: Wrong form parameter(s)")
        retval = "ERROR: Wrong form parameter(s)"
    return retval



class RequestHandler(BaseHTTPRequestHandler):

    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_HEAD(self):
        self._set_headers()


    def do_GET(self):

        request_path = self.path

        print("\n----- Request Start ----->\n")
        print("Request path:", request_path)
        print("Request headers:", self.headers)
        print("<----- Request End -----\n")

        self.send_response(200)
        self.send_header("Set-Cookie", "foo=bar")
        self.end_headers()

    def parse_POST(self):
        ctype, pdict = parse_header(self.headers['content-type'])
        #pdict['boundary'] = bytes(pdict['boundary'], "utf-8")
        if ctype == 'multipart/form-data':
            postvars = parse_multipart(self.rfile, pdict)
        elif ctype == 'application/x-www-form-urlencoded':
            length = int(self.headers['content-length'])
            postvars = parse_qs(
                    self.rfile.read(length),
                    keep_blank_values=1)
        else:
            postvars = {}

        newdict = {}
        print(postvars)
        for key, value in postvars.items():
            newkey = str(key)[2:-1]
            newval = str(value)[3:-2]
            newdict[newkey] = newval

        return newdict


    def do_POST(self):
        global userlist
        form = self.parse_POST()
        request_path = self.path

        print("\n----- Request Start ----->\n")
        print("Request path:", request_path)

        request_headers = self.headers
        content_length = request_headers.get('Content-Length')
        length = int(content_length) if content_length else 0

        print("Content Length:", length)
        print("Request headers:", request_headers)

       # payload_raw = self.rfile.read(length)
       # payload = str(payload_raw)[2:-1]

       # print("PAYLOAD: ", payload)
        print("<----- Request End -----\n")
        ip = self.client_address[0]
        print ("IP address and port number is : ", ip)

        self.send_response(200)
        self.end_headers()

        retval = parse_FORM(form, ip)

        self.wfile.write(bytes(retval, "utf-8"))
        self.wfile.write(bytes('\n', "utf-8"))

    do_PUT = do_POST
    do_DELETE = do_GET

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    pass
    """Handle requests in a separate thread."""

def main():
    print("IP: ", end='')
    ip = input()
    port = 8080
    server = ThreadedHTTPServer((ip, port), RequestHandler)
    print('Listening on %s:%s' % (ip,port))
    server.serve_forever()


if __name__ == "__main__":
    parser = OptionParser()
    parser.usage = ("Creates an http-server that will echo out any GET or POST parameters\n"
                    "Run:\n\n"
                    "   reflect")
    (options, args) = parser.parse_args()

    main()
