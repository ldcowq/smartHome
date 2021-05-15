# smartHome
基于ZigBee+ESP32+MQTT+EMQX+TomCat+Servlet接口+MySQL+安卓app的物联网项目

## 一、写在前面
在放寒假前，自己也说过了，这个寒假一定好好复盘一下大三上学期的一个项目。

## 二、课设简介
我的课设想法：因为自己已经大三了，也不年轻了，技术也学得很广泛也很烂（物联网专业也算得上是一个万金油专业），综合自己学过的技术和对自己专业的认知，当时萌生了做一个很普通的适合物联网三层架构思想的一个小课设，不求有多高级多厉害多实用，但求技术稍微全面一点，所以我只用到了一些简单的传感器，比如烟雾、光照、温湿度、执行器使用led灯，自己打算从底层到传输层，传输层到应用层，一层一层地来实现，尽量不使用或者少使用第三方的技术。
![我的整个想法-系统框图](https://img-blog.csdnimg.cn/20210212180614658.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)

**课设名称：** 智能家居数据上传和远程控制系统
**花费时间：** 20天
**成本价格：** 180元

## 三、不眠夜开始了
### 1、基于zigbee网络数据采集的底层实现
因为课程要求需要用到zigbee模块和esp32模块，所以采集传感器的数据理所当然地由zigbee节点完成了。

**原理：** 如图所示，节点1负责采集烟雾和光强的数据，节点2负责采集温室度数据，同时这两个节点上自带了led灯，后面的开灯操作就是直接控制板子上的led灯。两个节点采集到的数据将通过zigbee网络发送给协调器（zigbee网络的构建者和管理者），协调器收到两个子节点发来的数据，然后通过串口（杜邦线连接）把数据发送给ESP32网关。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210212173620282.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)
关于zigbee的学习，可以用**挣扎**两字来形容，大家都学得很吃力，甚至大多数人包括我都是一知半解，直接看函数的解释来使用的，毕竟能力就到这了。老师规定使用zigbee要使用SimpleApi框架，能看懂函数，就应该都会用！

**节点1读取烟雾浓度和光强数据的部分代码：**

```c
//读取adc的值，参数是通道数
unsigned int readAdc(unsigned char channal);
unsigned int readAdc(unsigned char channal)
{
  unsigned int value ; 
  APCFG |= 1 << channal ; 
  ADCIF = 0 ;
  ADCCON3 = channal;          
  while ( !ADCIF ) ; 
  value = ADCL;
  value |= ((unsigned int) ADCH) << 8 ;
  value>>=2;
  return value; 
}

//上电执行，构建网络使用，加入网络成功时status==ZSUCCESS。
void zb_StartConfirm( uint8 status )
{
  if(status==ZSUCCESS)
  {    
    //可把节点所包含的led灯的ID号发送过去
    zb_SendDataRequest(0X0,LEDJOINNET_CMD_ID,LEDNUM,ledIdList,0,FALSE,AF_DEFAULT_RADIUS);
    osal_start_timerEx(sapi_TaskID,TIMER_TIMEOUT_EVT,2000); //每两秒给协调器发送一个心跳包
    osal_start_timerEx(sapi_TaskID,READ_MQ2_EVENT,5000);//每5秒采集一次烟雾浓度数据
    osal_start_timerEx(sapi_TaskID,READ_LIGHT_EVENT,5000);//每5秒采集一次光照强度数据
  }
}
//事件处理函数
void zb_HandleOsalEvent( uint16 event )
{
  if(event&READ_MQ2_EVENT){//读取烟雾浓度数据的事件
    osal_start_timerEx(sapi_TaskID,READ_MQ2_EVENT,5000);//定时下一次采集烟雾浓度的事件，也是5秒
    unsigned char mq2value[4];
    unsigned int AdcValue=0;
    AdcValue=readAdc(5);//采集第5通道的adc值
    sprintf(mq2value,"%d\r\n", AdcValue);//把数值放到char类型的数组
    //把读取到的烟雾浓度值通过zigbee网络发给协调器，协调器默认的网络地址是0
    zb_SendDataRequest(0X0,MQ2_CMD_ID,osal_strlen(mq2value),mq2value,0,FALSE,AF_DEFAULT_RADIUS); 
  }
}
```

