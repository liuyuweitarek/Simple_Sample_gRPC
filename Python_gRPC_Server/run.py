"""
File: interacter.py
Author: Yuwei Liu
Institution: Modeling and Informatics Laboratory
Version: v0.1.0
"""

from InteracterServer import Server
import time
import os
import sys
import argparse
from threading import Thread
import subprocess

class MainLoop(object):
    def __init__(self, args):
        self.path = os.path.dirname(os.path.abspath(__file__))

        self.server = Server(50051)

        #Wait until the robot has connected
        while not self.server.is_connected:
            time.sleep(1)

        print('client connected: ' + self.server.client_type)

    def start(self):
        input("Success Start...")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    args = parser.parse_args()
    mainLoop = MainLoop(args)
    mainLoop.start()