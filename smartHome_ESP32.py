import machine #该模块中提供了reset方法重启板子
import time
import ujson
from machine import UART
import _thread#该模块中提供了start_new_thread创建线程
from umqtt.simple import MQTTClient
from machine import Pin
import network
import os
from machine import PWM
u = UART(2, baudrate=115200, bits=8, parity=0, rx=22, tx=23, timeout=10)

SERVER = "103.152.132.235"
CLIENT_ID = "1840707262"
TOPIC = b"smartHome/ldc"
username='ldc'
password='ldc'
SSID="ldc"
PASSWORD="22222222"
fan=Pin(0,Pin.OUT,value=0)
led=Pin(2, Pin.OUT, value=0)
state = 0
wlan=None
c=None
publishMessage=None



def sub_cb(topic, msg):
  global state
  print((topic, msg))
  parsed = ujson.loads(msg)
  print(type(parsed))
  
  msg_type = parsed["m"]
  statue = parsed["statue"]
  print(msg_type)
  print(statue)
  if msg_type =="kitlight_open":
    led.value(1)
  if msg_type =="kitlight_close":
    led.value(0)
  if msg_type =="living_fan_open":
    fan.value(1)
  if msg_type =="living_fan_close":
    fan.value(0)
  if msg_type =="living_light_open":
    u.write(b'o1')
  if msg_type =="living_light_close":
    u.write(b'c1')
  if msg_type =="bedroom_light_open":
    u.write(b'o2')
  if msg_type =="bedroom_light_close":
    u.write(b'c2')
  

def connectWifi(ssid,passwd):
  global wlan
  wlan=network.WLAN(network.STA_IF)         #create a wlan object
  wlan.active(True)                         #Activate the network interface
  wlan.disconnect()                         #Disconnect the last connected WiFi
  wlan.connect(ssid,passwd)                 #connect wifi
  while(wlan.ifconfig()[0]=='0.0.0.0'):
    time.sleep(1)


def threadPublish():
  global publishMessage
  global c
  try:
    while True:
      if(u.any()):
        publishMessage = u.readline()
        print(publishMessage)
        c.publish(TOPIC,publishMessage)        
        time.sleep(4)
      else:
        print('no message')
        time.sleep(2)
      
  except:#当捕获到异常时重启板子
    print('推送消息异常')
    time.sleep(5)#出现异常时通过延时让板子不要频繁启动
    machine.reset()  

def receiveMessage():
  global c
  #try...except...捕获异常
  try:
    while True: 
	  #阻塞等待订阅消息
      c.wait_msg()
  except:
    #当出现异常时延迟2秒的目的是不要让板子频繁启动
    print('接收消息异常')
    time.sleep(5)
	#复位
    machine.reset()

try:
  connectWifi(SSID,PASSWORD)
  server=SERVER
  c = MQTTClient(CLIENT_ID, server,0,username,password)     #create a mqtt client
  c.set_callback(sub_cb)                    #set callback
  c.connect()                               #connect mqtt
  c.subscribe(TOPIC)                        #client subscribes to a topic
  print("Connected to %s, subscribed to %s topic" % (server, TOPIC))
  #_thread.start_new_thread(threadPublish,())  
  _thread.start_new_thread(receiveMessage,())
  
  
except:
  #开启一个新的线程用来负责接收订阅消息，这样主线程可以专心做其他的工作
  print('main() error')
  #_thread.start_new_thread(threadPublish,())
  _thread.start_new_thread(receiveMessage,())