**节点2读取温湿度数据部分代码：**

```c
void Read_Byte(void);//从DHT11读取一个字节函数   
void  Read_Byte(void)
{
  uchar i;
  for (i = 0; i < 8; i++)  //循环8次，读取8bit的数据
  {
    Overtime_counter = 2;  //读取并等待DHT11发出的12-14us低电平开始信号
    P0DIR &= ~0x10;
    while ((!DHT11_DATA) && Overtime_counter++);
    //Delay_10us(80); //26-28us的低电平判断门限
    MicroWait(27);
    bit_value = 0;  //跳过门限后判断总线是高还是低，高为1，低为0
    if(DHT11_DATA)
    bit_value = 1;
    Overtime_counter=2;  //等待1bit的电平信号结束，不管是0是1在118us后都变为低电平，否则错误超时
    while (DHT11_DATA && Overtime_counter++);  //当U8FLAG加到255后溢出为0，跳出循环，并后加加为1
    if (Overtime_counter == 1)
      break;   //超时则跳出for循环        
    comdata <<= 1;   //左移1位，LSB补0
    comdata |= bit_value;//LSB赋值
  }
}
void zb_StartConfirm( uint8 status )
{
  if(status==ZSUCCESS)
  {   
    //可把节点所包含的led灯的ID号发送过去
    zb_SendDataRequest(0X0,LEDJOINNET_CMD_ID,LEDNUM,ledIdList,0,FALSE,AF_DEFAULT_RADIUS);
    osal_start_timerEx(sapi_TaskID,TIMER_TIMEOUT_EVT,2000);   
    osal_start_timerEx(sapi_TaskID,READ_DHT11_EVENT,5000);
  }
}
//事件处理函数
void zb_HandleOsalEvent( uint16 event )
{
  if(event&READ_DHT11_EVENT){//读取温湿度数据的事件
    unsigned  char temphumi[2];
    Read_DHT11(temphumi,temphumi+1);//参数是一级指针变量，数组名也是首地址，可以传！
    if(temphumi!=NULL)//上一行执行完，temphumi里保存的就是温湿度的值
    {
      osal_start_timerEx(sapi_TaskID,READ_DHT11_EVENT,5000);//定时下一个读取温湿度数据的事件
      //把温湿度数据发送给协调器
      zb_SendDataRequest(0X0,TEMP_HUMI_CMD_ID,osal_strlen(temphumi),temphumi,0,FALSE,AF_DEFAULT_RADIUS); 
     }
   }
}
```
看上面的两段代码会发现没有任何关于灯或风扇的代码，是因为当时把整个系统调通之后，第二天就要答辩了，来不及了，所以就直接控制了协调器板子上的led灯。

节点1和节点2采集的数据发送给协调器后，协调器需要接收数据，还可以对数据进行一定的格式处理，再通过串口发送给ESP32网关。

**协调器的代码：**

