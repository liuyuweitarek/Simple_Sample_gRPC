"""
File: interacter.py
Author: Yuwei Liu
Institution: Modeling and Informatics Laboratory
Version: v0.1.0
"""

import os
import grpc
import time
from random import randrange
from concurrent import futures
import socket
import logging
import json
import interaction_pb2
import interaction_pb2_grpc 




class Server(interaction_pb2_grpc.InteractServicer):
    """ 
    This is the only class in the interacter package.
    It creates a gRPC server with the computer's local ip address with a specified port.
    """

    logging.basicConfig(level=os.environ.get("LOGLEVEL","INFO"))
    log = logging.getLogger(__name__)
    
    is_connected = False
    client_type = ''
    client_ip = ''
    client_command = None
    client_response = ''

    ERR_EMPTY_ARGS = 'Cannot have empty args!'

    def __init__(self, port):
        """ 
        The constructor for Server class. 
  
        Arguments: 
           port (int): The port where the server will be listening for incoming client connections. 
        """

        self.server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
        interaction_pb2_grpc.add_InteractServicer_to_server(self, self.server)
        self.server.add_insecure_port('[::]:' + str(port))
        self.log.info('gRPC server started on address '+ self.get_ip() + ':' + str(port))
        self.server.start()
        

    def SimpleConnect(self, request, context):
        """ 
        This method is called when a client connects to the server. 
        """
        self.is_connected = True
        handshake = json.loads(request.status)
        self.client_type = handshake["intent"]
        self.client_ip = handshake["value"]
        
        return interaction_pb2.ConnectReply(status="I got it. --from Python Server")

    def SimpleSend(self, request, context):
        """ 
        This method is called every time a message is received from the client. 
        If the robot returns an error message, it will be printed in the log.
        """
        
        json_response = json.loads(request.utterance)

        if 'value' in json_response:
            self.client_response = json_response['value']
        else:
            self.client_response = None

        if json_response['intent'] == 'error':
            self.log.warning(self.client_response)

        self.client_command = None
        while self.client_command == None:
            time.sleep(.2)

        return interaction_pb2.Output(utterance=self.client_command)
    
    def get_ip(self):
        """
        This method returns the current ip address of the computer.
        """
        
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            s.connect(('10.255.255.255', 1))
            IP = s.getsockname()[0]
        except:
            IP = '127.0.0.1'
        finally:
            s.close()
        return IP        