#include "smartHome.h"
#include "sapi.h"
#include "hal_led.h"
#include"onBoard.h"
#include "hal_uart.h"
#include "stdio.h"
#include <iocc2530.h>

#define uchar unsigned char 
#define uint unsigned int
//#define  DHT11_DATA  P0_4
#if 0
  #define NUM_LED_1 1
  #define NUM_LED_2 2
#elif 1
  #define NUM_LED_1 3
  #define NUM_LED_2 4
#else
  #define NUM_LED_1 5
  #define NUM_LED_2 6
#endif

#define LEDNUM  2
uint8 ledIdList[LEDNUM]={NUM_LED_1,NUM_LED_2};

#define NUM_IN_CMD_LEDDEVICE 1
#define NUM_OUT_CMD_LEDDEVICE 2
uint ADC_value=0;            //定义ADC转换值
float ADC_result=0;          //定义ADC最终值
char ADC_data[3]; //定义串口数组



const cId_t ledDeviceInputCommandList[NUM_IN_CMD_LEDDEVICE]=
                                {TOGGLE_LED_CMD_ID};
const cId_t ledDeviceOutputCommandList[NUM_OUT_CMD_LEDDEVICE]=
                                {LEDJOINNET_CMD_ID,HEART_BEAT_CMD_ID};
const SimpleDescriptionFormat_t zb_SimpleDesc=
{
  ENDPOINT_ID_SMARTHOME,
  PROFILE_ID_SMARTHOME,
  DEVICE_ID_LEDDEVICE,
  DEVIDE_VERSION_ID,
  0,
  NUM_IN_CMD_LEDDEVICE,
  (cId_t*)ledDeviceInputCommandList,
  NUM_OUT_CMD_LEDDEVICE,
  (cId_t*)ledDeviceOutputCommandList  
};
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

/***********
执行时机：发送的数据包被接收方收到时被调用
handle:包的编号；
status:ZSUCCESS表示成功接收
************/
void zb_SendDataConfirm( uint8 handle, uint8 status )
{
  
}

/***********
执行时机：接收到的数据包被调用
************/
void zb_ReceiveDataIndication( uint16 source, uint16 command, 
                              uint16 len, uint8 *pData  )
{
  
}


void zb_AllowBindConfirm( uint16 source )
{
}

void zb_HandleKeys( uint8 shift, uint8 keys )
{
  
}

void zb_BindConfirm( uint16 commandId, uint8 status )
{
}


//void zb_SendDataRequest ( uint16 destination, uint16 commandId, uint8 len,
//                          uint8 *pData, uint8 handle, uint8 ack, uint8 radius );
void zb_StartConfirm( uint8 status )
{
  if(status==ZSUCCESS)
  {    
    halUARTCfg_t uartcfg;
    uartcfg.baudRate=HAL_UART_BR_115200;
    uartcfg.flowControl=FALSE;
    uartcfg.callBackFunc=NULL;
    HalUARTOpen(0,&uartcfg);
    HalUARTWrite(0,"join success\r\n",osal_strlen("join success\r\n"));
    //可把节点所包含的led灯的ID号发送过去
    zb_SendDataRequest(0X0,LEDJOINNET_CMD_ID,LEDNUM,ledIdList,0,FALSE,AF_DEFAULT_RADIUS);
    osal_start_timerEx(sapi_TaskID,TIMER_TIMEOUT_EVT,2000);  
    osal_start_timerEx(sapi_TaskID,READ_MQ2_EVENT,5000);
    osal_start_timerEx(sapi_TaskID,READ_LIGHT_EVENT,5000);
    
  }
}

void zb_HandleOsalEvent( uint16 event )
{
  if(event&READ_MQ2_EVENT){//读取DHT11数据事件
    osal_start_timerEx(sapi_TaskID,READ_MQ2_EVENT,5000);
    unsigned char mq2value[4];
    unsigned int AdcValue=0;
    AdcValue=readAdc(5);
    AdcValue=AdcValue/82;
    sprintf(mq2value,"%d\r\n", AdcValue);
    //sprintf(mq2value,"{\"mq2\":\"%u\"}\r\n",AdcValue);
    HalUARTWrite(0,mq2value,osal_strlen(mq2value));
    //把读取到的烟雾浓度值发给协调器
    zb_SendDataRequest(0X0,MQ2_CMD_ID,osal_strlen(mq2value),mq2value,0,FALSE,AF_DEFAULT_RADIUS); 
   
  }
  
  if(event&READ_LIGHT_EVENT){
    osal_start_timerEx(sapi_TaskID,READ_LIGHT_EVENT,5000);
    unsigned char mq2value[4];
    unsigned int AdcValue=0;
    AdcValue=readAdc(4);
    AdcValue=100-AdcValue/82;
    sprintf(mq2value,"%d\r\n", AdcValue);
    //sprintf(mq2value,"{\"mq2\":\"%u\"}\r\n",AdcValue);
    HalUARTWrite(0,mq2value,osal_strlen(mq2value));
    //把读取到的烟雾浓度值发给协调器
    zb_SendDataRequest(0X0,LIGHT_CMD_ID,osal_strlen(mq2value),mq2value,0,FALSE,AF_DEFAULT_RADIUS); 
   
  }
  
  
  if(event&TIMER_TIMEOUT_EVT)
  {
    osal_start_timerEx(sapi_TaskID,TIMER_TIMEOUT_EVT,2000);
    zb_SendDataRequest(0X0,HEART_BEAT_CMD_ID,
                       0,NULL,0,FALSE,AF_DEFAULT_RADIUS); 
  }
}

void zb_FindDeviceConfirm( uint8 searchType, 
                          uint8 *searchKey, uint8 *result )
{
  
}