```c
//当构建网络或加入网络成功时被调用
void zb_StartConfirm( uint8 status )
{
  halUARTCfg_t uartcfg;
  uartcfg.baudRate=HAL_UART_BR_115200;
  uartcfg.flowControl=FALSE;
  uartcfg.callBackFunc=uart_receive;
  HalUARTOpen(HAL_UART_PORT_1,&uartcfg);
  HalLedSet(HAL_LED_1, HAL_LED_MODE_OFF);//初始化关闭led
  HalLedSet(HAL_LED_2, HAL_LED_MODE_OFF);//初始化关闭led
  if(status==ZSUCCESS)
  {
    char buf[]="Coordinator is created successfully!\r\n";//协调器构建网络成功
    HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));
    osal_start_timerEx(sapi_TaskID,TIMER_TIMEOUT_EVT,2000);//定时心跳包事件
  }  
}

//执行时机：接收到数据包时被调用
void zb_ReceiveDataIndication( uint16 source, uint16 command, uint16 len, uint8 *pData  ){
  .....省略部分代码
  }else if(command==TEMP_HUMI_CMD_ID){//收到节点2发来的温湿度数据包
	sprintf(buf,"{\"t\":\"th\",\"temp\":\"%d\",\"humi\":\"%d\"}",pData[0],pData[1]);//把数据整合成json格式
	HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));//通过串口发送给ESP32网关
	HalUARTWrite(HAL_UART_PORT_1,"\r\n",2);
  }else if(command==MQ2_CMD_ID){//收到节点1发来的烟雾浓度数据包
    sprintf(buf,"{\"t\":\"mq2\",\"mq2\":\"%c%c%c%c\"}",pData[0],pData[1],pData[2],pData[3]);//整合成json格式
    HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));//通过串口发送给ESP32网关
    HalUARTWrite(HAL_UART_PORT_1,"\r\n",2);
  }else if(command==LIGHT_CMD_ID){//收到节点1发来的光强数据包
    sprintf(buf,"{\"t\":\"light\",\"light\":\"%c%c%c%c\"}",pData[0],pData[1],pData[2],pData[3]);
    HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));//通过串口发送给ESP32网关
    HalUARTWrite(HAL_UART_PORT_1,"\r\n",2);
  }
}

//协调器串口收到ESP32网关发来的数据时，会执行该函数。
void uart_receive(uint8 port,uint8 event){
  uint16 dstAddr;
  if(event& (HAL_UART_RX_FULL|HAL_UART_RX_ABOUT_FULL|HAL_UART_RX_TIMEOUT))
  {
    //这里不是很严谨，不能很好地分出一个逻辑数据包
    uint8 buf[5];
    HalUARTRead(port,buf,2);//第一个字节是灯的开或关状态，第二个字节是那一盏灯，即灯的编号！
    if(buf[1]=='1'){
      if(buf[0]=='o'){
      HalLedSet(HAL_LED_1,HAL_LED_MODE_ON);
      }
      if(buf[0]=='c'){
      HalLedSet(HAL_LED_1,HAL_LED_MODE_OFF);
      }  
    }
    if(buf[1]=='2'){
      if(buf[0]=='o'){
      HalLedSet(HAL_LED_2,HAL_LED_MODE_ON);
      }
      if(buf[0]=='c'){
      HalLedSet(HAL_LED_2,HAL_LED_MODE_OFF);
      }  
    }     
  }  
}
```
小结：这个课设一开始做的就是zigbee底层这部分，因为学得真的很烂，花了足足三天的时间去折腾（熬了三天的夜）

### 2、基于ESP32和mqtt协议的数据上传功能
到了ESP32开发就好玩多了，代码没那么多，也没那么难懂，虽然我没学过python，在看micropython的代码时，也感觉比看zigbee的代码舒服多了。

现在，zigbee协调器已经可以把数据通过串口发送到了ESP32的串口，ESP32可以直接读取串口的数据，接着把数据再进一步传递出去，看一下数据上传的系统框图吧！

