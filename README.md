# Simple_Sample_gRPC

## Implement gRPC Connection: Android as Client |  Python as Server

---

## Demo

---

[]()

## Quick Start

---

1. Clone Project
    
    ```bash
    $ git clone https://github.com/liuyuweitarek/Simple_Sample_gRPC.git
    ```
    
2. Start Python gRPC Server
    
    ```bash
    $ cd Simple_Sample_gRPC/Python_gRPC_Server
    $ python run.py
    ```
    
3. Install Android App
    
    If you use Android emulator and set up python server on your localhost, please input 10.0.2.2  in App UI.
    

## Steps by Steps

---

Follow the steps on the gRPC documents. ([https://grpc.io/docs/languages/python/quickstart/](https://grpc.io/docs/languages/python/quickstart/)) and record some details here. 

1. Define your proto file and place it under your android project folder.
    
    e.g.  
    
    ```bash
    $ mkdir ./Simple_gRPC/Android_gRPC_Client/src/main/proto
    $ vim interaction.proto
    ```
    
    interaction.proto
    
    ```protobuf
    /**
     * Send Message between  Android APP Client and Python Server.
     */
    
    syntax = "proto3";
    
     option java_multiple_files = true;
     option java_package = "com.interaction.robot.interaction";
     option java_outer_classname = "InteractProto";
     option objc_class_prefix = "ST";
    
     package interaction;
    
     // The interact service definition.
     service Interact {
       // Sends connection confirm
       rpc SimpleConnect (ConnectRequest) returns (ConnectReply) {}
    
       // Sends information
       rpc SimpleSend (Input) returns (Output) {}
    
     }
    
     // The connect request message contains the status
     message ConnectRequest {
       string status = 1;
     }
    
     // The connect response message contains the status
     message ConnectReply {
       string status = 1;
     }
    
     // The input message contains the utterance
     message Input {
       string utterance = 1;
     }
    
     // The output info message contains the utterance
     message Output {
       string utterance = 1;
     }
    ```
    
2. Using python module "grpcio" and "grpcio-tools" to generate your python server configuration and protocol objects.
    
    ```bash
    $ pip install grpcio grpcio-tools
    ```
    
    ```bash
    python -m grpc_tools.protoc --proto_path={.proto_filepath} {.proto filename} --python_out={generated_python_filepath} --grpc_python_out={generated_python_grpc__filepath}
    
    # (e.g.)
    # $ cd to the .proto filepath
    # $ python -m grpc_tools.protoc --proto_path=. ./interaction.proto --python_out=../../../../Python_gPRC_Server/. --grpc_python_out=../../../../Python_gPRC_Server/.
    ```
    
3.  How to use Python Server
    
    In last step, wo generated two files, *_pb2.py and *_pb2_grpc.py, which contained objects of python gRPC protocol and settings. In the purpose of using them in a clean way, we'll define a new server object and set up a server folder.
    
    為了讓連線的Channel更乾淨，以及能擴充各種API，設計下面的目錄：
    
    - Python_gRPC_Server
        - interaction_pb2.py
        - interaction_pb2_grpc.py
        - InteracterServer.py
            
            Define Server which maintains the connection settings and API usage.
            
            ```python
            """
            File: InteracterServer.py
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
            		
            		def Other_Client_API(self, value_to_client=value):
            				output = {'intent': 'client_function', 'value': str(value)}
                    self.client_command = json.dumps(output)
                
                    while self.client_command != None:
                        time.sleep(0.1)
            
                    return self.client_response
            ```
            
        - run.py
            
            Here we could use the APIs and add other services
            
            ```python
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
            				self.server.other_client_function(value="haha")
                    input("Success Start...")
            
            if __name__ == "__main__":
                parser = argparse.ArgumentParser()
                args = parser.parse_args()
                mainLoop = MainLoop(args)
                mainLoop.start()
            ```
            
4.  Set Android Java Client
    
    Please notify TAG "// <!Edit Here>--/>" in the files below.
    
    - Import gRPC to Global Gradle
        
        ./Simple_gRPC/Android_gRPC_Client/build.gradle：
        
        ```groovy
        // Top-level build file where you can add configuration options common to all sub-projects/modules.
        
        buildscript {
            
            repositories {
                google()
                jcenter()
            }
            dependencies {
                classpath 'com.android.tools.build:gradle:3.2.1'
        				
        				//<!Add Protobuf Classpath>
                classpath 'com.google.protobuf:protobuf-gradle-plugin:0.8.8'  
        				//--/>
                
        				// NOTE: Do not place your application dependencies here; they belong
                // in the individual module build.gradle files
            }
        }
        
        allprojects {
            repositories {
                google()
                jcenter()
            }
        }
        
        task clean(type: Delete) {
            delete rootProject.buildDir
        }
        ```
        
    - Import gRPC to APP Gradle
        1.  The version of gRPC in this sample is 1.4.0。
        2. ./Simple_gRPC/build.gradle :
        
        ```groovy
        apply plugin: 'com.android.application'
        apply plugin: 'com.google.protobuf'  //<Edit Here>
        //<!Edit Here>
        ext {
            grpcVersion = '1.4.0'
        }
        //--/>
        
        android {
            compileSdkVersion 30
            buildToolsVersion "30.0.3"
            defaultConfig {
                applicationId "ntu.mil.simple_grpc"
                minSdkVersion 26
                targetSdkVersion 30
                versionCode 1
                versionName "1.0"
                testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
            }
            buildTypes {
                release {
                    minifyEnabled false
                    proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
                }
            }
        }
        
        //<!Edit Here>
        protobuf {
            protoc {
                artifact = 'com.google.protobuf:protoc:3.3.0'
            }
            plugins {
                javalite {
                    artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0"
                }
                grpc {
                    artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
                }
            }
            generateProtoTasks {
                all().each { task ->
                    task.plugins {
                        javalite {}
                        grpc {
                            option 'lite'
                        }
                    }
                }
            }
        }
        //--/>
        
        dependencies {
            implementation fileTree(dir: 'libs', include: ['*.jar'])
            implementation 'androidx.appcompat:appcompat:1.3.1'
            implementation 'androidx.constraintlayout:constraintlayout:2.1.1'
            testImplementation 'junit:junit:4.12'
            androidTestImplementation 'androidx.test.ext:junit:1.1.0'
            androidTestImplementation 'androidx.test.espresso:espresso-core:3.1.1'
        		
        		//<!Edit Here>
            // For Dealing with Command
            implementation 'com.google.code.gson:gson:2.8.5'
        
            //gRPC Client
            implementation "io.grpc:grpc-okhttp:${grpcVersion}"
            implementation "io.grpc:grpc-protobuf-lite:${grpcVersion}"
            implementation "io.grpc:grpc-stub:${grpcVersion}"
            implementation 'javax.annotation:javax.annotation-api:1.3.2'
            protobuf 'com.google.protobuf:protobuf-java:3.3.1'
        		//--/>
        ```
        
    - Add Network permission ./Simple_gRPC/Android_gRPC_Client/src/main/AndroidManifest.xml：
        
        ```xml
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            package="ntu.mil.simple_grpc">
        		
        		//<!Edit Here>
            <uses-permission android:name="android.permission.INTERNET" />
            //--/>
        		
        		<application
                android:allowBackup="true"
                android:icon="@mipmap/ic_launcher"
                android:label="@string/app_name"
                android:roundIcon="@mipmap/ic_launcher_round"
                android:supportsRtl="true"
                android:theme="@style/AppTheme">
                <activity android:name=".SimpleGrpcActivity">
                    <intent-filter>
                        <action android:name="android.intent.action.MAIN" />
                        <category android:name="android.intent.category.LAUNCHER" />
                    </intent-filter>
                </activity>
            </application>
        
        </manifest>
        ```
        
    - Setup UI
        
        ./Simple_gRPC/Android_gRPC_Client/src/main/res (could just watch the code)
        
    - Setup Android gRPC Client Connection
        
         ./src/main/java/SimpleGrpcActivity.java (could just watch the code)
        
        ```java
        public void connect(){
                mIp = ip_1.getText().toString() + "."
                        + ip_2.getText().toString() + "."
                        + ip_3.getText().toString() + "."
                        + ip_4.getText().toString();
                mPort = Integer.parseInt(host_port.getText().toString());
        
                Log.i(TAG,"Attempting to connect to server with ip " + mIp);
        
                try {
                    ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(mIp, mPort)
                            .usePlaintext(true)
                            .build();
                    mInteracter = InteractGrpc.newBlockingStub(managedChannel);
        
                    String jsonHandshake = new Gson().toJson(new ServerCommand("Android APP", mLocalIp.getText().toString()));
                    ConnectRequest connectRequest = ConnectRequest.newBuilder().setStatus(jsonHandshake).build();
                    ConnectReply connectReply = mInteracter.simpleConnect(connectRequest);
        
                    mConnectState = connectReply.getStatus();
                    mTextState.setText(mConnectState);
        
                } catch (Exception e) {
                    Log.e(TAG, "connect() Fail: " + e);
                    mTextState.setText("connect() Fail: " + e);
                }
            }
        ```
        
    - Setup Protocol setters and getters
        
          ./src/main/java/ServerCommand.java (could just watch the code)
        
        ```java
        public class ServerCommand {
            private String intent;
            private String value;
        
            public ServerCommand(String intent, String value){
                this.intent = intent;
                this.value = value;
            }
        
            public String getIntent() {
                return intent;
            }
        
            public void setIntent(String intent) {
                this.intent = intent;
            }
        
            public String getValue() {
                return value;
            }
        
            public void setValue(String value) {
                this.value = value;
            }
        
            @Override
            public String toString() {
                return "ServerCommand{" +
                        "intent='" + intent + '\'' +
                        ", value='" + value + '\'' +
                        '}';
            }
        }
        ```