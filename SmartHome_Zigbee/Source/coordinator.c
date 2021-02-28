#include "smartHome.h"
#include "sapi.h"
#include "osal.h"
#include "hal_uart.h"
#include "hal_led.h"
#include "stdio.h"
#define NUM_IN_CMD_COORINATOR 2
#define NUM_OUT_CMD_COORINATOR 1
void *alloceLedDeviceNode(uint8 lednum);
void uart_receive(uint8 port,uint8 event);
const cId_t coordinatorInputCommandList[NUM_IN_CMD_COORINATOR]=
                                {LEDJOINNET_CMD_ID,HEART_BEAT_CMD_ID};
const cId_t coordinatorOutputCommandList[NUM_OUT_CMD_COORINATOR]=
                                {TOGGLE_LED_CMD_ID};
struct led_device_node
{
  struct led_device_node *next;
  uint8 shortAddr[2];
  uint8 lostHeartCount;
  uint8 ledNum;
  uint8 ledId[1];
};

static struct led_device_node ledDeviceHeader={NULL};

void *alloceLedDeviceNode(uint8 lednum)
{
  return osal_mem_alloc(sizeof(struct led_device_node)-1+lednum);
}
//为simple API定义简单描述符
const SimpleDescriptionFormat_t zb_SimpleDesc=
{
  ENDPOINT_ID_SMARTHOME,
  PROFILE_ID_SMARTHOME,
  DEVICE_ID_COORDINATOR,
  DEVIDE_VERSION_ID,
  0,
  NUM_IN_CMD_COORINATOR,
  (cId_t*)coordinatorInputCommandList,
  NUM_OUT_CMD_COORINATOR,
  (cId_t*)coordinatorOutputCommandList  
};



//当构建网络或加入网络成功时被调用
void zb_StartConfirm( uint8 status )
{
  halUARTCfg_t uartcfg;
  uartcfg.baudRate=HAL_UART_BR_115200;
  uartcfg.flowControl=FALSE;
  uartcfg.callBackFunc=uart_receive;
  HalUARTOpen(HAL_UART_PORT_1,&uartcfg);
  HalLedSet(HAL_LED_1, HAL_LED_MODE_OFF);
  HalLedSet(HAL_LED_2, HAL_LED_MODE_OFF);
  if(status==ZSUCCESS)
  {
    char buf[]="Coordinator is created successfully!\r\n";
    HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));
    osal_start_timerEx(sapi_TaskID,TIMER_TIMEOUT_EVT,2000);
  }  
}

//用来处理用户的自定义事件
void zb_HandleOsalEvent(uint16 event)
{
  if(event&TIMER_TIMEOUT_EVT){
    struct led_device_node *p=ledDeviceHeader.next;
        struct led_device_node *pre=ledDeviceHeader.next;   
        osal_start_timerEx(sapi_TaskID,TIMER_TIMEOUT_EVT,2000);
    while(p!=NULL){//判断节点是否存在
      p->lostHeartCount--;//心跳数减一
      char count[10];
      //sprintf(count,"lostHeartCount=%d\r\n",p->lostHeartCount);
      //HalUARTWrite(0,count,osal_strlen(count));
      if(p->lostHeartCount<=0){
         char buf[100];
         struct led_device_node *pTmp=p;
         pre->next=p->next;  
         p=p->next;
         sprintf(buf,"{\"offline\":\"%u\"}",(uint16)p->shortAddr);
         HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));
         HalUARTWrite(HAL_UART_PORT_1,"\r\n",2);
         continue;
      }
      pre=p;
      p=p->next;
    }        
  }
}


/***********
执行时机：发送的数据包被接收方收到时被调用
handle:包的编号；
status:ZSUCCESS表示成功接收
************/
void zb_SendDataConfirm( uint8 handle, uint8 status )
{
  
}