![数据上传-系统框图](https://img-blog.csdnimg.cn/20210212204010138.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70#pic_center)

ESP32模块连接手机热点或者家里wifi，就可以把数据送到互联网，这是物联网中很重要的一部分，如果不联网，再好的功能也只是局限在本地进行。而关于协议的选择，我选择了MQTT协议，这是一个基于发布和订阅模型的协议，在物联网中应用非常广泛（协议的具体信息可以百度查找）

**原理：** ESP32网关连接上手机热点或者wifi，创建MQTT实例并连接MQTT服务器，设置订阅的回调函数，当串口缓冲区有数据来时，调用 c.publish(TOPIC,publishMessage)函数，把数据推送到云端的MQTT服务器，在服务器的后台是可以查看到该主题的消息的。

**ESP32网关代码：**

```python
u = UART(2, baudrate=115200, bits=8, parity=0, rx=22, tx=23, timeout=10)#创建串口对象

SERVER = "你的服务器ip地址或者域名"
CLIENT_ID = "客户端id"
TOPIC = b"smartHome/ldc"
username='ldc'
password='ldc'
SSID="ldc"
PASSWORD="ldc"
fan=Pin(0,Pin.OUT,value=0)#驱动风扇的管脚
led=Pin(2, Pin.OUT, value=0)#驱动led灯的管脚
wlan=None
c=None
publishMessage=None

def connectWifi(ssid,passwd):#连接wifi函数
  global wlan
  wlan=network.WLAN(network.STA_IF) 
  wlan.active(True)                         
  wlan.disconnect()
  wlan.connect(ssid,passwd)
  while(wlan.ifconfig()[0]=='0.0.0.0'):#等待wifi连接
    time.sleep(1)

#当ESP32网关收到订阅的主题发来消息时
def sub_cb(topic, msg):
  parsed = ujson.loads(msg)
  msg_type = parsed["m"]
  statue = parsed["statue"]
  if msg_type =="1":
    led.value(1)
  if msg_type =="2":
    led.value(0)
  if msg_type =="3":
    fan.value(1)
  if msg_type =="4":
    fan.value(0)
  if msg_type =="5":
    u.write(b'o1')
  if msg_type =="6":
    u.write(b'c1')
  if msg_type =="7":
    u.write(b'o2')
  if msg_type =="8":
    u.write(b'c2')
    
try:         
	connectWifi(SSID,PASSWORD)#让ESP32网关连接wifi
	server=SERVER
	c = MQTTClient(CLIENT_ID, server,0,username,password)#创建一个MQTT客户端实例
	c.set_callback(sub_cb) #设置回调函数
	c.connect() #连接MQTT服务器
	c.subscribe(TOPIC) #订阅主题
	while True: 
		if(u.any()):#判断串口缓冲区时候有内容，返回缓冲区大小的字节数
	      publishMessage = u.readline()#读取一行数据
	      c.publish(TOPIC,publishMessage) #把数据发送到MQTT服务器的TOPIC这个主题中，如果其他客户端也订阅这个主题，将会收到这个消息！
		c.check_msg()#判断是否有该主题的消息
		time.sleep(1)
finally:
	if (c is not None):
		c.disconnect()
	wlan.disconnect()
	wlan.active(False)
```
### 3、使用EMQX作为MQTT服务器软件
关于搭建MQTT服务器据我了解有EMQX和mosquito两个服务器软件，网上也有很多关于这两个服务器软件的安装教程或者优缺点评价的帖子，有兴趣的可以去了解。我个人选用了EMQX来搭建MQTT服务器，搭建完成后，它有一个可视化的后台管理程序，可以非常直观地看到订阅或者发布的消息！如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210213230217307.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)现在ESP32网关已经能把数据发送给云端的MQTT服务器，但是服务器并没有把数据保存起来，只能在后台管理中能查看到某个主题的某个信息。问题就来了，把数据保存到数据库不就行了吗？是的，不过因为我安装的EMQX是免费的开源版本，没有自带持久化数据到MySQL等数据库的插件，那是企业版才有的功能（对于学生来说，太贵了），我的课设想法当时差点就被扼杀了。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210221032555567.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)
好在我坚持查找资料、爬帖子，又看到了希望的曙光。方法是这样的：EMQX是支持HTTP协议转发的，当MQTT服务器收到客户端的消息后，可以根据你设定的规则进行消息的过滤，然后根据你设置的请求URL把符合规则的消息转发到该URL。说实话，看到这里，凭我当时的知识储备我是一脸懵的，我不知道它转发到我指定的URL后怎么获取数据，还有就是我怎么搞一个URL？
### 4、使用Java编写Servlet接口程序（制作URL）
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210213232044439.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)
我继续坚持查找资料、爬帖子、看B站！two thousand years later，又看到了希望的曙光。是这样的：这个所谓的URL，也可以叫做接口，需要我写一个Servlet接口程序，来处理EMQX服务器发来的请求，至于是POST请求还是GET请求，在创建接口资源的时候可以自行选择。