/***********
执行时机：接收到数据包时被调用
************/
void zb_ReceiveDataIndication( uint16 source, uint16 command, uint16 len, uint8 *pData  ){
  char buf[100];
  if(command==LEDJOINNET_CMD_ID){
    
    //int i;
    struct led_device_node *p=ledDeviceHeader.next;
      while(p!=NULL){
        if( osal_memcmp(pData,p->ledId,len)==TRUE)
          break;
        else
        {
          p=p->next;
        }
      }
      if(p==NULL){//新节点加入
        struct led_device_node *np=(struct led_device_node *)alloceLedDeviceNode(len);
        osal_memcpy(np->shortAddr,&source,2);
        np->ledNum=len;
        osal_memcpy(np->ledId,pData,len); 
        np->next=ledDeviceHeader.next;//头插
        ledDeviceHeader.next=np;
        p=np;
      }else{
        osal_memcpy(p->shortAddr,&source,2);
      }
      sprintf(buf,"{\"online\":\"%u\"}",(uint16)p->shortAddr);
      HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));
      //for(i=0;i<p->ledNum;i++)
      //{
     //   sprintf(buf,"%u  ",p->ledId[i]);
      //  HalUARTWrite(0,buf,osal_strlen(buf));
     // }
     HalUARTWrite(HAL_UART_PORT_0,"\r\n",2);
  }else if(command==HEART_BEAT_CMD_ID) {//收到终端节点的心跳包
      struct led_device_node *p=ledDeviceHeader.next;
      while(p!=NULL)
      {
        if( osal_memcmp(&source,p->shortAddr,2)==TRUE)
          break;
        else{
          p=p->next;
        }
      } 
      if(p!=NULL){
        p->lostHeartCount=HEART_BEAT_MAX_COUNT;//重置心跳计数值
      }
  }else if(command==TEMP_HUMI_CMD_ID){
   
    
    sprintf(buf,"{\"t\":\"th\",\"temp\":\"%d\",\"humi\":\"%d\"}",pData[0],pData[1]);
    HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));
    HalUARTWrite(HAL_UART_PORT_1,"\r\n",2);

    
  }else if(command==MQ2_CMD_ID){
    
    //HalUARTWrite(0,pData,osal_strlen(pData));   
    //HalUARTWrite(0,pData,4);
    sprintf(buf,"{\"t\":\"mq2\",\"mq2\":\"%c%c\"}",pData[0],pData[1],pData[2],pData[3]);
    HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));
    HalUARTWrite(HAL_UART_PORT_1,"\r\n",2);
  }else if(command==LIGHT_CMD_ID){
    
    //HalUARTWrite(0,pData,osal_strlen(pData));   
    //HalUARTWrite(0,pData,4);
    sprintf(buf,"{\"t\":\"light\",\"light\":\"%c%c\"}",pData[0],pData[1],pData[2],pData[3]);
    HalUARTWrite(HAL_UART_PORT_1,buf,osal_strlen(buf));
    HalUARTWrite(HAL_UART_PORT_1,"\r\n",2);
  }
  
  
}

//用户任务通过zb_FindDeviceRequest通过节点的物理地址获取节点的网络地址时的调用 
void zb_FindDeviceConfirm( uint8 searchType, 
                          uint8 *searchKey, uint8 *result )
{
  
}

void uart_receive(uint8 port,uint8 event){
  uint16 dstAddr;
  if(event& (HAL_UART_RX_FULL|HAL_UART_RX_ABOUT_FULL|HAL_UART_RX_TIMEOUT))
  {
    //这里不是很严谨，不能很好地分出一个逻辑数据包
    uint8 buf[5];
    //struct led_device_node *p=ledDeviceHeader.next;
    HalUARTRead(port,buf,2);
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

//其它节点绑定到该节点时调用
void zb_AllowBindConfirm( uint16 source )
{
  
}

//用来处理按键消息
void zb_HandleKeys( uint8 shift, uint8 keys )
{
  
}

void zb_BindConfirm( uint16 commandId, uint8 status )
{
}