在这里可以明白，所谓的支持HTTP转发，就是它可以拿着你想要的数据，以参数的形式通过HTTP协议向你的服务器发起请求，而此时你的服务器就必须要有一个Servlet后端程序来处理它的请求，从中获取到参数的值，也就是传感器的数据，然后保存到MySql数据库中，说白了这个接口的工作就是实现数据库的增删改查！

好巧不巧的是，疫情期间上网课的时候，在家里强迫自己把上硅谷的Java Web课程粗略地看了一遍（因为当时心里有想过，以后干脆就走后端方向好了，在我目前的情况来看，当时这种想法是不可取的，因为你干不过web的，物联网就应该有物联网的优势），当时也只是看教程，很少动手敲代码，更别说做一个小项目来验证自己学过的知识，所以Java Web的知识我也只是停留在理解阶段，就是这段奇葩的学习经历，再综合网上的资料，才有了上面的思路，我的课设想法再次经受住了考验！
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210214000849898.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)
我又立刻复习了一下当时的Java Web教程，尝试开始敲一个Servlet接口程序，足足花了一个星期（包括熬夜），才把这个接口给写完了，期间遇到无数bug。

印象最深刻的一次是，我在接口资源那里选择的是POST请求，然后一股脑地在doGet()方法里获取参数值，时间就是被这种小细节给浪费了。查出问题后，转向doPost()方法，我把doGet()方法里获取参数的代码复制到doPost()方法，居然报错了。经过一番折腾，发现POST请求的参数在请求体里面获取，而不像GET请求是直接获取链接参数的.

这就是学完一门技术后，没有做项目去巩固知识的后果，理解终究只是理解而已，血的教训，不要学我哈！
![在这里插入图片描述](https://img-blog.csdnimg.cn/2021021400211023.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)
**烟雾浓度数据保存到MySql数据库的部分代码：**

```java
/**
 * @author LDC
 * @create 2020-12-21 21:14
 */
public class Mq2Servlet extends HttpServlet {
    private SaveDataDao dao = new SaveDataDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
    throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
    throws ServletException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String str = "";
        String wholeStr = "";
        while((str = reader.readLine()) != null){
            //一行一行的读取body体里面的内容；
            wholeStr += str;
        }
        JSONObject t= JSONObject.fromObject(wholeStr);
        String mq2 = (String) t.get("mq2");
        dao.addMq2(mq2);
    }
}
```
**从MySql数据库中查询烟雾浓度数据部分代码：**

```java
/**
 * @author LDC
 * @create 2020-12-24 23:22
 */
public class QueryMq2Servlet extends HttpServlet {
    private QueryDataDao dao = new QueryDataDao();
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
        String pagers = request.getParameter("pagers");
        List<Mq2> resultList = dao.queryMq2(pagers);
        System.out.println(resultList.toString());
        PrintWriter writer = response.getWriter();
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (Mq2 mq2 : resultList) {
            buffer.append("{\"mq2\":\"").append(mq2.getMq2()).append("\"},");
        }
        //去掉最后一个逗号
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append("]");
        writer.println(buffer);
    }
}
```
写完整个接口程序时，整个人兴奋的不得了，因为最难的那一道坎过去了，不过答辩的时间也越来越近了，丝毫不敢放松，当时除了有CBA广东队的球赛会看一场放松一下，其余的看综艺、刷微博甚至连打球的时间都没了。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210214004523932.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)

### 5、在云端部署Servlet接口程序
由于上面返回的数据是在本地进行测试的，Servlet接口程序是部署在本地的Tomcat服务器，所以新的问题又来了，我需要把这个接口程序部署到云端的Tomcat服务器上.

因为之前有使用**宝塔面板**搭建过网站，所以Tomcat和MySql数据都已经有了，经过一顿熬夜操作，终于可以访问云端服务器ip来获取到传感器数据了。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210214014631103.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)

其实来到了这一步，传输层的工作已经完成了，同时也已经证明了我的想法是可以实现的，是ok的，那剩下的应用层只是时间和审美的问题了！

### 6、编写应用层——安卓app查看数据
这个学期刚好要学android，所以应用层就需要做成一个app的形式。安卓端如果需要查看传感器的数据，只要向我云端的服务器发起HTTP请求，就能获取到服务器返回来的json数组格式的传感器数据，然后再做一下json格式的处理，就能显示到手机上，如下图所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210214020733448.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70)

第一张图的实时数据是通过订阅ESP32网关设定的主题来获取的，是传感器上传的实时数据，而后两张图的数据就是历史数据，是通过发起HTTP协议请求访问Servlet接口程序，接口根据参数查询用户需要的数据，并下发给用户，数据就能显示到手机上了。

**手机端获取温湿度历史数据部分代码：**
```kotlin
private void refresh() {
    new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isAdded()) {//判断fragment是否已经添加到activity中
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpUtil.sendHttpRequest("http://ip+端口/smartHome/th?pagers=1", new Callback() {
                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) 
                            throws IOException {
                                String responseData = response.body().string();
                                System.out.println(responseData);
                                Gson gson = new Gson();
                                List<TempHumiJavaBean> tempHumiJavaBeans = 
                                gson.fromJson(responseData, new TypeToken<List<TempHumiJavaBean>>() {
                                }.getType());
                                for (TempHumiJavaBean t : tempHumiJavaBeans) {
                                    mTHdataList.add(new TempHumiJavaBean(t.getTemp(), t.getHumi()));
                                }
                                swipeRefresh.setRefreshing(false);
                            }

                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                e.printStackTrace();
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }).start();
}
```
### 7、app远程控制灯和风扇
原理：当跳转到远程控制页面时，自动订阅ESP32网关的主题，这个时候手机端和led灯或者风扇已经是通过互联网连接好了，当按下对应灯或者风扇的按钮，只需要发一个信号，可以是数字1、2、3、4，或者是字符串，ESP32就会接收到该消息，然后只需要判断一下你发过来的信息，确认你要触发的动作是什么，比如收到数字1时，控制某个灯是开还是关等等。

**远程控制的部分代码：**
```java
kitchen_light_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            while (!mqttClient.isConnected()) ;
            if (mqttClient != null && mqttClient.isConnected()) {
                try {
                    String payload = "1";
                    MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
                    mqttClient.publish(topic, mqttMessage);
                    kitchen_light_img.setImageResource(R.drawable.ketchen_light_open);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("消息推送失败！");
                kitchen_light_switch.setChecked(false);
                kitchen_light_img.setImageResource(R.drawable.kitchen_light_close);
            }
        } else {
            try {
                String payload = "2";
                MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
                mqttClient.publish(topic, mqttMessage);
                kitchen_light_img.setImageResource(R.drawable.kitchen_light_close);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
});
```
界面展示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021022104084417.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzYyNzAyMg==,size_16,color_FFFFFF,t_70#pic_center)

## 四、不眠夜结束了
以上就是我大三上学期的一个课设想法和实现过程啦，分享出来也不知道会不会有人看。anyway，就当作记录一下自己的大学生活吧。

现在已经2021年，对于物联网专业还是有点迷茫，真不知道往那个方向走，不管怎么样，往死里学就对了！切忌只学软件或者硬件的思想！

源码链接：https://github.com/ldcowq/smartHome

博客链接：https://blog.csdn.net/weixin_43627022/article/details/113795556
